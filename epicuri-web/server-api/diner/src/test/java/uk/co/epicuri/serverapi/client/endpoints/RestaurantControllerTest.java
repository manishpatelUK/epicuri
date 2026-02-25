package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerRestaurantView;
import uk.co.epicuri.serverapi.common.pojo.customer.LocationAndCustomerView;
import uk.co.epicuri.serverapi.common.pojo.host.HostOpeningHoursView;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.TakeawayOfferingType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.AbsoluteBlackout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RestaurantControllerTest extends BaseIT {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        restaurantRepository.deleteAll();
        insertRestaurant(0,0,"foo", cuisine1.getId(), true);
        insertRestaurant(-1,0,"boo", cuisine2.getId(), false);
        insertRestaurant(0,-1,"loo", cuisine3.getId(), true);
        insertRestaurant(-1,-1,"too", cuisine1.getId(), false);
        insertRestaurant(1,1,"goo", cuisine2.getId(), true);
        insertRestaurant(1,1,"doo", cuisine2.getId(), false);
    }

    public void insertRestaurant(double lat, double longi, String name, String cuisineId, boolean hasTakeaway) {
        Restaurant rest = new Restaurant();
        rest.setPosition(new LatLongPair(lat,longi));
        rest.setName(name);
        rest.setCuisineId(cuisineId);
        rest.setStaffFacingId(name);
        rest.setEnabledForDiner(true);
        if(hasTakeaway) {
            rest.setTakeawayOffered(TakeawayOfferingType.DELIVERY_AND_COLLECTION);
        }
        restaurantRepository.insert(rest);
    }

    @Test
    public void testGetRestaurantsLongLats() throws Exception {
        //all
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);

        assertEquals(6, restaurantViews.length);
    }

    @Test
    public void testGetRestaurantsLongLatsOpeningHours() throws Exception {
        Restaurant restaurant = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);
        OpeningHours openingHours = getOpeningHours(BookingType.RESERVATION);
        openingHours.setRestaurantId(restaurant.getId());
        openingHoursRepository.insert(openingHours);

        //all
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);

        assertEquals(6, restaurantViews.length);
        boolean found = false;
        for(CustomerRestaurantView customerRestaurantView : restaurantViews) {
            if(customerRestaurantView.getId().equals(restaurant.getId())) {
                found = true;
                HostOpeningHoursView openingHoursView = customerRestaurantView.getOpeningHours();
                assertHoursEqual(9, 21, DayOfWeek.MONDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.TUESDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.WEDNESDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.THURSDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.FRIDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.SATURDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.SUNDAY, openingHoursView);
                assertHoursEqual(9, 21, DayOfWeek.SUNDAY, openingHoursView);
            }
        }
        assertTrue(found);
    }

    private void assertHoursEqual(int open, int close, DayOfWeek dayOfWeek, HostOpeningHoursView openingHoursView) {
        assertEquals(open, openingHoursView.getHours().get(dayOfWeek).get(0).getHourOpen());
        assertEquals(close, openingHoursView.getHours().get(dayOfWeek).get(0).getHourClose());
    }

    private OpeningHours getOpeningHours(BookingType type) {
        OpeningHours openingHours = new OpeningHours();
        AbsoluteBlackout blackout1 = new AbsoluteBlackout();
        blackout1.setStart(1498608000000L); //28th June 2017 midnight UTC
        blackout1.setEnd(1498608000000L + (1000*60*60*24));
        AbsoluteBlackout blackout2 = new AbsoluteBlackout();
        blackout2.setStart(1488326400000L); //March 1st 2017 midnight UTC
        blackout2.setEnd(1488326400000L + (1000*60*60*24));
        openingHours.getAbsoluteBlackouts().add(blackout2);
        openingHours.getAbsoluteBlackouts().add(blackout1);
        openingHours.getHours().put(DayOfWeek.MONDAY, getHours());
        openingHours.getHours().put(DayOfWeek.TUESDAY, getHours());
        openingHours.getHours().put(DayOfWeek.WEDNESDAY, getHours());
        openingHours.getHours().put(DayOfWeek.THURSDAY, getHours());
        openingHours.getHours().put(DayOfWeek.FRIDAY, getHours());
        openingHours.getHours().put(DayOfWeek.SATURDAY, getHours());
        openingHours.getHours().put(DayOfWeek.SUNDAY, getHours());
        openingHours.setRestaurantId(restaurant1.getId());
        openingHours.setBookingType(type);
        return openingHours;
    }

    private List<HourSpan> getHours() {
        List<HourSpan> list = new ArrayList<>();
        HourSpan hourSpan = new HourSpan();
        hourSpan.setHourOpen(9); //this is in UTC!
        hourSpan.setHourClose(21); //this is in UTC!
        list.add(hourSpan);
        return list;
    }

    @Test
    public void testGetRestaurantsEnabledForDiner() throws Exception {
        Restaurant restaurant = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);
        restaurant.setEnabledForDiner(false);
        restaurantRepository.save(restaurant);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);

        assertEquals(5, restaurantViews.length);
    }

    @Test
    public void testGetRestaurantsCats() throws Exception {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .queryParam("Category", cuisine1.getId())
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(2, restaurantViews.length);
        assertTrue(contains(restaurantViews, "foo"));
        assertTrue(contains(restaurantViews, "too"));

        assertTrue(restaurantViews[0].getName().equals("foo") || restaurantViews[1].getName().equals("foo"));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("Category", cuisine1.getId())
                .get("/Restaurant");

        restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(2, restaurantViews.length);
        assertTrue(contains(restaurantViews, "foo"));
        assertTrue(contains(restaurantViews, "too"));
    }

    @Test
    public void testGetRestaurantsTakeaway() throws Exception {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .queryParam("HasTakeaway", "true")
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(3, restaurantViews.length);
        assertTrue(contains(restaurantViews, "foo"));
        assertTrue(contains(restaurantViews, "loo"));
        assertTrue(contains(restaurantViews, "goo"));


        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("HasTakeaway", "true")
                .get("/Restaurant");

        restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(3, restaurantViews.length);
        assertTrue(contains(restaurantViews, "foo"));
        assertTrue(contains(restaurantViews, "loo"));
        assertTrue(contains(restaurantViews, "goo"));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("HasTakeaway", "false")
                .get("/Restaurant");

        restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(6, restaurantViews.length);
    }

    @Test
    public void testGetRestaurantsName() throws Exception {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .queryParam("Name", "oo")
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(6, restaurantViews.length);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("Name", "oo")
                .get("/Restaurant");

        restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(6, restaurantViews.length);
    }

    @Test
    public void testGetRestaurantsId() throws Exception {
        Restaurant restaurant = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("TopRightLatitude", 2)
                .queryParam("TopRightLongitude", 2)
                .queryParam("BottomLeftLatitude", -2)
                .queryParam("BottomLeftLongitude", -2)
                .queryParam("Id", restaurant.getId())
                .get("/Restaurant");

        CustomerRestaurantView[] restaurantViews = response.getBody().as(CustomerRestaurantView[].class);
        assertEquals(1, restaurantViews.length);
        assertTrue(contains(restaurantViews, "too"));
    }

    @Test
    public void testGetRestaurantId() throws Exception {
        Restaurant restaurant = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", restaurant.getId())
                .get("/Restaurant/{id}");

        CustomerRestaurantView restaurantView = response.getBody().as(CustomerRestaurantView.class);
        assertEquals(restaurant.getId(), restaurantView.getId());
    }

    @Test
    public void testGetRestaurantLocationAndBookingId() throws Exception {
        Restaurant restaurant2 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("foo")).findFirst().orElseGet(null);

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", -0.024)
                .queryParam("longitude", 0.024)
                .queryParam("restaurantId", restaurant2.getId())
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        LocationAndCustomerView locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(1, locationAndCustomerView.getNearestRestaurants().size());
        assertEquals(restaurant2.getId(), locationAndCustomerView.getNearestRestaurants().get(0).getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", -0.024)
                .queryParam("longitude", 0.024)
                .queryParam("restaurantId", "foobar")
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(0, locationAndCustomerView.getNearestRestaurants().size());
    }

    @Test
    public void testGetRestaurantLocationAndBookingName() throws Exception {
        Restaurant restaurant2 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("foo")).findFirst().orElseGet(null);

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", -0.024)
                .queryParam("longitude", 0.024)
                .queryParam("name", "oo")
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        LocationAndCustomerView locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(1, locationAndCustomerView.getNearestRestaurants().size());
        assertEquals(restaurant2.getId(), locationAndCustomerView.getNearestRestaurants().get(0).getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", -0.024)
                .queryParam("longitude", 0.024)
                .queryParam("name", "zz")
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(0, locationAndCustomerView.getNearestRestaurants().size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", -0.024)
                .queryParam("longitude", 0.024)
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(1, locationAndCustomerView.getNearestRestaurants().size());
    }

    @Test
    public void testGetRestaurantLocationNoCancelledBookings() throws Exception {
        Restaurant restaurant1 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);
        Restaurant restaurant2 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("foo")).findFirst().orElseGet(null);
        restaurant1.setIANATimezone("Europe/London");
        restaurant2.setIANATimezone("Europe/London");

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);

        setUpBookings(restaurant1, restaurant2);
        setUpOrdersOnBooking2();

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", 0)
                .queryParam("longitude", 0)
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        LocationAndCustomerView locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);

        assertEquals(booking1.getId(), locationAndCustomerView.getNextReservation().getId());

        booking1.setCancelled(true);
        booking1.setDeleted(0L);
        bookingRepository.save(booking1);
    }

    @Test
    public void testGetRestaurantLocationAndBookingDistance() throws Exception {
        Restaurant restaurant1 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);
        Restaurant restaurant2 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("foo")).findFirst().orElseGet(null);
        Restaurant restaurant3 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("goo")).findFirst().orElseGet(null);
        restaurant1.setPosition(new LatLongPair(0.02,0.02));
        restaurant2.setPosition(new LatLongPair(0.019,0.019));
        restaurant3.setPosition(new LatLongPair(0.01,0.01));

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", 0)
                .queryParam("longitude", 0)
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        LocationAndCustomerView locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class);
        assertNull(locationAndCustomerView.getNextReservation());
        assertNull(locationAndCustomerView.getNextTakeaway());
        assertEquals(3, locationAndCustomerView.getNearestRestaurants().size());

        assertEquals(restaurant3.getId(), locationAndCustomerView.getNearestRestaurants().get(0).getId());
        assertEquals(restaurant2.getId(), locationAndCustomerView.getNearestRestaurants().get(1).getId());
        assertEquals(restaurant1.getId(), locationAndCustomerView.getNearestRestaurants().get(2).getId());
    }

    @Test
    public void testGetRestaurantAndBooking() throws Exception {
        Restaurant restaurant1 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("too")).findFirst().orElseGet(null);
        Restaurant restaurant2 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("foo")).findFirst().orElseGet(null);
        Restaurant restaurant3 = masterDataService.getRestaurant().stream().filter(r -> r.getName().equals("goo")).findFirst().orElseGet(null);
        restaurant1.setIANATimezone("Europe/London");
        restaurant2.setIANATimezone("Europe/London");
        restaurant3.setIANATimezone("Europe/London");
        restaurant1.setPosition(new LatLongPair(0.02,0.02));
        restaurant2.setPosition(new LatLongPair(0.019,0.019));
        restaurant3.setPosition(new LatLongPair(0.01,0.01));

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        setUpBookings(restaurant1, restaurant2);
        setUpOrdersOnBooking2();

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("latitude", 0)
                .queryParam("longitude", 0)
                .header(Params.AUTHORIZATION, token)
                .get("/Restaurant/onLocationAndBooking");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        LocationAndCustomerView locationAndCustomerView = response.getBody().as(LocationAndCustomerView.class, ObjectMapperType.JACKSON_2);
        assertEquals(booking1.getId(), locationAndCustomerView.getNextReservation().getId());
        assertEquals(booking2.getId(), locationAndCustomerView.getNextTakeaway().getId());
    }

    private void setUpOrdersOnBooking2() {
        session1.setOriginalBooking(booking2);
        sessionRepository.save(session1);

        menuItem1.setTaxTypeId(tax1.getId());
        menuItemRepository.save(menuItem1);

        order1.setSessionId(session1.getId());
        order1.setTaxRate(tax1);
        order1.setMenuItem(menuItem1);
        order1.setPriceOverride(menuItem1.getPrice());
        order1.setItemPrice(menuItem1.getPrice());
        orderRepository.save(order1);
    }

    private void setUpBookings(Restaurant restaurant1, Restaurant restaurant2) {
        booking1.setCustomerId(customerLogin.getId());
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setBookingType(BookingType.RESERVATION);
        booking1.setTargetTime(System.currentTimeMillis() + 60000);
        booking1.setName("booking1");
        booking2.setCustomerId(customerLogin.getId());
        booking2.setRestaurantId(restaurant2.getId());
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setTargetTime(System.currentTimeMillis() + 60000);
        booking2.setName("booking2");
        booking3.setCustomerId(customerLogin.getId());
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setBookingType(BookingType.RESERVATION);
        booking3.setTargetTime(System.currentTimeMillis() + 1200000);
        booking3.setName("booking3");

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
    }

    private boolean contains(CustomerRestaurantView[] restaurantViews, String name) {
        for(CustomerRestaurantView restaurantView : restaurantViews) {
            if(restaurantView.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String getToken() {
        return getTokenForCustomer(customerLogin);
    }
}
