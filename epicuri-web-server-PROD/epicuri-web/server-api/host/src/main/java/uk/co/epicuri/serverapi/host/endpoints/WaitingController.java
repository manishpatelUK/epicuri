package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderAndAdjustmentsPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "Waiting", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class WaitingController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BatchService batchService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postWaiting(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @NotNull @RequestBody WaitingPartyPayload payload) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(payload.getCreateSession() != null && payload.getCreateSession()) {
            Session session;
            Party party;
            try {
                session = sessionService.createSession(payload,restaurantId);
                party = session.getOriginalParty();
                if(payload.getCustomer() != null) {
                    liveDataService.setSessionDataOnCheckin(session.getRestaurantId(), party.getCustomerId(), session.getId(), party.getId());
                }
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            }

            return ResponseEntity.created(URI.create("/Party/" + party.getId())).body(new PartyResponse(party, session));
        } else {
            Party party = sessionService.createPartyFromPayload(payload, restaurantId);
            if(payload.getCustomer() != null) {
                liveDataService.setPartyIdOnCheckin(restaurantId, payload.getCustomer().getId(), party.getId());
            }
            return ResponseEntity.created(URI.create("/Party/" + party.getId())).body(new PartyResponse(party));
        }
    }

    // used for Quick Order
    @HostAuthRequired
    @RequestMapping(value = "PostWaitingWithOrder", method = RequestMethod.POST)
    public ResponseEntity<?> postPartyWithOrder(@RequestHeader(Params.AUTHORIZATION) String token,
                                                @NotNull @RequestBody WaitingPartyOrderPayload payload,
                                                @RequestParam(value = "orderPrintsRequired", defaultValue = "true", required = false) boolean orderPrintsRequired) {
        Tuple<Session,OrderResponse> tuple;
        try {
            tuple = createAdhocSession(token, payload, true, orderPrintsRequired);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

        return ResponseEntity.ok(tuple.getB());
    }

    private Tuple<Session,OrderResponse> createAdhocSession(String token, WaitingPartyOrderPayload payload, boolean willAttemptImmediatePrint, boolean orderPrintsRequired) throws IllegalArgumentException{
        String restaurantId = authenticationService.getRestaurantId(token);
        Session session;
        try {
            session = sessionService.createSession(payload.getParty(),restaurantId);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        if(session == null) {
            throw new IllegalArgumentException("Session could not be created");
        }

        if(session.getService().getCourses().size() == 0) {
            throw new IllegalArgumentException("Default Service has not been set up properly with a Course");
        }

        if(payload instanceof WaitingPartyOrderAndAdjustmentsPayload) {
            session = sessionService.addAdjustments(session, authenticationService.getStaffId(token), ((WaitingPartyOrderAndAdjustmentsPayload)payload).getAdjustments());
        }

        List<MenuItem> allMenuItems = masterDataService.getAllMenuItems(restaurantId);
        Map<String,MenuItem> itemMap = allMenuItems.stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        final String staffId = authenticationService.getStaffId(token);
        List<Order> orders = new ArrayList<>();
        Session finalSession = session;
        payload.getOrder().forEach(o -> {
            MenuItem item = itemMap.get(o.getMenuItemId());
            if(o.getQuantity() > 0
                    && itemMap.containsKey(o.getMenuItemId())
                    && StringUtils.isNotBlank(item.getDefaultPrinter()))  {
                Integer price = null;
                if(o.getPrice() != null) {
                    price = MoneyService.toPenniesRoundNearest(o.getPrice());
                }
                List<Modifier> modifiersWithTaxes = masterDataService.getModifiers(o.getModifiers());
                modifiersWithTaxes.forEach(m -> {
                    if(m.getTaxTypeId() != null) {
                        m.setTaxRate(taxRateMap.get(m.getTaxTypeId()));
                    }
                });

                Order order = new Order(finalSession, item, taxRateMap.get(item.getTaxTypeId()), o, modifiersWithTaxes, price, staffId);
                if(StringUtils.isNotBlank(payload.getOrderLocation())) {
                    order.setDeliveryLocation(payload.getOrderLocation());
                }
                orders.add(order);
            }
        });
        liveDataService.insertOrders(finalSession, allMenuItems, orders, session.getSessionType() != SessionType.REFUND, orderPrintsRequired);
        if(!willAttemptImmediatePrint) {
            List<Batch> batches = liveDataService.getBatchesForOrders(orders);
            if(batches.size() > 0) {
                liveDataService.updateImmediatePrint(batches.stream().map(Batch::getId).collect(Collectors.toList()), false);
            }
        }

        List<HostBatchView> list = batchService.getHostBatchViews(restaurantId,
                Collections.singletonList(finalSession),
                finalSession.getOriginalBookingId() == null ? new HashMap<>() : Collections.singletonMap(finalSession.getOriginalBookingId(), finalSession.getOriginalBooking()),
                orders.stream().collect(Collectors.groupingBy(Order::getSessionId)),
                true);

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        List<Table> tables = restaurant.getTables().stream().filter(t -> finalSession.getTables().contains(t.getId())).collect(Collectors.toList());
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setBatches(list);
        orderResponse.setSessionId(finalSession.getId());
        Map<CalculationKey, Number> calculatedValues = sessionCalculationService.calculateValues(finalSession, orders);
        HostSessionView hostSessionView = new HostSessionView(
                finalSession,
                orders,
                tables,
                new ArrayList<>(),
                0L,
                restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity())),
                calculatedValues,
                new ArrayList<>(),
                null,
                new HashMap<>(),
                new HashMap<>(),
                new BillSplit(),
                SessionCalculationService.isPaid(calculatedValues)
                );
        orderResponse.setHostSessionView(hostSessionView);

        return new Tuple<>(finalSession, orderResponse);
    }

    @HostAuthRequired
    @RequestMapping(value = "PostWaitingWithOrderAndAdjustments", method = RequestMethod.POST)
    public ResponseEntity<?> postPartyWithOrderAndAdjustments(@RequestHeader(Params.AUTHORIZATION) String token,
                                                              @NotNull @RequestBody WaitingPartyOrderAndAdjustmentsPayload payload) {
        Tuple<Session,OrderResponse> tuple;
        try {
            tuple = createAdhocSession(token, payload, true, true);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

        //add adjustments
        Session session = tuple.getA();
        if(sessionService.isFullyPaid(session)) {
            sessionService.markPaid(session.getId(), true, authenticationService.getStaffId(token));
        }

        return ResponseEntity.ok(tuple.getB());
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteParty(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id,
                                         @RequestParam(value = "withPrejudice", defaultValue = "false") boolean addBlackMark) {

        Party party = liveDataService.getParty(id);
        if(party == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        liveDataService.deleteParty(id);

        if(addBlackMark && StringUtils.isNotBlank(party.getCustomerId())) {
            Customer customer = customerService.getCustomer(party.getCustomerId());
            if(customer != null) {
                BlackMark blackMark = new BlackMark();
                blackMark.setTime(System.currentTimeMillis());
                blackMark.setReason("Waiting List");
                customer.getBlackMarks().add(blackMark);
                customerService.addBlackMark(customer.getId(), blackMark);
            }
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        List<CheckIn> checkIns = liveDataService.getCheckIns(restaurantId);
        List<String> checkInsDelete = checkIns.stream().filter(c ->
                                        (c.getCustomerId() != null && party.getCustomerId() != null && c.getCustomerId().equals(party.getCustomerId()))
                                        || (c.getPartyId() != null && c.getPartyId().equals(party.getId()))).map(CheckIn::getId).collect(Collectors.toList());
        liveDataService.softDeleteCheckIns(checkInsDelete);
        return ResponseEntity.noContent().build();
    }

}
