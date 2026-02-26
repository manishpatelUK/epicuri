package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCheckInView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Diner;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 14.7.16..
 */
public class CheckInControllerTest extends BaseIT{

    private long currentTime;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        session1.setStartTime(System.currentTimeMillis() - (1000*60*60));
        session1.setRestaurantId(restaurant1.getId());
        session1 = sessionRepository.save(session1);

        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn2.setRestaurantId(restaurant1.getId());
        checkIn3.setRestaurantId(restaurant1.getId());

        checkIn1.setCustomerId(customer1.getId());
        checkIn2.setCustomerId(customer2.getId());
        checkIn3.setCustomerId(customer3.getId());

        currentTime = System.currentTimeMillis();

        checkIn1.setSessionId(session1.getId());
        checkIn2.setSessionId(session1.getId());
        checkIn3.setSessionId(session1.getId());

        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);
    }

    @Test
    public void postCheckIn() throws Exception {
        Response response;

        String token = "";

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();

        //UNAUTHORIZED if the user(customer) is not logged in
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForCustomer(customerLogin);

        //OK if the user(customer) is logged in but restaurant is not found
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        //OK if the user(customer) is logged in and its new checkin
        customerCheckInView.setRestaurantId(restaurant1.getId());
        customerCheckInView.setReservationId(booking1.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        CustomerCheckInView customerCheckInView1 = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);
        assertEquals(restaurant1.getId(), customerCheckInView1.getRestaurantId());
        testEquals(customerCheckInView1, checkInRepository.findOne(customerCheckInView1.getId()));
    }

    @Test
    public void postCheckInMultipleGuests() throws Exception {
        String token = getTokenForCustomer(customerLogin);
        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());
        customerCheckInView.setReservationId(booking1.getId());
        customerCheckInView.setNumberOfPeople(5);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");

        CustomerCheckInView checkInView = response.as(CustomerCheckInView.class);
        CheckIn checkIn = checkInRepository.findOne(checkInView.getId());
        Party party = partyRepository.findOne(checkIn.getPartyId());
        assertEquals(5, party.getNumberOfPeople());
    }

    @Test
    public void postCheckInWhenPreviousExpired() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        checkIn1.setTime(0L);
        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setSessionId(null);
        checkInRepository.save(checkIn1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        CustomerCheckInView responseView = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);

        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
        assertNotEquals(checkIn1.getId(), responseView.getId());
        testEquals(responseView, checkInRepository.findOne(responseView.getId()));
    }

    @Test
    public void postCheckInWhenPreviousSessionValid() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        checkIn1.setTime(0L);
        checkIn1.setCustomerId(customerLogin.getId());
        checkInRepository.save(checkIn1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CustomerCheckInView responseView = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);

        assertNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
        assertEquals(checkIn1.getId(), responseView.getId());
        testEquals(responseView, checkInRepository.findOne(responseView.getId()));
    }

    @Test
    public void postCheckInWhenPreviousPartyValid() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        checkIn1.setTime(0L);
        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setSessionId(null);
        checkIn1.setPartyId(party1.getId());
        checkInRepository.save(checkIn1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CustomerCheckInView responseView = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);

        assertNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
        assertEquals(checkIn1.getId(), responseView.getId());
        testEquals(responseView, checkInRepository.findOne(responseView.getId()));
    }

    @Test
    public void postCheckInWhenPreviousSessionClosed() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        checkIn1.setTime(0L);
        checkIn1.setCustomerId(customerLogin.getId());
        checkInRepository.save(checkIn1);

        session1.setClosedTime(0L);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        CustomerCheckInView responseView = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);

        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
        assertNotEquals(checkIn1.getId(), responseView.getId());
        testEquals(responseView, checkInRepository.findOne(responseView.getId()));
    }

    @Test
    public void postCheckInWithBookingIdTooSoon() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        setUpBooking(1000 * 60);

        customerCheckInView.setReservationId(booking1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void postCheckInWithBookingIdWithinRange() throws Exception {
        String token = getTokenForCustomer(customerLogin);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());

        setUpBooking(-1000 * 60);

        customerCheckInView.setReservationId(booking1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        CustomerCheckInView view = response.getBody().as(CustomerCheckInView.class);
        CheckIn checkIn = checkInRepository.findOne(view.getId());
        assertNotNull(checkIn.getPartyId());
    }

    public void setUpBooking(long bookingTimeAddition) {
        RestaurantDefault minTime = restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.RESERVATION_MINIMUM_TIME)).findFirst().orElse(null);
        long value = ((Number) minTime.getValue()).intValue() * 60 * 1000;

        long now = System.currentTimeMillis();
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setCustomerId(customerLogin.getId());
        booking1.setTargetTime(now + value + bookingTimeAddition);
        bookingRepository.save(booking1);
    }

    private void testEquals(CustomerCheckInView view, CheckIn checkIn) {
        assertEquals(view.getId(), checkIn.getId());
        assertEquals(view.getRestaurantId(), checkIn.getRestaurantId());
        assertEquals(view.getTime(), checkIn.getTime()/1000);
        assertEquals(view.getReservationId(), checkIn.getBookingId());
    }

    @Test
    public void getCheckIn() throws Exception {
        Response response;

        String token = "";

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();

        //UNAUTHORIZED if the user(customer) is not logged in
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForCustomer(customerLogin);

        //NOT_FOUND if the customer is not the same as the token written
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setTime(currentTime);
        checkIn2.setTime(currentTime);
        checkIn3.setTime(currentTime);
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        //OK if everything is set
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        CustomerCheckInView entity = response.getBody().as(CustomerCheckInView.class, ObjectMapperType.JACKSON_2);
        assertNotNull(entity);
        assertEquals(checkIn1.getId(), entity.getId());
        assertEquals(currentTime/1000, entity.getTime());
        assertEquals(session1.getId(), entity.getSessionId());

        long expiration = 1000 * 60 * (Integer)masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.WALKIN_EXPIRATION_TIME).getValue();
        checkIn1.setTime(currentTime-expiration-1);
        checkIn1.setSessionId(null);
        checkInRepository.save(checkIn1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void getCheckInWhenIdPresent() throws Exception {
        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setTime(0);
        checkIn1.setSessionId(null);
        checkIn1 = checkInRepository.save(checkIn1);

        String token = getTokenForCustomer(customerLogin);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        checkInRepository.deleteAll();
        checkIn2.setSessionId("foo");
        checkIn2.setPartyId("foo");
        checkIn2.setTime(0);
        checkIn2.setCustomerId(customerLogin.getId());
        checkIn2 = checkInRepository.save(checkIn2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Checkin/"+checkIn2.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

    }

    @Test
    public void deleteCheckInWalkIn() throws Exception {
        deleteCheckIn(true);
    }

    @Test
    public void deleteCheckInAttachedToSession() throws Exception {
        deleteCheckIn(false);
    }

    private void deleteCheckIn(boolean nullifySession) {
        Response response;
        String token = "";

        //UNAUTHORIZED if the user(customer) is not logged in
        response = given()
                .header(Params.AUTHORIZATION, token)
                .delete("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForCustomer(customerLogin);

        //NOT_FOUND if the customer is not the same as the token written
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setPartyId(party1.getId());
        if(nullifySession) {
            checkIn1.setSessionId(null);
        }
        checkIn1 = checkInRepository.save(checkIn1);

        //Check the number of checkins in db
        assertEquals(checkInRepository.count(), 3);

        //OK if everything is set
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        //Check if the given checkin is really deleted
        List<CheckIn> byCustomerIdAndDeletedNull = checkInRepository.findByCustomerIdAndDeletedNull(customerLogin.getId());
        assertNull(byCustomerIdAndDeletedNull.stream().filter(c -> c.getId().equals(checkIn1.getId())).findFirst().orElse(null));
        //Check again the number of checkins in db
        assertEquals(checkInRepository.count(), 3);
        if(nullifySession) {
            assertNull(partyRepository.findOne(party1.getId()));
        } else {
            assertNotNull(partyRepository.findOne(party1.getId()));
            assertNotNull(sessionRepository.findOne(checkIn1.getSessionId()));
        }
    }

    @Test
    public void deleteCheckInRemovesOnSession() throws Exception {
        checkIn1.setCustomerId(customerLogin.getId());
        checkIn1.setSessionId(session1.getId());
        checkIn1 = checkInRepository.save(checkIn1);

        Diner diner = new Diner(session1);
        diner.setCustomer(customerLogin);
        diner.setCustomerId(customerLogin.getId());
        session1.getDiners().add(diner);
        sessionRepository.save(session1);

        String token = getTokenForCustomer(customerLogin);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("/Checkin/"+checkIn1.getId());

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        for(Diner d : session.getDiners()) {
            assertNull(d.getCustomerId());
            assertNull(d.getCustomer());
        }
    }
}