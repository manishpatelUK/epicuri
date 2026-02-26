package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.host.HostDinerView;
import uk.co.epicuri.serverapi.common.pojo.host.HostEventView;
import uk.co.epicuri.serverapi.common.pojo.host.HostNotificationView;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SeatedSessionWorkflows extends OrderingWorkflow {

    @Test
    public void testSeatPartyDirectlyToTable() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);

        Table tableToSeat = testRestaurant.getTables().get(0);
        Service serviceToUse = testRestaurant.getServices().get(0);

        // POST /Waiting with {"Name":<table name>,"NumberOfPeople":2,"ServiceId":<service id>,"CreateSession":true,"Tables":[<table id>]}
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload(tableToSeat, serviceToUse);
        Response response1 = createSeatedParty(token, waitingPartyPayload);

        PartyResponse partyResponse = response1.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);

        Session session = sessionRepository.findOne(partyResponse.getSessionId());
        Party party = partyRepository.findOne(partyResponse.getId());
        assertNotNull(session);
        assertEquals(party.getId(), session.getOriginalPartyId());
        assertEquals(waitingPartyPayload.getName(), party.getName());
        assertEquals(SessionType.SEATED, session.getSessionType());
        assertEquals(waitingPartyPayload.getNumberOfPeople(), session.getDiners().size()-1);
        assertEquals(waitingPartyPayload.getNumberOfPeople(), party.getNumberOfPeople());
        assertEquals(testRestaurant.getServices().get(0).getId(), session.getService().getId());
        assertEquals(testRestaurant.getServices().get(0).getId(), partyResponse.getServiceId());
        assertTrue(partyResponse.isCreateSession());
        assertEquals(tableToSeat.getId(), partyResponse.getTables().get(0));

        // call GET /Session/{id}, ensure response
        // {"Diners":[{"Id":7305,"IsTable":true,"Orders":[],"SessionId":3969,"IsBirthday":false,"IsTakeawaySession":false,"EpicuriUser":null,"SubTotal":0.0,"SharedTotal":0.0,"InteractionTime":"0001-01-01T00:00:00"},{"Id":7306,"IsTable":false,"Orders":[],"SessionId":3969,"IsBirthday":false,"IsTakeawaySession":false,"EpicuriUser":null,"SubTotal":0.0,"SharedTotal":0.0,"InteractionTime":"0001-01-01T00:00:00"},{"Id":7307,"IsTable":false,"Orders":[],"SessionId":3969,"IsBirthday":false,"IsTakeawaySession":false,"EpicuriUser":null,"SubTotal":0.0,"SharedTotal":0.0,"InteractionTime":"0001-01-01T00:00:00"}],"Tables":[{"Id":97,"Name":"17","Position":null,"DefaultCovers":1,"Shape":0}],"ChairData":"","Delay":0,"IsAdHoc":false,"Id":3969,"SessionType":"Seated","StartTime":1481990754.0,"ClosedTime":0.0,"ServiceName":"Default Service","MenuId":26,"PartyName":"17","Void":false,"ScheduleItems":[{"Delay":300,"Order":0,"Notifications":[{"Id":1,"Text":"Take order","Target":"waiter/action","Acknowledgements":[]}],"ServiceId":6,"Id":13},{"Delay":5100,"Order":0,"Notifications":[{"Id":2,"Text":"Complete session","Target":"waiter/action","Acknowledgements":[]}],"ServiceId":6,"Id":14}],"RecurringScheduleItems":[],"AdhocNotifications":[],"Orders":[],"Adjustments":[],"RealAdjustments":[],"RealPayments":[],"VoidReason":null,"NumberOfDiners":2,"SubTotal":0.0,"PaymentTotal":0.0,"DiscountTotal":0.0,"RemainingTotal":0.0,"Tips":0.0,"TipTotal":12.5,"Total":0.0,"VATTotal":0.0,"ServiceId":6,"InstantiatedFromId":0,"Paid":false,"RequestedBill":false,"OverPayments":0.0,"Change":0.0,"CashGiven":0.0}
        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session.getId())
                .get("Session/{id}");

        HostSessionView sessionView = response2.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        List<HostDinerView> diners = sessionView.getDiners();
        assertEquals(waitingPartyPayload.getNumberOfPeople(), diners.size()-1);
        assertTrue(diners.get(0).isTable());
        for(int i = 1; i < diners.size(); i++) {
            assertFalse(diners.get(i).isTable());
        }
        assertEquals(1, sessionView.getTables().size());
        assertEquals(tableToSeat.getId(), sessionView.getTables().get(0).getId());
        assertEquals(tableToSeat.getName(), sessionView.getTables().get(0).getName());
        assertNotNull(sessionView.getTables().get(0).getPosition());
        assertEquals(tableToSeat.getShape().getOrdinal(), sessionView.getTables().get(0).getShape());
        assertTrue(StringUtils.isBlank(sessionView.getChairData()));
        assertEquals(0, sessionView.getDelay());
        assertFalse(sessionView.getAdhoc());
        assertEquals(session.getId(), sessionView.getId());
        assertEquals("Seated", sessionView.getSessionType());
        assertTrue(sessionView.getStartTime() > (System.currentTimeMillis()-10000)/1000);
        assertEquals(0, sessionView.getClosedTime());
        assertEquals(serviceToUse.getName(), sessionView.getServiceName());
        assertEquals(serviceToUse.getId(), sessionView.getServiceId());
        assertEquals(serviceToUse.getDefaultMenuId(), sessionView.getMenuId());
        assertEquals(waitingPartyPayload.getName(), sessionView.getPartyName());
        assertFalse(sessionView.isVoided());
        assertEquals(3, sessionView.getScheduledEvents().size());
        for(int i = 0; i < sessionView.getScheduledEvents().size(); i++) {
            ScheduledNotificationView scheduledNotificationView = sessionView.getScheduledEvents().get(i);
            assertEquals(serviceToUse.getId(), scheduledNotificationView.getServiceId());
            assertEquals(scheduledItemList.get(i).getId(), scheduledNotificationView.getId());
            assertEquals(1, scheduledNotificationView.getNotifications().size());
            for(HostNotificationView hostNotificationView : scheduledNotificationView.getNotifications()) {
                assertNotNull(hostNotificationView.getId());
                assertEquals(0, hostNotificationView.getAcknowledgements().size());
                assertEquals(scheduledItemList.get(i).getTarget(), hostNotificationView.getTarget());
                assertEquals(scheduledItemList.get(i).getText(), hostNotificationView.getText());
            }
        }

        assertEquals(1, sessionView.getRecurringEvents().size());
        for(int i = 0; i < sessionView.getRecurringEvents().size(); i++) {
            RecurringNotificationView recurringNotificationView = sessionView.getRecurringEvents().get(i);
            assertEquals(serviceToUse.getId(), recurringNotificationView.getServiceId());
            assertEquals(recurringItemList.get(i).getId(), recurringNotificationView.getId());
            assertEquals(recurringItemList.get(i).getRecurring() / 1000, recurringNotificationView.getPeriod());
            assertEquals(1, recurringNotificationView.getNotifications().size());
            for(HostNotificationView hostNotificationView : recurringNotificationView.getNotifications()) {
                assertNotNull(hostNotificationView.getId());
                assertEquals(0, hostNotificationView.getAcknowledgements().size());
                assertEquals(recurringItemList.get(i).getTarget(), hostNotificationView.getTarget());
            }
        }

        //check notifications are built properly
        //GET /Event
        Response response3 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Event");

        HostEventView[] hostEventViews = response3.getBody().as(HostEventView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(4, hostEventViews.length);

        for(int i = 0; i < hostEventViews.length; i++) {
            if(i > 0) {
                assertTrue(hostEventViews[i].getDue() >= hostEventViews[i-1].getDue());
            }
            assertEquals(session.getId(), hostEventViews[i].getSessionId());
            assertEquals(NotificationConstant.TARGET_WAITER_ACTION.getConstant(), hostEventViews[i].getTarget());
            assertNotNull(hostEventViews[i].getId());
            assertTrue(hostEventViews[i].getDue() > 0);
            assertNotNull(hostEventViews[i].getDelay());
            assertNotNull(hostEventViews[i].getText());
        }

        //check print batches
        //GET /Print
        Response response4 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        HostBatchView[] hostBatchViews = response4.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, hostBatchViews.length);

        //add some orders
        createOrder(token, session, course1, menuItem1);

        Response response5 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        hostBatchViews = response5.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, hostBatchViews.length);

        unSpoolAllBatches();
        Response response6 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        hostBatchViews = response6.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2);
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

    protected Response createSeatedParty(String token, WaitingPartyPayload waitingPartyPayload) {
        return given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(Params.AUTHORIZATION, token)
                    .body(waitingPartyPayload)
                    .post("Waiting");
    }

    protected WaitingPartyPayload createWaitingPartyPayload(Table tableToSeat, Service serviceToUse) {
        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setName(tableToSeat.getName());
        waitingPartyPayload.setNumberOfPeople(20);
        waitingPartyPayload.setServiceId(serviceToUse.getId());
        waitingPartyPayload.setCreateSession(true);
        waitingPartyPayload.getTables().add(tableToSeat.getId());
        return waitingPartyPayload;
    }

    protected Tuple<WaitingPartyPayload,Session> createSession(String token, Table tableToSeat) {
        Service serviceToUse = testRestaurant.getServices().get(0);

        // POST /Waiting with {"Name":<table name>,"NumberOfPeople":2,"ServiceId":<service id>,"CreateSession":true,"Tables":[<table id>]}
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload(tableToSeat, serviceToUse);
        Response response1 = createSeatedParty(token, waitingPartyPayload);

        PartyResponse partyResponse = response1.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(partyResponse.getSessionId());

        return new Tuple<>(waitingPartyPayload, session);
    }
}

