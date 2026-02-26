package uk.co.epicuri.serverapi.client.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.OnlineOrderingAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.authentication.OnlineOrderingAuthResponse;
import uk.co.epicuri.serverapi.common.pojo.booking.TimeSlots;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.comms.EmailRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.errors.IllegalStateResponseException;
import uk.co.epicuri.serverapi.service.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Collections;

@CrossOrigin
@RestController
@RequestMapping(value = "/onlineorders", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class OnlineOrderingController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineOrderingController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private CommonDataViewsConversionService commonDataViewsConversionService;

    @Autowired
    private CustomerTakeawayService customerTakeawayService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handleOptions() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/acquireToken", method = RequestMethod.POST)
    public ResponseEntity<?> acquireToken(@RequestParam("restaurantId") String restaurantId, @RequestParam("publicToken") String publicToken, HttpServletRequest request) {
        if(StringUtils.isBlank(restaurantId) || StringUtils.isBlank(publicToken)) {
            return ResponseEntity.badRequest().build();
        }

        String ipAddress1 = request.getRemoteAddr();
        String ipAddress2 = request.getHeader("X-FORWARDED-FOR");

        String token = authenticationService.onlineOrderingLogin(restaurantId, publicToken, new String[]{ipAddress1, ipAddress2});

        if(token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        String symbol = "£";
        try {
            CurrencyUnit currencyUnit = CurrencyUnit.of(restaurant.getISOCurrency());
            symbol = currencyUnit.getSymbol();
        } catch (Exception ex){
            LOGGER.warn("Cannot get currency for Restaurant {}, currency code={}", restaurantId, restaurant.getISOCurrency());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Incorrect configuration: ISO currency not found");
        }

        OnlineOrderingAuthResponse response = new OnlineOrderingAuthResponse(token);
        response.setCurrencySymbol(symbol);
        if(restaurant.getIntegrations().containsKey(ExternalIntegration.STRIPE)) {
            response.setStripePublicKey(restaurant.getIntegrations().get(ExternalIntegration.STRIPE).getToken());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stripe is not configured for this venue");
        }

        response.setMaxOrderValue((Double)restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAX_TAKEAWAY_VALUE)).findFirst().orElse(RestaurantDefault.newDefault("", 30D)).getValue());
        response.setMinOrderValue((Double)restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MIN_TAKEAWAY_VALUE)).findFirst().orElse(RestaurantDefault.newDefault("", 5D)).getValue());
        response.setMaxWithoutCC((Double)restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC)).findFirst().orElse(RestaurantDefault.newDefault("", 15D)).getValue());
        response.setTakeawayMinimumTime((Integer) restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.TAKEAWAY_MINIMUM_TIME)).findFirst().orElse(RestaurantDefault.newDefault("", 30)).getValue());
        response.setIsoCurrency(restaurant.getISOCurrency());
        response.setRestaurantName(restaurant.getName());
        response.setRestaurantImage(restaurant.getGuestLogoURL());
        response.setRestaurantPhoneNumber(restaurant.getPhoneNumber1());
        response.setAddress(restaurant.getAddress());

        return ResponseEntity.ok(response);
    }

    @OnlineOrderingAuthRequired
    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    public ResponseEntity<?> getTakeawayMenu(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        if(StringUtils.isBlank(restaurant.getTakeawayMenu())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(commonDataViewsConversionService.getMenuView(restaurantId, restaurant.getTakeawayMenu()));
    }

    @OnlineOrderingAuthRequired
    @RequestMapping(value = "/timeslots", method = RequestMethod.GET)
    public ResponseEntity<?> getTimeSlots(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setRestaurantId(restaurantId);

        TimeSlots timeSlots = new TimeSlots();

        for(String time : bookingService.getAllTakeawayTimeSlots()) {
            long utcTime = TimeUtil.toEpochMillisToday(time, restaurant.getIANATimezone());
            request.setRequestedTime(utcTime / 1000);
            if(!customerTakeawayService.checkTimesFuseBox(request)) {
                timeSlots.getTimes().add(time);
            }
        }

        return ResponseEntity.ok(timeSlots);
    }

    @OnlineOrderingAuthRequired
    @RequestMapping(value = "/takeaway", method = RequestMethod.PUT)
    public ResponseEntity<?> checkTakeaway(@NotNull @RequestBody CustomerTakeawayOrderRequest order) {
        try {
            return ResponseEntity.ok(customerTakeawayService.checkTakeaway(null, order));
        } catch (IllegalStateResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(new StringMessage(e.getResponseMessage()));
        }
    }

    @OnlineOrderingAuthRequired
    @RequestMapping(value = "/takeaway", method = RequestMethod.POST)
    public ResponseEntity<?> createTakeaway(@NotNull @RequestBody CustomerTakeawayOrderRequest order) {
        try {
            return ResponseEntity.ok(customerTakeawayService.createTakeaway(null, order));
        } catch (IllegalStateResponseException e) {
            return ResponseEntity.status(e.getStatus()).body(new StringMessage(e.getResponseMessage()));
        }
    }

    @OnlineOrderingAuthRequired
    @RequestMapping(value = "/email/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> emailDetails(@RequestHeader(Params.AUTHORIZATION) String token, @PathVariable("id") String bookingId, @NotNull @RequestBody EmailRequest emailRequest) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Booking booking = bookingService.getBooking(bookingId);
        if(booking == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Takeaway not found"));
        }

        Session session = sessionService.getByBookingId(bookingId);
        if(session == null || !session.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Takeaway not found"));
        }

        asyncCommunicationsService.sendReceiptToCustomer(session, Collections.singletonList(emailRequest));
        return ResponseEntity.ok().build();
    }
}
