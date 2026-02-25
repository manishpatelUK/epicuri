package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostSessionView;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.SessionServiceTest;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class TakeawayControllerTest extends SessionSetupBaseIT {

    private long now;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        restaurant1.setCountryId(country1.getId());
        restaurantRepository.save(restaurant1);
        country1.setAcronym("GB");
        countryRepository.save(country1);
    }

    private void setUpTakeaways() {
        setUpMixedSessions();
        now = System.currentTimeMillis();
        session1.setStartTime(now + 10000);
        session2.setStartTime(now + (23*60*60*1000));
        session3.setStartTime(now - (23*60*60*1000));
        session3.setSessionType(SessionType.TAKEAWAY);
        session3.setRestaurantId(restaurant1.getId());
        session1.setTakeawayType(TakeawayType.COLLECTION);
        session2.setTakeawayType(TakeawayType.COLLECTION);
        session2.setCalculatedDeliveryCost(null);
        session3.setTakeawayType(TakeawayType.DELIVERY);
        session3.setCalculatedDeliveryCost(0);
        session3.getDiners().remove(1);
        session3.getDiners().get(0).setDefaultDiner(false);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        booking1.setTargetTime(session1.getStartTime());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTakeawayType(TakeawayType.COLLECTION);
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setNotes(session1.getId());
        booking2.setTargetTime(session2.getStartTime());
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setRestaurantId(restaurant1.getId());
        booking2.setNotes(session2.getId());
        booking2.setTakeawayType(TakeawayType.COLLECTION);
        booking3.setTargetTime(session3.getStartTime());
        booking3.setBookingType(BookingType.TAKEAWAY);
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setNotes(session3.getId());
        booking3.setTakeawayType(TakeawayType.DELIVERY);
        Address deliveryAddress = new Address();
        deliveryAddress.setCity("leicester");
        deliveryAddress.setPostcode("le7 9ud");
        deliveryAddress.setStreet("17 pulford drive");
        deliveryAddress.setTown("leicester");
        booking3.setDeliveryAddress(deliveryAddress);
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
    }

    @Test
    public void testGetTakeaways() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        //test regex on path
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (session3.getStartTime()-1000)/1000)
                .queryParam("toTime", (session2.getStartTime()+1000)/1000)
                .get("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostSessionView> list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, list.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (session3.getStartTime()-1000)/1000)
                .queryParam("toTime", (session2.getStartTime()+1000)/1000)
                .get("TakeAway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, list.size());

        for(HostSessionView hostSessionView : list) {
            //bit of a hack - using message/notes!
            Session session = sessionRepository.findOne(hostSessionView.getMessage());
            Booking booking = session.getOriginalBooking();

            SessionServiceTest.assertEqual(hostSessionView, session, booking);
        }

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", now/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, list.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, list.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("toTime", now/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, list.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, list.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, list.size());

        booking1.setRejected(true);
        bookingRepository.save(booking1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, list.size());
    }

    @Test
    public void testGetTakeawaysPendingWaiterAction() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        booking1.setAccepted(true);
        booking3.setAccepted(true);
        booking2.setRejected(false);
        booking2.setAccepted(false);
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .queryParam("pendingWaiterAction", true)
                .get("TakeAway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostSessionView> list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, list.size());
    }

    @Test
    public void testPostTakeaway() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        TakeawayPayload request = new TakeawayPayload();
        request.setRequestedTime((System.currentTimeMillis() - 1000*60*6)/1000);

        //test regex on path
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        request.setRestaurantId(restaurant1.getId());
        request.setDelivery(false);
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000*60*24))/1000);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostSessionView hostSessionView = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(hostSessionView.getId());
        Booking booking = session.getOriginalBooking();

        SessionServiceTest.assertEqual(hostSessionView, session, booking);

        request.setLeadCustomerId(customer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        hostSessionView = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        session = sessionRepository.findOne(hostSessionView.getId());
        booking = session.getOriginalBooking();
        assertEquals(customer1.getId(), booking.getCustomerId());
        assertEquals(customer1.getId(), hostSessionView.getCustomer().getCustomer().getId());

        request.setAddress(booking3.getDeliveryAddress());
        request.setDelivery(true);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        hostSessionView = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        session = sessionRepository.findOne(hostSessionView.getId());
        booking = session.getOriginalBooking();
        assertEquals(booking3.getDeliveryAddress(), booking.getDeliveryAddress());
        assertEquals(booking3.getDeliveryAddress(), hostSessionView.getDeliveryAddress());
        assertTrue(booking.isAccepted());
        assertFalse(booking.isRejected());
    }

    @Test
    public void testPutTakeawayCheck() throws Exception {
        String token = getTokenForStaff(staff1);

        TakeawayPayload request = new TakeawayPayload();
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000*60*24))/1000);
        request.setDelivery(true);
        request.setAddress(booking3.getDeliveryAddress());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Map map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertTrue(map.containsKey("Warning"));

        request.setLeadCustomerId(customer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertTrue(map.containsKey("Warning"));


        request.setRequestedTime(now + 10000);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertNotNull(map.get("Warning"));
    }

    @Test
    public void testPutTakeawayCheckDeliveryRadius() throws Exception {
        String token = getTokenForStaff(staff1);

        TakeawayPayload request = new TakeawayPayload();
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000 * 60 * 24)) / 1000);
        request.setDelivery(true);
        Address address = new Address();
        address.setPostcode("LE7 9UD");
        request.setAddress(address);

        Restaurant restaurant = restaurantRepository.findOne(staff1.getRestaurantId());
        Address rAddress = new Address();
        rAddress.setPostcode("HA6 1AU");
        restaurant.setAddress(rAddress);
        //restaurant.setPosition(new LatLongPair(51.609520, -0.411678));
        RestaurantDefault deliveryRadius = restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.MAX_DELIVERY_RADIUS)).findFirst().orElse(null);
        deliveryRadius.setValue(10);
        RestaurantDefault deliveryCost = restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.DELIVERY_SURCHARGE)).findFirst().orElse(null);
        deliveryCost.setValue(10);
        restaurantRepository.save(restaurant);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        @SuppressWarnings("unchecked") Map<String,Object> warnings = (Map<String,Object>)response.as(Map.class, ObjectMapperType.JACKSON_2);
        assertEquals(10D, (Double)warnings.get("Cost"), 0.01);

        address.setPostcode("HA6 1AU");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        //noinspection unchecked
        warnings = (Map<String,Object>)response.as(Map.class, ObjectMapperType.JACKSON_2);
        assertFalse(warnings.containsKey("Cost"));
    }

    @Test
    public void testPutTakeawayCheckDeliveryDistance() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpTakeaways();

        Address address = new Address();
        address.setCity("leicester");
        address.setPostcode("le7 9ud");
        address.setStreet("18 pulford drive");
        address.setTown("leicester");
        restaurant1.setAddress(address);
        restaurantRepository.save(restaurant1);

        TakeawayPayload request = new TakeawayPayload();
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000 * 60 * 60 * 24)) / 1000);
        request.setDelivery(true);
        request.setAddress(booking3.getDeliveryAddress());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Map map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertEquals(0, ((List)map.get("Warning")).size());
    }

    @Test
    public void testPutTakeawayCheckTakeawayLockWindow() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpTakeaways();

        TakeawayPayload request = new TakeawayPayload();
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000 * 60 )) / 1000);
        request.setDelivery(false);
        request.setAddress(booking3.getDeliveryAddress());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Map map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertEquals(1, ((List)map.get("Warning")).size());
    }

    @Test
    public void testPutTakeaway() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        TakeawayPayload request = new TakeawayPayload();
        request.setRestaurantId(restaurant1.getId());
        request.setDelivery(false);
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000*60*24))/1000);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostSessionView hostSessionView = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);

        //change stuff
        request.setExpectedTime((now + (1000*60*24*2))/1000);
        request.setDelivery(true);
        request.setName("Fooman Chu2");
        request.setTelephone("123542342");
        request.setMessage("a message2");
        request.setAddress(booking3.getDeliveryAddress());
        request.setLeadCustomerId(customer1.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .pathParam("id", hostSessionView.getId())
                .put("Takeaway/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(hostSessionView.getId());
        Booking booking = session.getOriginalBooking();

        assertEquals(request.getName(), session.getName());
        assertEquals(request.getName(), booking.getName());
        assertEquals(request.getTelephone(), booking.getTelephone());
        assertEquals(BookingType.TAKEAWAY, booking.getBookingType());
        assertEquals(TakeawayType.DELIVERY, booking.getTakeawayType());
        assertEquals(SessionType.TAKEAWAY, session.getSessionType());
        assertEquals(TakeawayType.DELIVERY, session.getTakeawayType());
        assertEquals(request.getMessage(), booking.getNotes());
        assertEquals(customer1.getId(), booking.getCustomerId());
        assertEquals(request.getAddress(), booking.getDeliveryAddress());
        assertEquals(request.getExpectedTime()*1000,booking.getTargetTime());

        HostSessionView hostSessionView2 = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        assertNotNull(hostSessionView2);
        assertEquals(request.getExpectedTime(), hostSessionView2.getStartTime());
        assertEquals(request.getName(), hostSessionView2.getName());
        assertEquals(request.getMessage(), hostSessionView2.getMessage());
        assertEquals(request.getTelephone(), hostSessionView2.getTelephone());
        assertEquals(request.getAddress(), hostSessionView2.getDeliveryAddress());
        assertEquals(request.getLeadCustomerId(), hostSessionView2.getCustomer().getCustomer().getId());
    }

    @Test
    public void testDeleteTakeaway1() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id",session1.getId())
                .delete("Takeaway/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getDeleted());
        Booking booking = bookingRepository.findOne(booking1.getId());
        assertTrue(booking.isCancelled());
    }

    @Test
    public void testDeleteTakeaway2() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpTakeaways();

        order1.setSessionId(session1.getId());
        orderRepository.save(order1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id",session1.getId())
                .delete("Takeaway/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNotNull(sessionArchiveRepository.findBySessionId(session1.getId()));
        assertEquals(0, orderRepository.findBySessionId(session1.getId()).size());
    }

    @Test
    public void testDeletTakeawayCancelsPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        setUpTakeaways();

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.setValue(10000);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, new ChargeSummary());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id",session1.getId())
                .delete("Takeaway/{id}");
        verify(stripeService);

        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().get(0).isVoided());
    }
}