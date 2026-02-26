package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.host.SessionIdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CheckInWorkflows extends WorkflowsBaseIT {
    @Override
    public void setUp() throws Exception{
        super.setUp();

        customer1.setFirstName("foo");
        customer1.setLastName("bar");
        customerRepository.save(customer1);

        CheckIn checkIn = new CheckIn();
        checkIn.setTime(System.currentTimeMillis());
        checkIn.setRestaurantId(testRestaurant.getId());
        checkIn.setCustomerId(customer1.getId());
        checkInRepository.insert(checkIn);
    }

    @Test
    public void testCheckInWorkflowTab() throws Exception {
        String token = getTokenForStaff(testStaff);

        //{"Name":"Manish Patel","NumberOfPeople":2,"LeadCustomer":{"Id":"59c0f81679414866993f2712"}}
        Response response1 = createWaitingParty(token);

        // {"Id":"59c1036c7941487f2436d7de","LeadCustomerId":"59c0f81679414866993f2712","SessionId":"0","CreateSession":false,"ServiceId":"0","IsAdHoc":false,"NumberOfPeople":2,"Name":"Manish Patel","Created":1505821548}
        PartyResponse partyResponse1 = assertPartyResponseEquals(response1);

        List<CheckIn> checkIns = liveDataService.getCheckIns(testRestaurant.getId(), 1000*60);
        assertEquals(1, checkIns.size());
        CheckIn checkIn = checkIns.get(0);
        assertEquals(partyResponse1.getId(), checkIn.getPartyId());

        // POST to /Session/FromParty/59c1036c7941487f2436d7de with {"Tables":[],"ServiceId":"58ab16bde4b0af26e64d49f1-2DROWCA8"}
        // (starting a tab)
        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setTables(new ArrayList<>());
        sessionPayload.setServiceId(testRestaurant.getServices().get(0).getId());

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(sessionPayload)
                .pathParam("id", partyResponse1.getId())
                .post("/Session/FromParty/{id}");
        assertEquals(HttpStatus.CREATED.value(), response2.getStatusCode());
        SessionIdPojo sessionIdPojo1 = response2.getBody().as(SessionIdPojo.class);

        checkIns = liveDataService.getCheckIns(testRestaurant.getId(), 1000*60);
        assertEquals(1, checkIns.size());
        assertEquals(sessionIdPojo1.getId(), checkIns.get(0).getSessionId());
        Session session = sessionRepository.findOne(sessionIdPojo1.getId());
        assertNotNull(session);
        assertEquals(SessionType.TAB, session.getSessionType());
    }

    @Test
    public void testCheckInWorkflowTable() throws Exception {
        String token = getTokenForStaff(testStaff);

        //{"Name":"Manish Patel","NumberOfPeople":2,"LeadCustomer":{"Id":"59c0f81679414866993f2712"}}
        Response response1 = createWaitingParty(token);

        // {"Id":"59c1036c7941487f2436d7de","LeadCustomerId":"59c0f81679414866993f2712","SessionId":"0","CreateSession":false,"ServiceId":"0","IsAdHoc":false,"NumberOfPeople":2,"Name":"Manish Patel","Created":1505821548}
        PartyResponse partyResponse1 = assertPartyResponseEquals(response1);

        List<CheckIn> checkIns = liveDataService.getCheckIns(testRestaurant.getId(), 1000*60);
        assertEquals(1, checkIns.size());
        CheckIn checkIn = checkIns.get(0);
        assertEquals(partyResponse1.getId(), checkIn.getPartyId());

        // POST to /Session/FromParty/59c1036c7941487f2436d7de with {"Tables":["tableId"],"ServiceId":"58ab16bde4b0af26e64d49f1-2DROWCA8"}
        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setTables(Collections.singletonList(testRestaurant.getTables().get(0).getId()));
        sessionPayload.setServiceId(testRestaurant.getServices().get(0).getId());

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(sessionPayload)
                .pathParam("id", partyResponse1.getId())
                .post("/Session/FromParty/{id}");
        assertEquals(HttpStatus.CREATED.value(), response2.getStatusCode());
        SessionIdPojo sessionIdPojo1 = response2.getBody().as(SessionIdPojo.class);

        checkIns = liveDataService.getCheckIns(testRestaurant.getId(), 1000*60);
        assertEquals(1, checkIns.size());
        assertEquals(sessionIdPojo1.getId(), checkIns.get(0).getSessionId());
        Session session = sessionRepository.findOne(sessionIdPojo1.getId());
        assertNotNull(session);
        assertEquals(SessionType.SEATED, session.getSessionType());
    }

    public PartyResponse assertPartyResponseEquals(Response response1) {
        PartyResponse partyResponse1 = response1.getBody().as(PartyResponse.class);
        assertEquals("0", partyResponse1.getSessionId());
        assertEquals("0", partyResponse1.getServiceId());
        Party party = partyRepository.findOne(partyResponse1.getId());
        assertEquals(customer1.getId(), party.getCustomerId());
        return partyResponse1;
    }

    public Response createWaitingParty(String token) {
        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setName(customer1.getFirstName() + " " + customer1.getLastName());
        waitingPartyPayload.setNumberOfPeople(2);
        HostCustomerView customer = new HostCustomerView();
        customer.setId(customer1.getId());
        waitingPartyPayload.setCustomer(customer);

        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(waitingPartyPayload)
                .post("Waiting");
    }
}
