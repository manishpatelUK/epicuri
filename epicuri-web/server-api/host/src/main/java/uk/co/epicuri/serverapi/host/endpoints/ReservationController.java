package uk.co.epicuri.serverapi.host.endpoints;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationRequest;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Reservation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ReservationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CustomerBindingService customerBindingService;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    private final FuseBox customerReservationCreationFuseBox;
    private final FuseBox customerReservationDeleteFuseBox;

    public ReservationController() {
        this.customerReservationCreationFuseBox = new FuseBox();
        this.customerReservationDeleteFuseBox = new FuseBox();

        //creation
        customerReservationCreationFuseBox.add(FuseBox::checkTimeBeforeNow);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationMinTime);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationsBlackouts);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxCoversPerReservation);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxActiveReservations);
        customerReservationCreationFuseBox.add(FuseBox::checkMaxActiveReservationsCovers);
        customerReservationCreationFuseBox.add(FuseBox::checkReservationAlreadyExists);
        customerReservationCreationFuseBox.finalise();

        //deletion
        customerReservationDeleteFuseBox.add(FuseBox::checkReservationLockWindow);
        customerReservationDeleteFuseBox.finalise();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getReservations(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestParam(value = "fromTime", defaultValue = "0") long fromTime, //in seconds
                                             @RequestParam(value = "toTime", defaultValue = "0") long toTime,     //in seconds
                                             @RequestParam(value = "pendingWaiterAction", defaultValue = "false") boolean pendingWaiterAction) {
        String restaurantId = authenticationService.getRestaurantId(token);

        List<Booking> reservations;

        if(fromTime == 0) {
            LocalTime midnight = LocalTime.MIDNIGHT;
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
            fromTime = todayMidnight.toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            fromTime = fromTime*1000;
        }

        if(toTime == 0) {
            reservations = bookingService.getReservationsFrom(restaurantId, fromTime);
        } else {
            reservations = bookingService.getReservations(restaurantId, fromTime, toTime*1000);
        }

        if(pendingWaiterAction) {
            reservations = reservations.stream().filter(
                    r -> !r.isAccepted()
                    && !r.isRejected()
                    && r.getDeleted() == null).collect(Collectors.toList());
        } else {
            reservations = reservations.stream().filter(
                    r -> !r.isRejected()).collect(Collectors.toList());
        }

        //order by reservation time
        reservations = reservations.stream().sorted(Comparator.comparingLong(Booking::getTargetTime)).collect(Collectors.toList());
        return ResponseEntity.ok(convert(restaurantId, reservations));
    }

    @HostAuthRequired
    @RequestMapping(value = "/Accept/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putAccepted(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        booking.setAccepted(true);
        bookingService.upsert(booking);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Reject/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putRejected(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id,
                                         @NotNull @RequestBody StringMessage message) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        bookingService.rejectBooking(booking.getId(), message.getNotice());

        CheckIn checkIn = liveDataService.getCheckInByCustomer(booking.getCustomerId());
        if(checkIn != null && checkIn.getRestaurantId().equals(restaurantId)) {
            liveDataService.softDeleteCheckIn(checkIn.getId());
        }

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/Arrived/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putArrived(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        Party party = liveDataService.getPartyByBookingId(restaurantId, booking.getId());
        if(party == null) {
            party = new Party(booking);
        }

        party.setArrivedTime(System.currentTimeMillis());

        party = party.getId() == null ? liveDataService.insert(party) : liveDataService.upsert(party);
        if(StringUtils.isNotBlank(party.getCustomerId())) {
            liveDataService.tiePartyCheckIn(party.getCustomerId(), party);
        }

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @NotNull @RequestBody HostReservationRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);

        Customer customer = null;
        if(request.getLeadCustomerId() != null) {
            customer = customerService.getCustomer(request.getLeadCustomerId());
        }
        Booking booking;
        if(customer != null) {
            booking = new Booking(restaurantId, request, customer, authenticationService.getStaffId(token));
        } else {
            // one last chance to look up a customer by email/phone
            if(StringUtils.isNotBlank(request.getEmail())) {
                customer = customerService.getCustomerByEmail(request.getEmail().trim());
            }
            if(customer == null && request.getPhoneNumber() != null && request.getPhoneNumber().length() > 6) {
                String phone = phoneNumberValidationService.trimPhoneNumber(request.getPhoneNumber());
                customer = customerService.getCustomerByPhone(phone);
            }
            if(customer != null) {
                booking = new Booking(restaurantId, request, customer, authenticationService.getStaffId(token));
            } else {
                booking = new Booking(restaurantId, request, authenticationService.getStaffId(token));
            }
        }

        booking.setAccepted(true);
        booking.setRejected(false);
        booking.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
        booking.setTableId(request.getTableId());
        booking.setDuration(request.getDuration());
        booking = bookingService.insert(booking);

        if(customer == null) {
            Booking finalBooking = booking;
            customerBindingService.onBookingCreation(booking).thenAccept(v -> bookingService.sendBookingConfirmations(finalBooking));
        } else {
            bookingService.sendBookingConfirmations(booking);
        }
        List<HostReservationView> list = convert(restaurantId, Collections.singletonList(booking));

        return ResponseEntity.ok(list.get(0));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id,
                                            @NotNull @RequestBody HostReservationRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        booking.setNotes(request.getNotes());
        booking.setName(request.getName());
        booking.setTelephone(request.getPhoneNumber());
        booking.setCustomerId(request.getLeadCustomerId());
        booking.setTargetTime(request.getReservationTime()*1000);
        booking.setNumberOfPeople(request.getNumberInParty());
        booking.setTableId(request.getTableId());
        booking.setDuration(request.getDuration());
        booking.setOmitFromChecks(request.isOmitFromChecks());
        if(StringUtils.isBlank(booking.getCustomerId()) && StringUtils.isNotBlank(request.getLeadCustomerId())) {
            booking.setCustomerId(request.getLeadCustomerId());
        }
        booking.setAccepted(true);

        bookingService.upsert(booking);

        return ResponseEntity.ok().build(); //waiter app doesn't currently expect response
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        List<HostReservationView> list = convert(restaurantId, Collections.singletonList(booking));

        return ResponseEntity.ok(list);
    }

    private Booking getBooking(String id, String restaurantId) {
        Booking booking = bookingService.getBookingIncludeCancelled(id);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            //sometimes the id is actually the party id from the waiter app
            Party party = liveDataService.getParty(id);
            if(party != null && StringUtils.isNotBlank(party.getBookingId())) {
                booking = bookingService.getBooking(party.getBookingId());
            }

            if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
                booking = null;
            }
        }

        return booking;
    }

    private List<HostReservationView> convert(String restaurantId, List<Booking> bookings) {
        List<String> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toList());
        Map<String,Party> partiesByBookingId = liveDataService.getPartyByBookingId(restaurantId, bookingIds).stream().collect(Collectors.toMap(Party::getBookingId, Function.identity()));
        Map<String,Session> sessionsByBookingId = sessionService.getSessionsByBookingIds(restaurantId, bookingIds).stream().collect(Collectors.toMap(Session::getOriginalBookingId, Function.identity()));
        List<String> customerIds = bookings.stream().filter(b -> StringUtils.isNotBlank(b.getCustomerId())).map(Booking::getCustomerId).collect(Collectors.toList());
        Map<String,Customer> customersById = customerService.getCustomers(customerIds).stream().collect(Collectors.toMap(Customer::getId, Function.identity()));

        Map<String,CheckIn> checkInsByBookingId = liveDataService.getCheckInsByBookingIds(restaurantId, bookingIds).stream().collect(Collectors.toMap(CheckIn::getBookingId, Function.identity()));

        List<RestaurantDefault> defaults = masterDataService.getRestaurant(restaurantId).getRestaurantDefaults();
        RestaurantDefault birthdayTimeSpan = defaults.stream().filter(d -> d.getName().equals(FixedDefaults.BIRTHDAY_TIMESPAN)).findFirst().orElse(null);
        RestaurantDefault walkInExpiration = defaults.stream().filter(d -> d.getName().equals(FixedDefaults.WALKIN_EXPIRATION_TIME)).findFirst().orElse(null);
        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        List<HostReservationView> hostReservationViews = new ArrayList<>();
        bookings.forEach(b -> hostReservationViews.add(
                new HostReservationView(b,
                        partiesByBookingId.get(b.getId()),
                        sessionsByBookingId.get(b.getId()),
                        customersById.get(b.getCustomerId()),
                        birthdayTimeSpan,
                        checkInsByBookingId.get(b.getId()),
                        walkInExpiration,
                        allPreferences)));

        return hostReservationViews;
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                               @RequestParam(value = "withPrejudice", defaultValue = "false") boolean withPrejudice,
                                               @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = getBooking(id, restaurantId);

        if(booking == null || !booking.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found");
        }

        if(StringUtils.isNotBlank(booking.getCustomerId()) && withPrejudice) {
            customerService.addBlackMark(booking.getCustomerId());
        }

        List<String> checkIns = liveDataService.getCheckInsByBookingId(restaurantId, booking.getId()).stream().map(CheckIn::getId).collect(Collectors.toList());
        liveDataService.softDeleteCheckIn(checkIns);

        List<Party> parties = liveDataService.getPartyByBookingId(restaurantId, Collections.singletonList(booking.getId()));
        parties.forEach(p -> liveDataService.deleteParty(p.getId()));

        bookingService.cancelBooking(booking.getId());

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/ReservationCheck", method = RequestMethod.POST)
    public ResponseEntity<?> postReservationCheck(@RequestHeader(Params.AUTHORIZATION) String token,
                                                  @NotNull @RequestBody HostReservationRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, restaurantId, request);

        NoticeAggregator output = customerReservationCreationFuseBox.check(fuseBoxAggregationProxy);

        Map<String,Set<String>> response = new HashMap<>();
        response.put("Warning", output.getIndividualNotices());
        return ResponseEntity.ok(response);
    }
}
