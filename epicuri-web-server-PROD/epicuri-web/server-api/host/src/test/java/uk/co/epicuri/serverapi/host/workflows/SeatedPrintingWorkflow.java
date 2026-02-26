package uk.co.epicuri.serverapi.host.workflows;

import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdList;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostBatchView;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by manish on 03/11/2017.
 */
public class SeatedPrintingWorkflow extends SeatedSessionWorkflows {
    @Value("${epicuri.waiter.print.window}")
    private long printSpoolWindow;

    @Test
    public void testBatchesAreReturned1() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);

        Session session = setUpSessionAndBatches(token);

        List<Order> orders = orderRepository.findBySessionId(session.getId());
        assertEquals(4, orders.size());

        assertEquals(4, batchRepository.findBySessionId(session.getId()).size());

        unSpoolAllBatches();
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        List<HostBatchView> batches = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(4, batches.size());
        batches.forEach(b -> assertEquals(1, b.getOrders().size()));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        batches = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(0, batches.size());
    }

    public Session setUpSessionAndBatches(String token) {
        Tuple<WaitingPartyPayload,Session> tuple = createSession(token, testRestaurant.getTables().get(0));

        Session session = tuple.getB();

        //create a set of separate orders
        List<HostBatchView> batches1 = createOrder(token, session, course1, menuItem1);
        List<HostBatchView> batches2 = createOrder(token, session, course1, menuItem1);
        List<HostBatchView> batches3 = createOrder(token, session, course1, menuItem2);
        List<HostBatchView> batches4 = createOrder(token, session, course1, menuItem3);

        batches1.forEach(b -> assertEquals(1, b.getOrders().size()));
        batches2.forEach(b -> assertEquals(1, b.getOrders().size()));
        batches3.forEach(b -> assertEquals(1, b.getOrders().size()));
        batches4.forEach(b -> assertEquals(1, b.getOrders().size()));
        return session;
    }

    @Test
    public void testFailedPrint() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);
        Session session = setUpSessionAndBatches(token);

        List<Batch> batches = liveDataService.getBatchesBySessionId(session.getId());
        assertTrue(batches.stream().allMatch(Batch::isAwaitingImmediatePrint));

        // a failed print spools the batch
        IdList idList = new IdList();
        batches.forEach(b -> idList.getIds().add(b.getId()));
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(idList)
                .put("Print/spool");

        batches = liveDataService.getBatchesBySessionId(session.getId());
        assertFalse(batches.stream().allMatch(Batch::isAwaitingImmediatePrint));
        assertTrue(batches.stream().allMatch(b -> b.getSpoolTime().size() == 1));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        HostBatchView[] views = response.getBody().as(HostBatchView[].class);
        assertEquals(0, views.length);

        batches.forEach(b -> b.getSpoolTime().add(System.currentTimeMillis() - printSpoolWindow - 1));
        batchRepository.save(batches);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        views = response.getBody().as(HostBatchView[].class);
        assertEquals(4, views.length);
    }

    @Test
    public void testBatchesAreReturned2() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);

        Tuple<WaitingPartyPayload,Session> tuple1 = createSession(token, testRestaurant.getTables().get(0));
        Session session1 = tuple1.getB();
        //create a set of separate orders
        List<HostBatchView> batches1 = createOrder(token, session1, course1, menuItem1);
        batches1.forEach(b -> assertEquals(1, b.getOrders().size()));

        Tuple<WaitingPartyPayload,Session> tuple2 = createSession(token, testRestaurant.getTables().get(1));
        Session session2 = tuple2.getB();
        //create a set of separate orders
        List<HostBatchView> batches2 = createOrder(token, session2, course1, menuItem1);
        batches2.forEach(b -> assertEquals(1, b.getOrders().size()));

        unSpoolAllBatches();
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        List<HostBatchView> batches = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(2, batches.size());
        batches.forEach(b -> assertEquals(1, b.getOrders().size()));
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

    @Test
    public void testGetPrintJobsForDifferentOrders() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);

        Tuple<WaitingPartyPayload,Session> tuple1 = createSession(token, testRestaurant.getTables().get(0));
        Session session1 = tuple1.getB();
        //create a set of separate orders
        setUpItem(testRestaurant, printer1, menuItem1);
        setUpItem(testRestaurant, printer3, menuItem2);

        List<HostBatchView> batches1 = createOrder(token, session1, course1, menuItem1, menuItem2);
        assertEquals(2, batches1.size());
    }

    @Test
    public void testStaffPopulated() throws Exception {
        // create a seated session
        String token = getTokenForStaff(testStaff);

        Tuple<WaitingPartyPayload,Session> tuple1 = createSession(token, testRestaurant.getTables().get(0));
        Session session1 = tuple1.getB();
        List<HostBatchView> batches = createOrder(token, session1, course1, menuItem1);
        assertEquals(1, batches.size());
        assertEquals(testStaff.getUserName(), batches.get(0).getStaffUserName());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        List<HostBatchView> batches2 = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(0, batches2.size());

        unSpoolAllBatches();
        resetImmediatePrintingToFalse(session1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
        List<HostBatchView> batches3 = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(1, batches3.size());
    }

    @Test
    public void testSelfServicePopulated1() throws Exception {
        testSelfService(ActivityInstantiationConstant.ANDROID);
    }

    @Test
    public void testSelfServicePopulated2() throws Exception {
        testSelfService(ActivityInstantiationConstant.IOS);
    }

    @Test
    public void testSelfServicePopulated3() throws Exception {
        testSelfService(ActivityInstantiationConstant.CUSTOMER);
    }

    public void testSelfService(ActivityInstantiationConstant constant) {
        String token = getTokenForStaff(testStaff);

        Tuple<WaitingPartyPayload,Session> tuple1 = createSession(token, testRestaurant.getTables().get(0));
        Session session1 = tuple1.getB();

        List<Order> orders = new ArrayList<>();
        CustomerOrderItemView orderView = new CustomerOrderItemView();
        orderView.setInstantiatedFromId(constant.getId());
        orderView.setMenuItemId(menuItem1.getId());
        orders.add(new Order(session1, orderView, new ArrayList<>(), tax1, menuItem1, session1.getDiners().get(0), null));
        liveDataService.insertOrders(session1, Collections.singleton(menuItem1), orders, true, true);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");

        List<HostBatchView> batches = Lists.newArrayList(response.getBody().as(HostBatchView[].class));
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).isSelfService());
    }
}
