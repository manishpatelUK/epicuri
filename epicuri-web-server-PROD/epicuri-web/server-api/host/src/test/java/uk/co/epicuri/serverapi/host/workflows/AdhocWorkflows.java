package uk.co.epicuri.serverapi.host.workflows;

import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdhocWorkflows extends OrderingWorkflow {
    @Test
    public void testCreateAdhocAndConvertToTab() throws Exception{
        // create a seated session
        String token = getTokenForStaff(testStaff);

        WaitingPartyOrderPayload waitingPartyOrderPayload = createWaitingPartyPayload();
        Response response = createAdhoc(token, waitingPartyOrderPayload);
        SimpleSessionPayload simpleSessionPayload = response.getBody().as(SimpleSessionPayload.class);

        Session session = sessionRepository.findOne(simpleSessionPayload.getSessionId());
        assertEquals(2, session.getDiners().size());
        assertTrue(session.getDiners().get(0).isDefaultDiner());
        assertEquals(SessionType.ADHOC, session.getSessionType());

        PartyUpdateRequest partyUpdateRequest = new PartyUpdateRequest();
        partyUpdateRequest.setSessionId(session.getId());
        PartyUpdateRequest.Update update = new PartyUpdateRequest.Update();
        update.setName("New Party");
        update.setNumberOfPeople(2);
        update.setServiceId(testRestaurant.getServices().get(0).getId());
        update.setCreateSession(true);
        update.setTables(new ArrayList<>());
        partyUpdateRequest.setUpdate(update);

        Response convertResponse = convertAdhoc(token, partyUpdateRequest);
        assertEquals(HttpStatus.ACCEPTED.value(), convertResponse.getStatusCode());

        session = sessionRepository.findOne(simpleSessionPayload.getSessionId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(3, session.getDiners().size());
    }

    @Test
    public void testCreateAdhocAndDeleteBatches() throws Exception {
        String token = getTokenForStaff(testStaff);

        WaitingPartyOrderPayload waitingPartyOrderPayload = createWaitingPartyPayload();
        Response response1 = createAdhoc(token, waitingPartyOrderPayload);
        SimpleSessionPayload simpleSessionPayload = response1.getBody().as(SimpleSessionPayload.class);

        //artificially set the batches to not waiting for immediate print
        List<Batch> batches = batchRepository.findAll();
        batches.forEach(b -> b.setAwaitingImmediatePrint(false));
        batchRepository.save(batches);

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        HostBatchView[] batches1 = response2.as(HostBatchView[].class);
        assertEquals(1, batches1.length);

        given().accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Print/all");

        Response response3 = given()
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .header(Params.AUTHORIZATION, token)
                            .get("Print");

        HostBatchView[] batches2 = response3.as(HostBatchView[].class);
        assertEquals(0, batches2.length);
    }

    protected Response createAdhoc(String token, WaitingPartyOrderPayload waitingPartyPayload) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(waitingPartyPayload)
                .post("Waiting/PostWaitingWithOrder");
    }

    protected Response convertAdhoc(String token, PartyUpdateRequest partyUpdateRequest) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(partyUpdateRequest)
                .post("Session/ConvertAdHocToTab");
    }

    private WaitingPartyOrderPayload createWaitingPartyPayload() {
        WaitingPartyOrderPayload waitingPartyOrderPayload = new WaitingPartyOrderPayload();

        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setName("QuickOrder");
        waitingPartyPayload.setNumberOfPeople(0);
        waitingPartyPayload.setCreateSession(true);
        waitingPartyPayload.setAdHoc(true);
        waitingPartyPayload.setTables(new ArrayList<>());
        waitingPartyOrderPayload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = new WaitingPartyOrderPayload.OrderPayload();
        orderPayload.setQuantity(1);
        orderPayload.setMenuItemId(menuItem1.getId());
        orderPayload.setInstantiatedFromId(ActivityInstantiationConstant.WAITER.getId());
        orderPayload.setModifiers(new ArrayList<>());
        orderPayload.setNote("");
        orderPayload.setDinerId("-1");
        orderPayload.setCourseId("-1");
        waitingPartyOrderPayload.getOrder().add(orderPayload);

        return waitingPartyOrderPayload;
    }
}
