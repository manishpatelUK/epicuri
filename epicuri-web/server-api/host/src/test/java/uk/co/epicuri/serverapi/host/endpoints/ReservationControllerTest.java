package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationRequest;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationView;
import uk.co.epicuri.serverapi.engines.DateTimeConstants;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by Manish Patel
 */
public class ReservationControllerTest extends BaseIT {

    private long now;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        //switch now to 12 o'clock today
        LocalDateTime localDateTime = LocalDateTime.now().toLocalDate().atStartOfDay().plusHours(12);
        now = localDateTime.toEpochSecond(ZoneOffset.UTC)*1000;
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(now + 10000);
        booking1.setBookingType(BookingType.RESERVATION);
        booking2.setRestaurantId(restaurant1.getId());
        booking2.setTargetTime(now + (23*60*60*1000));
        booking2.setBookingType(BookingType.RESERVATION);
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setTargetTime(now - (23*60*60*1000));
        booking3.setBookingType(BookingType.RESERVATION);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        restaurant1.setIANATimezone("Europe/London");
        restaurant1.setCountryId(country1.getId());
        restaurantRepository.save(restaurant1);

        country1.setAcronym("GB");
        countryRepository.save(country1);
    }

    @Test
    public void testGetReservations() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostReservationView> reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, reservationViews.size());
        testEqual(null,null,booking1,reservationViews.stream().filter(b->b.getId().equals(booking1.getId())).findFirst().orElse(null));
        testEqual(null,null,booking2,reservationViews.stream().filter(b->b.getId().equals(booking2.getId())).findFirst().orElse(null));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", now/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, reservationViews.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, reservationViews.size());
        testEqual(null,null,booking1,reservationViews.stream().filter(b->b.getId().equals(booking1.getId())).findFirst().orElse(null));
        testEqual(null,null,booking2,reservationViews.stream().filter(b->b.getId().equals(booking2.getId())).findFirst().orElse(null));
        testEqual(null,null,booking3,reservationViews.stream().filter(b->b.getId().equals(booking3.getId())).findFirst().orElse(null));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("toTime", now/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, reservationViews.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, reservationViews.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, reservationViews.size());

        booking1.setRejected(true);
        bookingRepository.save(booking1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, reservationViews.size());

        booking1.setRejected(false);
        booking1.setCancelled(true);
        bookingRepository.save(booking1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, reservationViews.size());
        assertTrue(reservationViews.stream().filter(b -> b.getId().equals(booking1.getId())).findFirst().orElse(null).isDeleted());

        booking1.setRejected(true);
        bookingRepository.save(booking1);
        booking2.setRejected(true);
        booking2.setAccepted(false);
        bookingRepository.save(booking2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .queryParam("pendingWaiterAction", true)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, reservationViews.size());

        party1.setRestaurantId(restaurant1.getId());
        party1.setBookingId(booking3.getId());
        party1.setArrivedTime(now);
        partyRepository.save(party1);
        session1.setRestaurantId(restaurant1.getId());
        session1.setOriginalBooking(booking3);
        sessionRepository.save(session1);
        booking3.setCustomerId(customer1.getId());
        bookingRepository.save(booking3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .queryParam("pendingWaiterAction", true)
                .get("Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        testEqual(session1, party1, booking3, reservationViews.get(0));
    }

    private void testEqual(Session session, Party party, Booking booking, HostReservationView view) {
        if(party == null) {
            assertNull(view.getArrivedTime());
        } else {
            assertNotNull(view.getArrivedTime());
            assertEquals(party.getArrivedTime() / 1000, view.getArrivedTime().longValue());
        }
        if(session == null) {
            assertEquals("0", view.getSessionId());
        } else {
            assertEquals(session.getId(), view.getSessionId());
        }
        testEqual(booking, view, false);
    }

    private void testEqual(Booking booking, HostReservationView view, boolean customerWasOnRequest) {
        assertEquals(booking.getId(), view.getId());
        assertEquals(booking.getName(), view.getName());
        if(customerWasOnRequest) {
            assertEquals(booking.getCustomerId(), view.getLeadCustomerId());
        }
        assertEquals(booking.getNotes(), view.getNotes());
        if(booking.getCustomerId() == null) {
            assertNull(view.getLeadCustomer());
        } else {
            if(customerWasOnRequest) {
                assertNotNull(view.getLeadCustomer());
                Customer customer = customerRepository.findOne(view.getLeadCustomerId());
                HostCustomerView hostCustomerView = view.getLeadCustomer();
                assertEquals(customer.getAddress(), hostCustomerView.getAddress());
                assertEquals(customer.getId(), hostCustomerView.getId());
                assertEquals(customer.getFavouriteDrink(), hostCustomerView.getFavouriteDrink());
                assertEquals(customer.getFavouriteFood(), hostCustomerView.getFavouriteFood());
                assertEquals(customer.getHatedFood(), hostCustomerView.getHatedFood());
                if(StringUtils.isNotBlank(customer.getFirstName())) {
                    assertEquals(customer.getFirstName(), hostCustomerView.getName().getFirstName());
                } else {
                    assertEquals("", hostCustomerView.getName().getFirstName());
                }
                if(StringUtils.isNotBlank(customer.getLastName())) {
                    assertEquals(customer.getLastName(), hostCustomerView.getName().getLastName());
                } else {
                    assertEquals("", hostCustomerView.getName().getLastName());
                }

                assertTrue(hostCustomerView.getPhoneNumber().endsWith(customer.getPhoneNumber()));
                assertEquals(customer.getAllergies().size(), hostCustomerView.getAllergies().size());
                assertEquals(customer.getDietaryRequirements().size(), hostCustomerView.getDietaryRequirements().size());
                assertEquals(customer.getFoodPreferences().size(), hostCustomerView.getFoodPreferences().size());
            }
        }
        assertEquals(restaurant1.getId(), booking.getRestaurantId());
        assertEquals(booking.getNumberOfPeople(), view.getNumberInParty());
        assertEquals(booking.getTargetTime()/1000, view.getReservationTime());
        assertEquals(booking.isAccepted(), view.isAccepted());
    }

    @Test
    public void testPutAccepted() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setAccepted(false);
        bookingRepository.save(booking1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", booking1.getId())
                .put("Reservation/Accept/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertTrue(bookingRepository.findOne(booking1.getId()).isAccepted());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("pendingWaiterAction", false)
                .queryParam("fromTime", (now-(24*60*60*1000))/1000)
                .queryParam("toTime", (now+(24*60*60*1000))/1000)
                .get("Reservation");
        List<HostReservationView> reservationViews = Arrays.asList(response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2));
        HostReservationView view = reservationViews.stream().filter(b -> b.getId().equals(booking1.getId())).findFirst().orElse(null);
        assertNotNull(view);
        assertTrue(view.isAccepted());
    }

    @Test
    public void testPutRejected() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setRejected(false);
        booking1.setCustomerId(customer1.getId());
        bookingRepository.save(booking1);

        StringMessage message = new StringMessage();
        message.setNotice("foo");

        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn1.setTime(System.currentTimeMillis());
        checkIn1.setCustomerId(customer1.getId());
        checkInRepository.save(checkIn1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", booking1.getId())
                .body(message)
                .put("Reservation/Reject/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Booking booking = bookingRepository.findOne(booking1.getId());
        assertTrue(booking.isRejected());
        assertEquals(message.getNotice(), booking.getRejectionNotice());
        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
    }

    @Test
    public void testPutArrived() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setCustomerId(customer1.getId());
        booking1.setNumberOfPeople(3);
        booking1.setTargetTime(System.currentTimeMillis());
        booking1.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
        booking1.setName("foobar");
        bookingRepository.save(booking1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", booking1.getId())
                .put("Reservation/Arrived/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Party party = partyRepository.findAll().stream().filter(p -> p.getBookingId() != null && p.getBookingId().equals(booking1.getId())).findFirst().get();
        assertTrue((System.currentTimeMillis()-party.getArrivedTime()) < 2000);
        assertEquals(booking1.getTargetTime(), party.getTime());
        assertEquals(booking1.getNumberOfPeople(), party.getNumberOfPeople());
        assertNotNull(party.getName());
        assertEquals(booking1.getCustomerId(), party.getCustomerId());
        assertEquals(PartyType.RESERVATION, party.getPartyType());
        assertEquals(booking1.getInstantiatedFrom(), party.getInstantiatedFrom());
    }

    @Test
    public void testPostReservation() throws Exception {
        String token = getTokenForStaff(staff1);

        HostReservationRequest reservationRequest = new HostReservationRequest();
        reservationRequest.setLeadCustomerId(null);
        reservationRequest.setNotes("foo");
        reservationRequest.setNumberInParty(4);
        reservationRequest.setReservationTime(System.currentTimeMillis()/1000);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(reservationRequest)
                .post("Reservation");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostReservationView hostReservationView = response.getBody().as(HostReservationView.class, ObjectMapperType.JACKSON_2);
        Booking booking = bookingRepository.findAll().stream().filter(b -> b.getNotes() != null && b.getNotes().equals(reservationRequest.getNotes())).findFirst().get();
        testEqual(booking, hostReservationView, false);

        assertEquals(restaurant1.getId(), booking.getRestaurantId());
        assertEquals(reservationRequest.getNumberInParty(), booking.getNumberOfPeople());
        assertEquals(reservationRequest.getReservationTime()*1000, booking.getTargetTime());

        reservationRequest.setLeadCustomerId(customer1.getId());
        bookingRepository.delete(booking.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(reservationRequest)
                .post("Reservation");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        booking = bookingRepository.findAll().stream().filter(b -> b.getNotes() != null && b.getNotes().equals(reservationRequest.getNotes())).findFirst().get();
        assertEquals(customer1.getId(), booking.getCustomerId());
    }

    @Test
    public void testPostReservationOmitCheck() throws Exception {
        String token = getTokenForStaff(staff1);

        HostReservationRequest reservationRequest = new HostReservationRequest();
        reservationRequest.setLeadCustomerId(null);
        reservationRequest.setNotes("foo");
        reservationRequest.setNumberInParty(4);
        reservationRequest.setReservationTime(System.currentTimeMillis()/1000);
        reservationRequest.setOmitFromChecks(true);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(reservationRequest)
                .post("Reservation");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Booking booking = bookingRepository.findAll().stream().filter(b -> b.getNotes() != null && b.getNotes().equals(reservationRequest.getNotes())).findFirst().get();
        assertTrue(booking.isOmitFromChecks());
    }

    @Test
    public void testPutReservation() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        bookingRepository.save(booking1);

        HostReservationRequest reservationRequest = new HostReservationRequest();
        reservationRequest.setLeadCustomerId(customer2.getId());
        reservationRequest.setNotes("foo");
        reservationRequest.setNumberInParty(4);
        reservationRequest.setReservationTime(System.currentTimeMillis()/1000);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(reservationRequest)
                .pathParam("id", booking1.getId())
                .put("Reservation/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Booking booking = bookingRepository.findOne(booking1.getId());
        assertEquals(customer2.getId(), booking.getCustomerId());
        assertEquals(restaurant1.getId(), booking.getRestaurantId());
        assertEquals(reservationRequest.getNumberInParty(), booking.getNumberOfPeople());
        assertEquals(reservationRequest.getReservationTime()*1000, booking.getTargetTime());
    }

    @Test
    public void testGetReservation() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setCustomerId(customer1.getId());
        booking1.setNumberOfPeople(3);
        booking1.setTargetTime(System.currentTimeMillis());
        bookingRepository.save(booking1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", booking1.getId())
                .get("Reservation/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostReservationView[] view = response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2);
        testEqual(booking1, view[0], true);

        Party party = new Party(booking1);
        party = partyRepository.insert(party);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party.getId())
                .get("Reservation/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        view = response.getBody().as(HostReservationView[].class, ObjectMapperType.JACKSON_2);
        testEqual(booking1, view[0], true);
    }

    @Test
    public void testDeleteReservation() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(System.currentTimeMillis());
        bookingRepository.save(booking1);
        checkIn1.setBookingId(booking1.getId());
        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn1.setTime(System.currentTimeMillis());
        checkInRepository.save(checkIn1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", booking1.getId())
                .delete("Reservation/{id}");
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
        assertTrue(bookingRepository.findOne(booking1.getId()).isCancelled());
    }

    @Test
    public void testPostReservationCheck() throws Exception {
        String token = getTokenForStaff(staff1);

        HostReservationRequest reservationRequest = new HostReservationRequest();
        reservationRequest.setLeadCustomerId(customer2.getId());
        reservationRequest.setNotes("foo");
        reservationRequest.setNumberInParty(4000);
        reservationRequest.setReservationTime(System.currentTimeMillis()/1000);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(reservationRequest)
                .post("Reservation/ReservationCheck");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Map view = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertNotNull(view.get("Warning"));
    }
}