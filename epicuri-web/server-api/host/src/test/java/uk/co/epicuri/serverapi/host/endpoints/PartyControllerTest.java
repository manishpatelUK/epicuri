package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostPartyChangeRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class PartyControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        party1.setRestaurantId(restaurant1.getId());
        party2.setRestaurantId(restaurant1.getId());
        party3.setRestaurantId(restaurant1.getId());

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(System.currentTimeMillis());
        bookingRepository.save(booking1);

        session1.setRestaurantId(restaurant1.getId());
        party1.setArrivedTime(System.currentTimeMillis()-100);
        party1.setPartyType(PartyType.WALK_IN);
        partyRepository.save(party1);
        session1.setOriginalParty(party1);

        session2.setRestaurantId(restaurant1.getId());
        party2.setArrivedTime(System.currentTimeMillis()-100);
        party2.setPartyType(PartyType.RESERVATION);
        party2.setBookingId(booking1.getId());
        partyRepository.save(party2);
        session2.setOriginalParty(party2);
        session2.setOriginalBooking(booking1);

        session3.setRestaurantId(restaurant1.getId());
        party3.setArrivedTime(System.currentTimeMillis()-(48*60*1000));
        partyRepository.save(party3);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
    }

    @Test
    public void testGetParties() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostPartyView> parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, parties.size());
        testEqual(party1, session1, null, null, parties.stream().filter(p -> p.getId().equals(party1.getId())).findFirst().orElse(null));
        testEqual(party2, session2, null, booking1, parties.stream().filter(p -> p.getId().equals(party2.getId())).findFirst().orElse(null));

        booking1.setCustomerId(customer1.getId());
        party2.setCustomerId(customer1.getId());
        bookingRepository.save(booking1);
        partyRepository.save(party2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, parties.size());
        testEqual(party1, session1, null, null, parties.stream().filter(p -> p.getId().equals(party1.getId())).findFirst().orElse(null));
        testEqual(party2, session2, customer1, booking1, parties.stream().filter(p -> p.getId().equals(party2.getId())).findFirst().orElse(null));

        party1.setCustomerId(customer1.getId());
        partyRepository.save(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, parties.size());
        testEqual(party1, session1, null, null, parties.stream().filter(p -> p.getId().equals(party1.getId())).findFirst().orElse(null));
        testEqual(party2, session2, customer1, booking1, parties.stream().filter(p -> p.getId().equals(party2.getId())).findFirst().orElse(null));

        //make sure wait timeout is ignored for tabs
        session3.setSessionType(SessionType.TAB);
        session3.setOriginalParty(party3);
        sessionRepository.save(session3);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(3, parties.size());
        testEqual(party1, session1, null, null, parties.stream().filter(p -> p.getId().equals(party1.getId())).findFirst().orElse(null));
        testEqual(party2, session2, customer1, booking1, parties.stream().filter(p -> p.getId().equals(party2.getId())).findFirst().orElse(null));
        testEqual(party3, session3, null, null, parties.stream().filter(p -> p.getId().equals(party3.getId())).findFirst().orElse(null));

        session3.setSessionType(SessionType.SEATED);
        sessionRepository.save(session3);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, parties.size());

        session2.setClosedTime(1L);
        sessionRepository.save(session2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Party");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        parties = Arrays.asList(response.getBody().as(HostPartyView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(1, parties.size());
    }

    @Test
    public void testPutPartyNegatives() throws Exception {
        String token = getTokenForStaff(staff1);
        session1.setOriginalParty(null);
        sessionRepository.save(session1);

        HostPartyChangeRequest hostPartyChangeRequest = new HostPartyChangeRequest();
        hostPartyChangeRequest.setNumberOfDiners(-1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostPartyChangeRequest)
                .pathParam("id", party1.getId())
                .put("Party/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        session1.setRestaurantId(party1.getRestaurantId());
        session1.setOriginalParty(party1);
        sessionRepository.save(session1);

        hostPartyChangeRequest.setNumberOfDiners(1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostPartyChangeRequest)
                .pathParam("id", party1.getId())
                .put("Party/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPutParty() throws Exception {
        String token = getTokenForStaff(staff1);
        session1.setOriginalParty(null);
        sessionRepository.save(session1);

        HostPartyChangeRequest hostPartyChangeRequest = new HostPartyChangeRequest();
        hostPartyChangeRequest.setNumberOfDiners(10);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostPartyChangeRequest)
                .pathParam("id", party1.getId())
                .put("Party/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Party party = partyRepository.findOne(party1.getId());
        assertEquals(10, party.getNumberOfPeople());
        assertEquals(party1.getName(), party.getName());

        hostPartyChangeRequest.setNumberOfDiners(8);
        hostPartyChangeRequest.setName("moonar");

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostPartyChangeRequest)
                .pathParam("id", party1.getId())
                .put("Party/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        party = partyRepository.findOne(party1.getId());
        assertEquals(8, party.getNumberOfPeople());
        assertEquals("moonar", party.getName());
    }

    public void testEqual(Party party, Session associatedSession, Customer associatedCustomer, Booking associatedBooking, HostPartyView view) {
        assertEquals(party.getId(), view.getId());
        assertEquals(party.getName(), view.getName());
        if(associatedSession != null) {
            assertEquals(associatedSession.getId(), view.getSessionId());
        } else {
            assertEquals("0", view.getSessionId());
        }

        if(associatedCustomer != null) {
            assertEquals(party.getCustomerId(), view.getLeadCustomer().getId());
        }
        assertEquals(party.getNumberOfPeople(), view.getNumberInParty());
        if(associatedBooking != null) {
            assertEquals(associatedBooking.getTargetTime()/1000, view.getReservationTime().longValue());
            assertEquals(party.getArrivedTime()/1000, view.getArrivedTime().longValue());
        } else {
            assertNull(view.getReservationTime());
            assertNull(view.getArrivedTime());
        }
    }
}