package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostSessionView;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Take{var:[Aa]}way", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class TakeawayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TakeawayController.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerBindingService customerBindingService;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    private final FuseBox simpleTakeawayCreationFuseBox;
    private final FuseBox takeawayCreationFuseBox;

    public TakeawayController() {
        simpleTakeawayCreationFuseBox = new FuseBox();
        takeawayCreationFuseBox = new FuseBox();

        simpleTakeawayCreationFuseBox.add(FuseBox::checkTakeawayLockWindow);
        simpleTakeawayCreationFuseBox.add(FuseBox::checkAddressExistence);
        simpleTakeawayCreationFuseBox.add(FuseBox::checkMaxDeliveryRadius);

        takeawayCreationFuseBox.add(FuseBox::checkTakeawaysBlackouts);
        takeawayCreationFuseBox.add(FuseBox::checkMaxTakeawaysPerHour);
        takeawayCreationFuseBox.add(FuseBox::checkDeliveryAddressPresence);
        takeawayCreationFuseBox.add(FuseBox::checkMaxDeliveryRadius);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<HostSessionView>> getTakeaways(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @RequestParam(value = "fromTime", defaultValue = "0") long fromTime, //in seconds
                                          @RequestParam(value = "toTime", defaultValue = "0") long toTime,     //in seconds
                                          @RequestParam(value = "pendingWaiterAction", defaultValue = "false") boolean pendingWaiterAction) {
        String restaurantId = authenticationService.getRestaurantId(token);

        boolean timeSpecified = false;
        if(fromTime == 0) {
            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
            fromTime = todayMidnight.toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            fromTime = fromTime * 1000; //millis
            timeSpecified = true;
        }

        if(toTime == 0) {
            toTime = System.currentTimeMillis() + 7776000000L; //3 months, make configurable
        } else {
            toTime = toTime * 1000; //millis
        }

        LOGGER.trace("Get takeaways for {} between {} and {} UTC", restaurantId, fromTime, toTime);
        List<HostSessionView> sessions = sessionService.getAllTakeawaySessions(restaurantId, fromTime, toTime);
        LOGGER.trace("Found {} sessions", sessions.size());
        if(pendingWaiterAction) {
            List<HostSessionView> filtered = sessions.stream().filter(s -> !s.isAccepted() && !s.isRejected()).collect(Collectors.toList());
            LOGGER.trace("Filter pendingWaiterAction, size={}", sessions.size());
            return ResponseEntity.ok(filtered);
        } else {
            if(timeSpecified) {
                LOGGER.trace("Time specified, return all sessions");
                return ResponseEntity.ok(sessions);
            } else {
                return ResponseEntity.ok(sessions.stream().filter(HostSessionView::isAccepted).collect(Collectors.toList()));
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postTakeaway(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @NotNull @RequestBody TakeawayPayload takeawayPayload) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(StringUtils.isBlank(takeawayPayload.getRestaurantId())) {
            takeawayPayload.setRestaurantId(restaurantId);
        }

        String messages = null;

        if(takeawayPayload.getRequestedTime() < (System.currentTimeMillis()-(1000*60*5))/1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Requested time in the past");
        }

        if(takeawayPayload.isDelivery()) {
            FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, takeawayPayload);
            NoticeAggregator output = simpleTakeawayCreationFuseBox.check(fuseBoxAggregationProxy);
            messages = output.getNotice();
        }

        takeawayPayload.setRestaurantId(restaurantId);
        Session session = sessionService.createTakeaway(takeawayPayload, messages, false);

        //bind/create the customer
        if(takeawayPayload.getLeadCustomerId() == null) {
            customerBindingService.onBookingCreation(session.getOriginalBookingId());
        }

        return createTakeawayResponse(takeawayPayload, restaurantId, session);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> putTakeawayCheck(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @NotNull @RequestBody TakeawayPayload takeawayPayload) {
        if(StringUtils.isBlank(takeawayPayload.getRestaurantId())) {
            takeawayPayload.setRestaurantId(authenticationService.getRestaurantId(token));
        }

        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, takeawayPayload);
        NoticeAggregator output = simpleTakeawayCreationFuseBox.check(fuseBoxAggregationProxy);

        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        Map<String,Object> warnings = new HashMap<>();
        if(takeawayPayload.isDelivery() && takeawayPayload.getAddress() != null) {
            if(restaurant.getAddress() != null) {
                Map<String,Object> restaurantDefaults = RestaurantDefault.asMap(restaurant.getRestaurantDefaults());
                double calculatedDistance = fuseBoxAggregationProxy.calculateDistance(restaurant.getAddress(), takeawayPayload.getAddress());
                double cutOff = (Double)restaurantDefaults.getOrDefault(FixedDefaults.FREE_DELIVERY_RADIUS, 2D);
                int calculatedCost = fuseBoxAggregationProxy.getDeliveryCost(calculatedDistance, cutOff);
                if(calculatedCost > 0) {
                    warnings.put("Cost", MoneyService.toMoneyRoundNearest(calculatedCost));
                }
            }
        }

        Set<String> individualNotices = output.getIndividualNotices();
        Set<String> actualNotices = new HashSet<>();
        for(String notice : individualNotices) {
            if(notice.equals(NoticeAggregator.TAKEAWAY_WITHIN_LOCK_WINDOW_PARTIAL_MESSAGE)) {
                RestaurantDefault restaurantDefault = restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.TAKEAWAY_LOCK_WINDOW)).findFirst().orElse(null);
                String limit = restaurantDefault == null ? "30" : restaurantDefault.getValue().toString();
                actualNotices.add(String.format(notice, limit));
            } else {
                actualNotices.add(notice);
            }
        }

        warnings.put("Warning", actualNotices);

        return ResponseEntity.ok(warnings);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putTakeaway(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id,
                                         @NotNull @RequestBody TakeawayPayload takeawayPayload) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(StringUtils.isBlank(takeawayPayload.getRestaurantId())) {
            takeawayPayload.setRestaurantId(restaurantId);
        }

        Session session = sessionService.getSession(id);
        if(session == null || !session.getRestaurantId().equals(restaurantId) || session.getSessionType() != SessionType.TAKEAWAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
        }

        Booking booking = bookingService.getBookingIncludeCancelled(session.getOriginalBookingId());
        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        }
        booking.setTargetTime(takeawayPayload.getExpectedTime() * 1000);
        booking.setTelephone(takeawayPayload.getTelephone());
        booking.setName(takeawayPayload.getName());
        booking.setCancelled(false);
        booking.setNotes(takeawayPayload.getMessage());
        booking.setCustomerId(takeawayPayload.getLeadCustomerId());

        if(takeawayPayload.isDelivery()) {
            session.setTakeawayType(TakeawayType.DELIVERY);
            booking.setTakeawayType(TakeawayType.DELIVERY);
            if(takeawayPayload.getAddress() == null && takeawayPayload.getLeadCustomerId() != null) {
                Customer customer = customerService.getCustomer(takeawayPayload.getLeadCustomerId());
                if(customer != null) {
                    takeawayPayload.setAddress(customer.getAddress());
                }
            } else {
                booking.setDeliveryAddress(takeawayPayload.getAddress());
            }
        } else {
            session.setTakeawayType(TakeawayType.COLLECTION);
            booking.setTakeawayType(TakeawayType.COLLECTION);
        }

        session.setOriginalBooking(booking);
        session.setOriginalBookingId(booking.getId());
        session.setName(booking.getName());
        session.setBillRequested(takeawayPayload.isRequestedBill());
        session.setStartTime(booking.getTargetTime());

        if(takeawayPayload.isDelivery()
                && takeawayPayload.getAddress() != null
                && booking.getDeliveryAddress() != null
                && !takeawayPayload.getAddress().equals(booking.getDeliveryAddress())) {
            booking.setDeliveryAddress(takeawayPayload.getAddress());
            sessionService.updateDeliveryCost(id, sessionCalculationService.calculateDeliveryCost(session));
        }

        sessionService.upsert(session);
        bookingService.upsert(booking);

        return createTakeawayResponse(takeawayPayload, restaurantId, session);
    }

    private ResponseEntity<?> createTakeawayResponse(TakeawayPayload takeawayPayload,
                                            String restaurantId,
                                            Session session) {
        Map<CalculationKey,Number> calculatedValues = sessionCalculationService.calculateValues(session, new ArrayList<>());

        Map<String, RestaurantDefault> defaultMap = masterDataService.getRestaurant(restaurantId).getRestaurantDefaults()
                .stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));

        Map<String,Staff> allStaff = new HashMap<>();
        if(session.getVoidReason() != null){
            allStaff = masterDataService.getAllStaff(session.getRestaurantId()).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));
        }

        Customer customer = null;
        if(StringUtils.isNotBlank(takeawayPayload.getLeadCustomerId())) {
            customer = customerService.getCustomer(takeawayPayload.getLeadCustomerId());
        }

        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        List<Order> orders = liveDataService.getOrders(session.getId());

        HostSessionView hostSessionView = new HostSessionView(
                    session,
                    orders,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0,
                    defaultMap,
                    calculatedValues,
                    new ArrayList<>(), //no need for customer list
                    customer,
                    allStaff,
                    allPreferences,
                    new BillSplit(),
                    SessionCalculationService.isPaid(calculatedValues));

        return ResponseEntity.ok(hostSessionView);
    }

    // this cancels a takeaway, not actually delete it
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTakeaway(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Session session = sessionService.getSession(id);
        if(session == null || !session.getRestaurantId().equals(restaurantId) || session.getSessionType() != SessionType.TAKEAWAY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found.");
        }

        if(StringUtils.isNotBlank(session.getOriginalBookingId())) {
            bookingService.cancelBooking(session.getOriginalBookingId());
        }

        sessionPaymentService.ensurePreAuthsAreCancelled(session);
        sessionService.updateDelete(session.getId());
        sessionService.clear(session, true, true, true, true, true);

        return ResponseEntity.ok().build();
    }
}

