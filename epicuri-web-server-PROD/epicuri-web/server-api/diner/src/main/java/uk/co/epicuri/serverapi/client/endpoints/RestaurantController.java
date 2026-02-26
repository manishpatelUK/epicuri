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
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.*;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.positioning.LongLatUtil;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Called by guest apps to get restaurant information
 */
@RestController
@RequestMapping(value = "/Restaurant", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class RestaurantController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    /**
     * Restaurant search function. All parameters are optional
     * @param trLat top right of polygon
     * @param trLong top right of polygon
     * @param blLat bottom left of polygon
     * @param blLong bottom left of polygon
     * @param cuisineId category
     * @param hasTakeaway takeaway of any type
     * @param name name ("contains" operation, not case sensitive)
     * @param restaurantId (exact restaurant id, ignores everything else)
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRestaurants(@RequestParam(value = "TopRightLatitude", required = false) Double trLat,
                                            @RequestParam(value = "TopRightLongitude", required = false) Double trLong,
                                            @RequestParam(value = "BottomLeftLatitude", required = false) Double blLat,
                                            @RequestParam(value = "BottomLeftLongitude", required = false) Double blLong,
                                            @RequestParam(value = "Category", defaultValue = "", required = false) String cuisineId,
                                            @RequestParam(value = "HasTakeaway", defaultValue = "", required = false) String hasTakeaway,
                                            @RequestParam(value = "Name", defaultValue = "", required = false) String name,
                                            @RequestParam(value = "Id", defaultValue = "", required = false) String restaurantId) {
        if(StringUtils.isNotBlank(restaurantId)) {
            Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
            if(restaurant != null && restaurant.isEnabledForDiner()) {
                return ResponseEntity.ok(Collections.singletonList(new CustomerRestaurantView(restaurant)));
            } else {
                return ResponseEntity.ok(new ArrayList<CustomerRestaurantView>());
            }
        }

        Boolean hasTakeawaySpecified = null;
        if(StringUtils.isNotBlank(hasTakeaway)) {
            hasTakeawaySpecified = Boolean.valueOf(hasTakeaway);
        }
        String nameSpecified = null;
        if(StringUtils.isNotBlank(name)) {
            nameSpecified = name;
        }
        String cuisineIdSpecified = null;
        if(StringUtils.isNotBlank(cuisineId)) {
            cuisineIdSpecified = cuisineId;
        }

        List<CustomerRestaurantView> toReturn = new ArrayList<>();
        List<Restaurant> list = masterDataService.searchRestaurants(trLat, trLong, blLat, blLong, cuisineIdSpecified, hasTakeawaySpecified, nameSpecified);
        list = list.stream().filter(Restaurant::isEnabledForDiner).collect(Collectors.toList());
        List<OpeningHours> openingHours = masterDataService.getOpeningHours(list.stream().map(Restaurant::getId).collect(Collectors.toList()), BookingType.RESERVATION);
        Map<String,OpeningHours> restaurantIdToHours = new HashMap<>();
        openingHours.forEach(o -> restaurantIdToHours.put(o.getRestaurantId(), o));

        toReturn.addAll(list.stream().map(r -> new CustomerRestaurantView(r, restaurantIdToHours.get(r.getId()))).distinct().collect(Collectors.toList()));
        Collections.sort(toReturn);
        return ResponseEntity.ok(toReturn);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/onLocationAndBooking", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRestaurants(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @RequestParam(value = "latitude") double latitude,
                                            @RequestParam(value = "longitude") double longitude,
                                            @RequestParam(value = "restaurantId", required = false) String restaurantId,
                                            @RequestParam(value = "name", required = false) String name) {
        double trLat = latitude + 0.05;
        double trLong = longitude + 0.05;
        double blLat = latitude - 0.05;
        double blLong = longitude - 0.05;

        LOGGER.trace("Query for {}, {}, {}, {}", latitude, longitude, name, restaurantId);
        List<Restaurant> list = new ArrayList<>();
        if(StringUtils.isNotBlank(restaurantId)) {
            Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
            if(restaurant != null) {
                list.add(restaurant);
            }
        } else {
            list.addAll(masterDataService.searchRestaurants(trLat, trLong, blLat, blLong, null, null, name));
        }
        LOGGER.trace("Got {} restaurants", list.size());

        list.sort((o1, o2) -> {
            if(o1.getPosition() == null || o2.getPosition() == null) {
                return Integer.MAX_VALUE;
            } else {
                return Double.compare(LongLatUtil.calculateDistanceKm(o1.getPosition().getLatitude(), o1.getPosition().getLongitude(), latitude, longitude),
                        LongLatUtil.calculateDistanceKm(o2.getPosition().getLatitude(), o2.getPosition().getLongitude(), latitude, longitude));
            }
        });

        Customer customer = customerService.getCustomer(authenticationService.getCustomerId(token));
        List<Booking> bookings = bookingService.getBookingsByCustomerId(customer.getId(), System.currentTimeMillis(), Long.MAX_VALUE, null);
        if(StringUtils.isNotBlank(restaurantId)) {
            bookings = bookings.stream().filter(b -> b.getRestaurantId().equals(restaurantId)).collect(Collectors.toList());
        }

        List<Booking> reservations = bookings.stream().filter(b -> b.getBookingType() == BookingType.RESERVATION && b.getDeleted() == null && !b.isCancelled()).collect(Collectors.toList());
        Collections.sort(reservations);
        List<Booking> takeaways = bookings.stream().filter(b -> b.getBookingType() == BookingType.TAKEAWAY && b.getDeleted() == null && !b.isCancelled()).collect(Collectors.toList());
        Collections.sort(takeaways);

        Map<String,Restaurant> idToRestaurant = masterDataService.getRestaurants(bookings.stream().filter(b -> b.getRestaurantId() != null).map(Booking::getRestaurantId).distinct().collect(Collectors.toList()))
                                                    .stream().collect(Collectors.toMap(Restaurant::getId, Function.identity()));

        LocationAndCustomerView locationAndCustomerView = new LocationAndCustomerView();
        List<OpeningHours> allOpeningHours = masterDataService.getOpeningHours(list.stream().map(Restaurant::getId).collect(Collectors.toList()), BookingType.RESERVATION);
        Map<String,OpeningHours> allOpeningHoursMap = allOpeningHours.stream().collect(Collectors.toMap(OpeningHours::getRestaurantId, Function.identity()));
        List<CustomerRestaurantView> nearestRestaurants = new ArrayList<>();
        list.forEach(r -> {
            if(allOpeningHoursMap.containsKey(r.getId())){
                nearestRestaurants.add(new CustomerRestaurantView(r, allOpeningHoursMap.get(r.getId())));
            } else {
                nearestRestaurants.add(new CustomerRestaurantView(r));
            }
        });
        locationAndCustomerView.setNearestRestaurants(nearestRestaurants);
        locationAndCustomerView.getNearestRestaurants().forEach(r -> {
            if(r.getPosition() != null) {
                r.setDistance(LongLatUtil.calculateDistanceKm(latitude, longitude, r.getPosition().getLatitude(), r.getPosition().getLongitude()));
            } else {
                r.setDistance(null);
            }
        });
        if(reservations.size() > 0) {
            Booking next = reservations.get(0);
            locationAndCustomerView.setNextReservation(new CustomerReservationView(next, idToRestaurant.get(next.getRestaurantId())));
        }
        if(takeaways.size() > 0) {
            Booking next = takeaways.get(0);
            Session session = sessionService.getByBookingId(next.getId());
            if(session != null) {
                List<Order> orders = liveDataService.getOrders(session.getId());
                Map<CalculationKey, Number> calculations = sessionCalculationService.calculateValues(session, orders);
                locationAndCustomerView.setNextTakeaway(new CustomerTakeawayResponseView(session, next, orders, idToRestaurant.get(next.getRestaurantId()), calculations, SessionCalculationService.isPaid(calculations)));
            }
        }

        return ResponseEntity.ok(locationAndCustomerView);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRestaurants(@PathVariable("id") String restaurantId) {
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Restaurant not found"));
        }

        OpeningHours openingHours = masterDataService.getOpeningHours(restaurantId, BookingType.RESERVATION);
        CustomerRestaurantView customerRestaurantView = new CustomerRestaurantView(restaurant, openingHours);
        return ResponseEntity.ok(customerRestaurantView);
    }
}
