package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.HostDinerRequest;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class DinerControllerTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpSession();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        long currentTime = System.currentTimeMillis();
        session1.setStartTime(currentTime);
        sessionRepository.save(session1);

        //checkin
        checkIn1.setTime(currentTime - 1000*60*180);
        checkInRepository.save(checkIn1);
    }

    @Ignore
    @Test
    public void testPostDiner() throws Exception {

    }

    @Test
    public void testPutDiner() throws Exception {
        String token = getTokenForStaff(staff1);

        HostDinerRequest request = new HostDinerRequest();
        // no epicuri user -> NOT_MODIFIED
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());

        assertEquals(HttpStatus.NOT_MODIFIED.value(), response.getStatusCode());

        // no checkin -> FORBIDDEN
        IdPojo id = new IdPojo();
        id.setId(customer1.getId());
        request.setEpicuriUser(id);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        checkIn1.setCustomerId(customer1.getId());
        checkInRepository.save(checkIn1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        // closed session -> BAD_REQUEST
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        // OK
        checkInRepository.deleteAll();
        session1.setClosedTime(null);
        session1.setOriginalParty(party1);
        sessionRepository.save(session1);
        checkIn1.setTime(System.currentTimeMillis() - (1000*60*2));
        checkIn1.setCustomerId(request.getEpicuriUser().getId());
        checkIn1.setPartyId(party2.getId());
        checkInRepository.save(checkIn1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        CheckIn test = checkInRepository.findOne(checkIn1.getId());
        assertNotNull(test.getSessionId());
        assertNotNull(test.getPartyId());
        assertEquals(customer1.getId(), sessionRepository.findOne(session1.getId()).getDiners().get(1).getCustomerId());
        assertNull(partyRepository.findOne(party2.getId()));
    }

    @Test
    public void testPutDinerGuestName() throws Exception {
        String token = getTokenForStaff(staff1);

        HostDinerRequest request = new HostDinerRequest();
        request.setGuestName("Mr Foo Man Chu Ski");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Diner/"+diner2.getId());
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertNull(session.getDiners().get(1).getCustomerId());
        assertEquals(request.getGuestName(), session.getDiners().get(1).getName());
    }

    @Ignore
    @Test
    public void testDeleteDiner() throws Exception {

    }

    @Test
    public void testDeleteDinerAssociation() throws Exception {
        String token = getTokenForStaff(staff1);
        String dinerId = "foobar";

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Diner/DisassociateCheckIn/"+dinerId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        dinerId = diner2.getId();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Diner/DisassociateCheckIn/"+dinerId);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        diner2.setCustomerId(customer1.getId());
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Diner/DisassociateCheckIn/"+dinerId);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(sessionRepository.findOne(session1.getId()).getDiners().get(1).getCustomerId());

        diner2.setCustomerId(customer1.getId());
        sessionRepository.save(session1);

        checkIn1.setCustomerId(customer1.getId());
        checkInRepository.save(checkIn1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Diner/DisassociateCheckIn/"+dinerId);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(sessionRepository.findOne(session1.getId()).getDiners().get(1).getCustomerId());
        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());
    }
}