package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionCalculationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/takeaway", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class TakeawayController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    protected SessionCalculationService sessionCalculationService;

    @Autowired
    private LiveDataService liveDataService;

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getTakeaways(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @RequestParam(value = "history", required = false) String history,
                                          @RequestParam(value = "restaurantId", required = false) String restaurantId) {
        String customerId = authenticationService.getCustomerId(token);
        Map<Booking,Session> bookingToSession = null;

        if(history == null || !history.equals("all")) {
            bookingToSession = liveDataService.getAssociatedSessionsByBookingRecent(customerId, BookingType.TAKEAWAY);
        } else {
            bookingToSession = liveDataService.getAssociatedSessionsByBookingAll(customerId, BookingType.TAKEAWAY);
        }

        List<CustomerTakeawayResponseView> list = new ArrayList<>();
        for(Map.Entry<Booking,Session> entry : bookingToSession.entrySet()) {
            Session session = entry.getValue();
            if(restaurantId != null && !session.getRestaurantId().equals(restaurantId)) {
                continue;
            }

            CustomerTakeawayResponseView customerTakeawayResponseView = getCustomerTakeawayResponseView(entry.getKey(), session);
            list.add(customerTakeawayResponseView);
        }

        return ResponseEntity.ok(list);
    }

    public CustomerTakeawayResponseView getCustomerTakeawayResponseView(Booking booking, Session session) {
        List<Order> orders = liveDataService.getOrders(session.getId());
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session, orders);
        return new CustomerTakeawayResponseView(session, booking, orders, restaurant, values, SessionCalculationService.isPaid(values));
    }
}
