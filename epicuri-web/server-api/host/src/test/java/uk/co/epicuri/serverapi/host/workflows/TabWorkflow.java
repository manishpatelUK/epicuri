package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.SessionIdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by manish on 26/07/2017.
 */
public class TabWorkflow extends OrderingWorkflow {

    @Test
    public void testCreateTabAndOrders() throws Exception{
        // create a waiting party
        String token = getTokenForStaff(testStaff);

        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setNumberOfPeople(20);

        Response response1 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(waitingPartyPayload)
                .post("Waiting");

        PartyResponse partyResponse = response1.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        assertEquals("0",partyResponse.getSessionId());
        assertNotNull(partyRepository.findOne(partyResponse.getId()));

        Service serviceToUse = testRestaurant.getServices().get(0);
        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setServiceId(serviceToUse.getId());

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", partyResponse.getId())
                .body(sessionPayload)
                .post("Session/FromParty/{id}");

        SessionIdPojo sessionIdPojo = response2.getBody().as(SessionIdPojo.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(sessionIdPojo.getId());
        assertEquals(partyResponse.getId(), session.getOriginalPartyId());

        //add some orders
        createOrder(token, session, course1, menuItem1);

        Response response4 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        HostBatchView[] hostBatchViews = response4.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(1, batchRepository.findBySessionId(session.getId()).size());
        assertEquals(0, hostBatchViews.length);

        unSpoolAllBatches();
        Response response5 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        hostBatchViews = response5.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(1, hostBatchViews.length);
    }

    private void unSpoolAllBatches() {
        //simulate old batches
        List<Batch> dbBatches = batchRepository.findAll();
        dbBatches.forEach(b -> {
            b.getSpoolTime().clear();
            b.getSpoolTime().add(System.currentTimeMillis() - (1000 * 60 * 5));
            b.setAwaitingImmediatePrint(false);
        } );
        batchRepository.save(dbBatches);
    }
}
