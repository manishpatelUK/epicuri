package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.errors.IllegalStateResponseException;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/Order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private CustomerTakeawayService customerTakeawayService;

    @CustomerAuthRequired
    @RequestMapping(value = "/Takeaway", method = RequestMethod.POST)
    public ResponseEntity<?> postTakeaway(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @NotNull @RequestBody CustomerTakeawayOrderRequest order) {
        String customerId = authenticationService.getCustomerId(token);
        try {
            return ResponseEntity.ok(customerTakeawayService.createTakeaway(customerId, order));
        } catch (IllegalStateResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(new StringMessage(e.getResponseMessage()));
        }
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/TakeawayCheck", method = RequestMethod.PUT)
    public ResponseEntity<?> takeawayCheck(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @NotNull @RequestBody CustomerTakeawayOrderRequest order) {
        String customerId = authenticationService.getCustomerId(token);
        try {
            return ResponseEntity.ok(customerTakeawayService.checkTakeaway(customerId, order));
        } catch (IllegalStateResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(new StringMessage(e.getResponseMessage()));
        }
    }


    @CustomerAuthRequired
    @RequestMapping(value = "/Takeaway", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getTakeaways(@RequestHeader(Params.AUTHORIZATION) String token) {
        String customerId = authenticationService.getCustomerId(token);
        Map<Booking,Session> bookingToSession = liveDataService.getAssociatedSessionsByBookingRecent(customerId, BookingType.TAKEAWAY);

        List<CustomerTakeawayResponseView> list = new ArrayList<>();
        for(Map.Entry<Booking,Session> entry : bookingToSession.entrySet()) {
            Session session = entry.getValue();
            CustomerTakeawayResponseView customerTakeawayResponseView = customerTakeawayService.getCustomerTakeawayResponseView(entry.getKey(), session);
            list.add(customerTakeawayResponseView);
        }

        return ResponseEntity.ok(list);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/Takeaway/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getTakeaway(@RequestParam(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        Map<Booking,Session> bookingToSession = liveDataService.getAssociatedSessionsByBookingRecent(customerId, BookingType.TAKEAWAY);
        Booking booking = bookingToSession.keySet().stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);

        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Booking not found"));
        }

        CustomerTakeawayResponseView customerTakeawayResponseView = customerTakeawayService.getCustomerTakeawayResponseView(booking, bookingToSession.get(booking));

        return ResponseEntity.ok(customerTakeawayResponseView);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/Takeaway/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTakeaway(@RequestHeader(Params.AUTHORIZATION) String token, @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        Map<Booking,Session> bookingToSession = liveDataService.getAssociatedSessionsByBookingRecent(customerId, BookingType.TAKEAWAY);
        Booking booking = bookingToSession.keySet().stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);

        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Booking not found"));
        }

        Session session = bookingToSession.get(booking);
        Diner diner = liveDataService.getDiner(session.getId(),customerId);
        if(diner == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("User not associated to this session"));
        }

        int window = (int) masterDataService.getRestaurantDefault(session.getRestaurantId(),FixedDefaults.TAKEAWAY_LOCK_WINDOW).getValue();
        if(booking.getTargetTime() < System.currentTimeMillis() + (window * 60 * 1000)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("Takeaway is due too soon to cancel online. Please contact the restaurant."));
        }

        booking.setCancelled(true);
        sessionPaymentService.ensurePreAuthsAreCancelled(session);
        sessionService.updateDelete(session.getId());
        sessionService.clear(session, true, true, true, true, true);
        bookingService.upsert(booking);

        return ResponseEntity.noContent().build();
    }
}
