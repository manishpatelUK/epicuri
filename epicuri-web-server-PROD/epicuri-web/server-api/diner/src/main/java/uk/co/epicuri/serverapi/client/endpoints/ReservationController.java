package uk.co.epicuri.serverapi.client.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Default;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.service.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Reservation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ReservationController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private final FuseBox customerReservationDeleteFuseBox;

    @Value("${epicuri.customer.blackmark.expiry}")
    private long blackMarkExpiry;

    public ReservationController() {
        this.customerReservationDeleteFuseBox = new FuseBox();

        //customer driven reservations deletion fusebox
        customerReservationDeleteFuseBox.add(FuseBox::checkReservationLockWindow);
        customerReservationDeleteFuseBox.finalise();
    }

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestBody CustomerReservationView reservation) {
        String customerId = authenticationService.getCustomerId(token);
        Customer customer = customerService.getCustomer(customerId);
        NoticeAggregator aggregator = bookingService.checkFuseBox(reservation, customer);
        setEmailOnReservation(reservation, customer);
        String response = checkNotices(aggregator);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage(response));
        }

        int duration = masterDataService.getRestaurantDefault(reservation.getRestaurantId(), FixedDefaults.RESERVATION_TIMESLOT).getAsOrDefault(Number.class, 120).intValue();
        Booking reservationToInsert = new Booking(reservation, customer, System.currentTimeMillis(), duration);
        if(aggregator.getIndividualNotices().size() > 0) {
            reservationToInsert.setRejectionNotice(aggregator.getNotice());
        }
        Booking saved = bookingService.upsertReservation(reservationToInsert);
        reservation.setId(saved.getId());

        bookingService.sendBookingConfirmations(saved);

        if(reservation.isAccepted()) {
            return ResponseEntity.created(URI.create(saved.getId())).body(reservation);
        }
        else {
            return ResponseEntity.accepted().body(reservation);
        }
    }

    private void setEmailOnReservation(@RequestBody CustomerReservationView reservation, Customer customer) {
        if(StringUtils.isBlank(reservation.getEmail()) && StringUtils.isNotBlank(customer.getEmail())) {
            reservation.setEmail(customer.getEmail());
        }
    }

    public String checkNotices(NoticeAggregator aggregator) {
        if(aggregator.getIndividualNotices().contains(NoticeAggregator.BOOKING_NOT_AVAILABLE_BLACK_MARKS_FRIENDLY_MESSAGE)) {
            return NoticeAggregator.BOOKING_NOT_AVAILABLE_BLACK_MARKS_FRIENDLY_MESSAGE;
        }

        if(aggregator.getIndividualNotices().contains(NoticeAggregator.BOOKING_TIME_TOO_SOON_MESSAGE)
                || aggregator.getIndividualNotices().contains(NoticeAggregator.BOOKING_TIME_IN_PAST_MESSAGE)
                || aggregator.getIndividualNotices().contains(NoticeAggregator.BOOKING_IN_BLACKOUT_MESSAGE)
                || aggregator.getIndividualNotices().contains(NoticeAggregator.BOOKING_ALREADY_EXISTS_MESSAGE)) {
            return NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE;
        }

        return null;
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> deleteReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                               @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        Booking reservation = bookingService.getBooking(id);

        if(reservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Reservation does not exist"));
        }
        if(!reservation.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("Cannot edit this reservation"));
        }

        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, reservation);

        try {
            customerReservationDeleteFuseBox.check(true, fuseBoxAggregationProxy); //will throw an error if not valid
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new StringMessage(NoticeAggregator.BOOKING_TOO_LATE_TO_CANCEL_MESSAGE));
        }

        bookingService.cancelBooking(id);

        return ResponseEntity.ok().build();
    }

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> putReservationCheck(@RequestHeader(Params.AUTHORIZATION) String token,
                                                 @RequestBody CustomerReservationView reservation) {
        String customerId = authenticationService.getCustomerId(token);
        Customer customer = customerService.getCustomer(customerId);

        if(bookingService.checkBlackoutsFuseBox(reservation)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
        }

        CustomerReservationCheck customerReservationCheck = new CustomerReservationCheck();
        NoticeAggregator aggregator = bookingService.checkFuseBox(reservation, customer);
        String reject = checkNotices(aggregator);
        if(reject != null) {
            customerReservationCheck.setWarning(Collections.singletonList(reject));
            return ResponseEntity.ok(customerReservationCheck);
        }

        if(aggregator.getIndividualNotices().size() > 0) {
            customerReservationCheck.setWarning(new ArrayList<>(aggregator.getIndividualNotices()));
        }

        return ResponseEntity.ok(customerReservationCheck);
    }

    @CustomerAuthRequired
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putReservation(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id,
                                            @RequestBody CustomerReservationView reservation) {
        String customerId = authenticationService.getCustomerId(token);
        Customer customer = customerService.getCustomer(customerId);
        if(StringUtils.isBlank(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Reservation not found"));
        }

        Booking booking = bookingService.getBooking(id);
        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Reservation not found"));
        }

        if(bookingService.checkBlackoutsFuseBox(reservation)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
        }

        NoticeAggregator aggregator = bookingService.checkFuseBoxForEdit(reservation, customer);
        String response = checkNotices(aggregator);
        if (response != null) {
            return ResponseEntity.badRequest().body(response);
        }

        booking.setNotes(reservation.getNotes());
        booking.setNumberOfPeople(reservation.getNumberOfPeople());
        booking.setTargetTime(reservation.getReservationTime() * 1000);
        booking.setAccepted(reservation.isAccepted());
        booking.setRejectionNotice(aggregator.getNotice());
        bookingService.upsert(booking);

        if(reservation.isAccepted()) {
            return ResponseEntity.created(URI.create(booking.getId())).body(reservation);
        }
        else {
            return ResponseEntity.accepted().body(reservation);
        }
    }

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getReservations(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestParam(value = "history", required = false) String history,
                                             @RequestParam(value = "restaurantId", required = false) String restaurantId) {
        String customerId = authenticationService.getCustomerId(token);
        Default aDefault = masterDataService.getDefaultByName(FixedDefaults.RESERVATION_FILTERTIME);

        long from = 0;
        long now = System.currentTimeMillis();
        if(history == null || !history.equals("all")) {
            long millis = Integer.valueOf(aDefault.getValue().toString()) * 3600000; //hours to millis
            from = now-millis;
        }

        List<Booking> reservations = bookingService.getBookingsByCustomerId(customerId, from, Long.MAX_VALUE, BookingType.RESERVATION);

        CheckIn checkIn = liveDataService.getCheckInByCustomer(customerId);
        //filter out reservation that is checked in against
        if(checkIn != null) {
            reservations = reservations.stream().filter(r -> !r.getId().equals(checkIn.getBookingId())).collect(Collectors.toList());
        }

        List<CustomerReservationView> list = new ArrayList<>();
        Map<String,Restaurant> cache = new HashMap<>();
        for(Booking reservation : reservations) {
            // leave out cancelled reservations that are in the past
            if(reservation.getTargetTime() < now && reservation.isCancelled()) {
                continue;
            }

            if(restaurantId != null && !restaurantId.equals(reservation.getRestaurantId())) {
                continue;
            }

            Restaurant restaurant = null;
            if(cache.containsKey(reservation.getRestaurantId())) {
                restaurant = cache.get(reservation.getRestaurantId());
            }
            else {
                restaurant = masterDataService.getRestaurant(reservation.getRestaurantId());
                cache.put(reservation.getRestaurantId(), restaurant);
            }

            list.add(new CustomerReservationView(reservation,restaurant));
        }

        Collections.sort(list);
        return ResponseEntity.ok(list);
    }
}
