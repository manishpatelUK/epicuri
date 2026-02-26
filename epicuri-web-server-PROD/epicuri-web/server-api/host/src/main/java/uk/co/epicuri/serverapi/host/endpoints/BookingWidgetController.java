package uk.co.epicuri.serverapi.host.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.booking.BookingRequest;
import uk.co.epicuri.serverapi.common.pojo.booking.BookingStaticsView;
import uk.co.epicuri.serverapi.common.pojo.booking.StaticsRequest;
import uk.co.epicuri.serverapi.common.pojo.booking.TimeSlots;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.booking.BookingStatics;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.BookingService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/booking/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class BookingWidgetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingWidgetController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @RequestMapping(value = "/statics", method = RequestMethod.POST)
    public ResponseEntity<BookingStaticsView> postStatics(@PathVariable("id") String id,
                                                          @NotNull @RequestBody StaticsRequest request) {
        String token = authenticationService.bookingWidgetLogin(id);
        BookingStatics statics = masterDataService.getBookingStaticsByLanguage(request.getLanguage());
        BookingStaticsView bookingStaticsView = new BookingStaticsView(statics, createTimes());
        bookingStaticsView.setToken(token);

        Restaurant restaurant = masterDataService.getRestaurantByStaffFacingId(id);
        bookingStaticsView.setEntityName(restaurant.getName());
        bookingStaticsView.setEntityNumber(restaurant.getPhoneNumber1());
        bookingStaticsView.setEntityEmail(restaurant.getPublicEmailAddress());

        return ResponseEntity.ok(bookingStaticsView);
    }

    private List<String> createTimes() {
        return new ArrayList<>(); //at some point this will check tolerances
    }

    @RequestMapping(value = "/reserve", method = RequestMethod.POST)
    public ResponseEntity<BookingRequest> postReserve(@PathVariable("id") String id,
                                                      @NotNull @RequestBody BookingRequest request) {
        if(!authenticationService.verifyBookingWidgetToken(id, request.getToken())) {
            return ResponseEntity.badRequest().build();
        }

        Restaurant restaurant = masterDataService.getRestaurantByStaffFacingId(id);
        CustomerReservationView customerReservationView = new CustomerReservationView(request, restaurant);
        customerReservationView.setInstantiatedFromId(ActivityInstantiationConstant.BOOKING_WIDGET.getId());

        if (hitsTolerances(customerReservationView)) {
            return ResponseEntity.badRequest().build();
        }

        int duration = ((Number)restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.RESERVATION_TIMESLOT)).findFirst().orElse(RestaurantDefault.newDefault("", 120)).getValue()).intValue();
        Booking reservationToInsert = new Booking(customerReservationView, null, System.currentTimeMillis(), duration);
        reservationToInsert.setOptedIntoMarketing(request.isMarketingOpt());
        reservationToInsert.setName(request.getName());
        bookingService.upsert(reservationToInsert);
        request.setAccepted(true);

        bookingService.sendBookingConfirmations(reservationToInsert);

        return ResponseEntity.ok(request);
    }

    @RequestMapping(value = "/reservecheck", method = RequestMethod.POST)
    public ResponseEntity<BookingRequest> postReserveCheck(@PathVariable("id") String id,
                                                           @NotNull @RequestBody BookingRequest request) {
        if(!authenticationService.verifyBookingWidgetToken(id, request.getToken())) {
            return ResponseEntity.badRequest().build();
        }

        Restaurant restaurant = masterDataService.getRestaurantByStaffFacingId(id);
        CustomerReservationView customerReservationView = new CustomerReservationView(request, restaurant);
        customerReservationView.setInstantiatedFromId(ActivityInstantiationConstant.BOOKING_WIDGET.getId());

        if (hitsTolerances(customerReservationView)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(request);
    }

    private boolean hitsTolerances(CustomerReservationView customerReservationView) {
        if(bookingService.checkBlackoutsFuseBox(customerReservationView)) {
            return true;
        }

        if(bookingService.checkFuseBox(customerReservationView).getIndividualNotices().size() > 0) {
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/timeslots", method = RequestMethod.POST)
    public ResponseEntity<TimeSlots> postTimeSlots(@PathVariable("id") String id,
                                                   @NotNull @RequestBody TimeSlots request) {
        if(!authenticationService.verifyBookingWidgetToken(id, request.getToken())) {
            return ResponseEntity.badRequest().build();
        }

        Restaurant restaurant = masterDataService.getRestaurantByStaffFacingId(id);

        for(String time : bookingService.getAllReservationTimeSlots()) {
            ZonedDateTime utcTime = TimeUtil.getUTCDateTime(request.getDate(), time, restaurant.getIANATimezone());
            CustomerReservationView customerReservationView = new CustomerReservationView(request, utcTime.toEpochSecond(), restaurant);
            if(!bookingService.checkTimesFuseBox(customerReservationView)) {
                request.getTimes().add(time);
            }
        }

        return ResponseEntity.ok(request);
    }


}
