package uk.co.epicuri.serverapi.client.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCheckInView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.errors.LockStateException;
import uk.co.epicuri.serverapi.service.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/Checkin", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CheckInController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInController.class);

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LockingService lockingService;


    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postCheckIn(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestBody CustomerCheckInView checkInPayload) {
        String customerId = authenticationService.getCustomerId(token);

        if(StringUtils.isBlank(checkInPayload.getRestaurantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Restaurant not found"));
        }

        //get restaurant object, check if restaurant id is valid
        Restaurant restaurant = masterDataService.getRestaurant(checkInPayload.getRestaurantId());
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Restaurant not found"));
        }

        SessionLock sessionLock = null;
        try {
            sessionLock = lockingService.lock(customerId, SessionLockType.CHECK_IN);
            return createOrGetCheckIn(checkInPayload, customerId, restaurant);
        } catch (LockStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Check In progressing, please wait");
        } finally {
            lockingService.release(sessionLock);
        }
    }

    private ResponseEntity<?> createOrGetCheckIn(@RequestBody CustomerCheckInView checkInPayload, String customerId, Restaurant restaurant) {
        Customer customer = customerService.getCustomer(customerId);
        List<CheckIn> currentCheckIns = liveDataService.getCheckInsForCustomer(customerId);

        Collections.reverse(currentCheckIns);
        CheckIn currentCheckIn = currentCheckIns.stream().filter(c -> c.getRestaurantId() != null && c.getRestaurantId().equals(restaurant.getId())).findFirst().orElse(null);
        LOGGER.trace("Current CheckIn: {}", currentCheckIn);

        //if no such checkin exists, create it and return
        if(currentCheckIn == null) {
            //check min time if there is a reservation
            if(checkInPayload.getReservationId() != null) {
                if(isCheckInTooSoon(restaurant,checkInPayload.getReservationId())) {
                    return ResponseEntity.badRequest().body(new StringMessage("Too soon for a Check In, please try again later!"));
                }
            }

            CheckIn newCheckIn = liveDataService.createCheckInAndParty(checkInPayload.getRestaurantId(), null, customer, checkInPayload.getNumberOfPeople(), true);
            return ResponseEntity.created(URI.create("Checkin")).body(new CustomerCheckInView(newCheckIn));
        } else {
            // if there is no session or party attached to checkin, and it has timed out, clean it up
            long expiration = getExpirationTime(masterDataService, currentCheckIn);
            if(isCheckInExpired(currentCheckIn, expiration)) {
                liveDataService.clearCheckIn(restaurant.getId(), customerId);
                CheckIn newCheckIn = liveDataService.createCheckInAndParty(checkInPayload.getRestaurantId(), null, customer, checkInPayload.getNumberOfPeople(), true);
                return ResponseEntity.created(URI.create("Checkin")).body(new CustomerCheckInView(newCheckIn));
            }

            //else if there is a corresponding session or party, make sure it's not already closed
            if(currentCheckIn.getSessionId() != null) {
                Session session = sessionService.getSession(currentCheckIn.getSessionId());
                if(session.getClosedTime() != null) {
                    liveDataService.clearCheckIn(restaurant.getId(), customerId);
                    CheckIn newCheckIn = liveDataService.createCheckInAndParty(checkInPayload.getRestaurantId(), null, customer,  checkInPayload.getNumberOfPeople(), true);
                    return ResponseEntity.created(URI.create("Checkin")).body(new CustomerCheckInView(newCheckIn));
                }
            }

            //return the existing check in
            CustomerCheckInView customerCheckInView = new CustomerCheckInView(currentCheckIn);
            return ResponseEntity.ok(customerCheckInView);
        }
    }

    private boolean isCheckInTooSoon(Restaurant restaurant, String reservationId) {
        Booking booking = bookingService.getBooking(reservationId);
        if(booking == null) {
            return false;
        }

        RestaurantDefault minTime = restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.RESERVATION_MINIMUM_TIME)).findFirst().orElse(null);
        long value = 1000 * 60 * 60 * 2;
        if (minTime != null) {
            value = ((Number) minTime.getValue()).intValue() * 60 * 1000;
        }

        return System.currentTimeMillis() < (booking.getTargetTime() - value);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getCheckIn(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        CheckIn currentCheckIn = liveDataService.getCheckIn(id);

        if(currentCheckIn == null || currentCheckIn.getCustomerId() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Check In not found"));
        }

        if(!currentCheckIn.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Check In not found"));
        }

        long expiration = getExpirationTime(masterDataService, currentCheckIn);
        if(isCheckInExpired(currentCheckIn, expiration)) {
            liveDataService.cancelCheckInAndParty(currentCheckIn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Check In not found"));
        }
        return ResponseEntity.ok(new CustomerCheckInView(currentCheckIn));

    }

    public static boolean isCheckInExpired(CheckIn currentCheckIn, long expiration) {
        return (currentCheckIn.getTime() < System.currentTimeMillis()-expiration) && currentCheckIn.getPartyId() == null && currentCheckIn.getSessionId() == null;
    }

    public static long getExpirationTime(MasterDataService masterDataService, CheckIn currentCheckIn) {
        long expiration;
        if(currentCheckIn.getBookingId() != null) {
            expiration = 1000 * 60 * (Integer) masterDataService.getRestaurantDefault(currentCheckIn.getRestaurantId(), FixedDefaults.CHECKIN_EXPIRATION_TIME).getValue();
        } else {
            expiration = 1000 * 60 * (Integer) masterDataService.getRestaurantDefault(currentCheckIn.getRestaurantId(), FixedDefaults.WALKIN_EXPIRATION_TIME).getValue();
        }
        return expiration;
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> deleteCheckIn(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        CheckIn currentCheckIn = liveDataService.getCheckIn(id);

        if(currentCheckIn != null && !currentCheckIn.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("CheckIn Not Found"));
        }

        if(currentCheckIn != null) {
            liveDataService.cancelCheckInAndParty(currentCheckIn);
        }

        if(currentCheckIn != null && currentCheckIn.getSessionId() != null) {
            Session session = sessionService.getSession(currentCheckIn.getSessionId());
            if(session != null) {
                removeCheckIn(session, currentCheckIn);
            }
        }

        return ResponseEntity.noContent().build();
    }

    private void removeCheckIn(Session session, CheckIn currentCheckIn) {
        for(Diner diner : session.getDiners()) {
            if(diner.getCustomerId() != null && diner.getCustomerId().equals(currentCheckIn.getCustomerId())) {
                diner.setCustomerId(null);
                diner.setCustomer(null);
                sessionService.upsert(session);
                break;
            }
        }
    }
}
