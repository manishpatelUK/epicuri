package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.OrderAttributionView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.service.AsyncOrderHandlerService;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.util.*;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.sessionId;
import static org.junit.Assert.*;

/**
 * Created by Manish Patel
 */
public class OrderControllerTest extends SessionSetupBaseIT {

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpSession();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        resetOrdersAndBatches();

        printer1.setIp("192.168.0.1");
        printer2.setIp("192.168.0.2");
        printerRepository.save(printer1);
        printerRepository.save(printer2);
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem2.setDefaultPrinter(printer1.getId());
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        modifier1.setPrice(1);
        modifier1.setPriceOverride(1);
        modifierRepository.save(modifier1);

        //whitebox PaymentSense
        PaymentSenseRestService mock = EasyMock.createMock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService,mock);
    }

    private void resetOrdersAndBatches() {
        orderRepository.deleteAll();
        batchRepository.deleteAll();
    }

    private OrderRequest createOrderRequest(MenuItem menuItem, Diner diner, boolean noSessionId) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCourseId(course1.getId());
        if(diner != null) {
            orderRequest.setDinerId(diner.getId());
        }
        orderRequest.setInstantiatedFromId(ActivityInstantiationConstant.WAITER.getId());
        orderRequest.setMenuItemId(menuItem.getId());
        orderRequest.setModifiers(Collections.singletonList(modifier1.getId()));
        orderRequest.setNote("foobar");
        orderRequest.setQuantity(1);
        if(!noSessionId) {
            orderRequest.setSessionId(session1.getId());
        }

        return orderRequest;
    }

    private void testEqual(OrderRequest request, Order order) {
        assertEquals(request.getCourseId(), order.getCourseId());
        assertEquals(request.getInstantiatedFromId(), order.getInstantiatedFrom().getId());
        assertEquals(request.getMenuItemId(), order.getMenuItemId());
        assertEquals(request.getModifiers(), order.getModifiers().stream().map(Modifier::getId).collect(Collectors.toList()));
        assertEquals(request.getNote(), order.getNote());
        assertEquals(request.getQuantity(), order.getQuantity());
        if(request.getSessionId() != null) {
            assertEquals(request.getSessionId(), order.getSessionId());
        } else if(request.getDinerId() != null){
            assertEquals(IDAble.extractParentId(request.getDinerId()), order.getSessionId());
        }
        if(request.getDinerId() != null) {
            assertEquals(request.getDinerId(), order.getDinerId());
        }
    }

    @Test
    public void testPostOrder() throws Exception {
        String token = getTokenForStaff(staff1);

        OrderRequest orderRequest = createOrderRequest(menuItem1, null, false);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Order order = orderRepository.findAll().get(0);
        Batch batch = batchRepository.findAll().get(0);
        testEqual(orderRequest, order);
        assertTrue(batch.getOrderIds().contains(order.getId()));
        assertEquals(session1.getId(), order.getSessionId());
        assertEquals(1, batch.getOrderIds().size());
        assertEquals(session1.getId(), batch.getSessionId());
        assertEquals(menuItem1.getDefaultPrinter(), batch.getPrinterId());
        assertTrue(batch.getCreationTime() >= System.currentTimeMillis()-1000);
        assertTrue(batch.getIntendedPrintTime() >= System.currentTimeMillis()-1000);
        assertNull(batch.getPrintedTime());
        assertTrue(batch.isAwaitingImmediatePrint());
        assertEquals(0, batch.getSpoolTime().size());
        OrderResponse orderResponse = response.as(OrderResponse.class);
        assertEquals(1, orderResponse.getBatches().size());
        assertEquals(batch.getId(), orderResponse.getBatches().get(0).getId());
        assertNull(batch.getDeleted());

        resetOrdersAndBatches();

        orderRequest = createOrderRequest(menuItem1, diner1, true);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        order = orderRepository.findAll().get(0);
        batch = batchRepository.findAll().get(0);
        testEqual(orderRequest, order);
        assertTrue(batch.getOrderIds().contains(order.getId()));
        assertEquals(1, batch.getOrderIds().size());
        assertEquals(session1.getId(), batch.getSessionId());
        assertEquals(menuItem1.getDefaultPrinter(), batch.getPrinterId());
        assertTrue(batch.getCreationTime() >= System.currentTimeMillis()-1000);
        assertTrue(batch.getIntendedPrintTime() >= System.currentTimeMillis()-1000);
        assertNull(batch.getPrintedTime());
        assertTrue(batch.isAwaitingImmediatePrint());
        assertEquals(0, batch.getSpoolTime().size());
        assertNull(batch.getDeleted());
        assertEquals(diner1.getId(), order.getDinerId());

        resetOrdersAndBatches();
        menuItem2.setDefaultPrinter(printer1.getId());
        menuItemRepository.save(menuItem2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(Arrays.asList(createOrderRequest(menuItem1, diner1, true),createOrderRequest(menuItem2, diner1, true)))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        batch = batchRepository.findAll().get(0);
        assertEquals(2, batch.getOrderIds().size());
        assertEquals(session1.getId(), batch.getSessionId());
        assertEquals(menuItem1.getDefaultPrinter(), batch.getPrinterId());
        assertTrue(batch.getCreationTime() >= System.currentTimeMillis()-1000);
        assertTrue(batch.getIntendedPrintTime() >= System.currentTimeMillis()-1000);
        assertNull(batch.getPrintedTime());
        assertTrue(batch.isAwaitingImmediatePrint());
        assertEquals(0, batch.getSpoolTime().size());
        assertNull(batch.getDeleted());

        resetOrdersAndBatches();
        menuItem2.setDefaultPrinter(printer2.getId());
        menuItemRepository.save(menuItem2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(Arrays.asList(createOrderRequest(menuItem1, diner1, true),createOrderRequest(menuItem2, diner1, true)))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        batch = batchRepository.findAll().get(0);
        assertEquals(2, batchRepository.findAll().size());
        assertEquals(1, batch.getOrderIds().size());
    }

    @Test
    public void testPostOrderImmediatePrintFlag() throws Exception {
        String token = getTokenForStaff(staff1);

        OrderRequest orderRequest = createOrderRequest(menuItem1, null, false);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("willAttemptImmediatePrint", true)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Order order = orderRepository.findAll().get(0);
        Batch batch = batchRepository.findAll().get(0);
        testEqual(orderRequest, order);
        assertTrue(batch.getOrderIds().contains(order.getId()));
        assertEquals(session1.getId(), order.getSessionId());
        assertEquals(1, batch.getOrderIds().size());
        assertEquals(session1.getId(), batch.getSessionId());
        assertEquals(menuItem1.getDefaultPrinter(), batch.getPrinterId());
        assertTrue(batch.getCreationTime() >= System.currentTimeMillis()-1000);
        assertTrue(batch.getIntendedPrintTime() >= System.currentTimeMillis()-1000);
        assertNull(batch.getPrintedTime());
        assertTrue(batch.isAwaitingImmediatePrint());
        assertEquals(0, batch.getSpoolTime().size());
        OrderResponse orderResponse = response.as(OrderResponse.class);
        assertEquals(1, orderResponse.getBatches().size());
        assertEquals(batch.getId(), orderResponse.getBatches().get(0).getId());
        assertNull(batch.getDeleted());
    }

    @Test
    public void testPostOrderNoPrintFlag() throws Exception {
        String token = getTokenForStaff(staff1);

        OrderRequest orderRequest = createOrderRequest(menuItem1, null, false);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("orderPrintsRequired", false)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Order order = orderRepository.findAll().get(0);
        Batch batch = batchRepository.findAll().get(0);
        testEqual(orderRequest, order);
        assertTrue(batch.getOrderIds().contains(order.getId()));
        assertEquals(session1.getId(), order.getSessionId());
        assertEquals(1, batch.getOrderIds().size());
        assertEquals(session1.getId(), batch.getSessionId());
        assertEquals(menuItem1.getDefaultPrinter(), batch.getPrinterId());
        assertTrue(batch.getCreationTime() >= System.currentTimeMillis()-1000);
        assertTrue(batch.getIntendedPrintTime() >= System.currentTimeMillis()-1000);
        assertNotNull(batch.getPrintedTime());
        assertTrue(batch.isAwaitingImmediatePrint());
        assertEquals(1, batch.getSpoolTime().size());
        OrderResponse orderResponse = response.as(OrderResponse.class);
        assertEquals(0, orderResponse.getBatches().size());
        assertNull(batch.getDeleted());
    }

    @Test
    public void testPostOrderPriceOverride() throws Exception {
        String token = getTokenForStaff(staff1);

        OrderRequest orderRequest = createOrderRequest(menuItem1, null, false);
        orderRequest.setPriceOverride(MoneyService.toMoneyRoundNearest(menuItem1.getPrice()+1));
        orderRequest.setQuantity(3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("willAttemptImmediatePrint", true)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Order order = orderRepository.findAll().get(0);

        assertEquals(3, order.getQuantity());
        assertEquals(menuItem1.getPrice()+1, order.getPriceOverride());

        Session session = sessionRepository.findOne(order.getSessionId());
        Map<CalculationKey,Number> numbers = sessionCalculationService.calculateValues(session);
        assertEquals(((menuItem1.getPrice()+1)*3) + 3, numbers.get(CalculationKey.TOTAL)); //+3 because there's a modifier of value 1
    }

    @Test
    public void testPostOrderWithModifierAndPriceOverride() throws Exception {
        String token = getTokenForStaff(staff1);

        OrderRequest orderRequest = createOrderRequest(menuItem1, null, false);
        orderRequest.setPriceOverride(MoneyService.toMoneyRoundNearest(menuItem1.getPrice()+1));
        orderRequest.setQuantity(3);
        modifier1.setPriceOverride(10);
        modifier1.setPrice(10);
        modifierRepository.save(modifier1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("willAttemptImmediatePrint", true)
                .body(Collections.singletonList(orderRequest))
                .post("Order");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Order order = orderRepository.findAll().get(0);

        assertEquals(3, order.getQuantity());
        assertEquals(menuItem1.getPrice()+1, order.getPriceOverride());

        Session session = sessionRepository.findOne(order.getSessionId());
        Map<CalculationKey,Number> numbers = sessionCalculationService.calculateValues(session);
        assertEquals(((menuItem1.getPrice()+1)*3) + 30, numbers.get(CalculationKey.TOTAL)); //+3 because there's a modifier of value 1
    }

    @Ignore
    @Test
    public void testPutOrder() throws Exception {

    }

    @Ignore
    @Test
    public void testPutRemoveFromReports() throws Exception {

    }

    @Ignore
    @Test
    public void testDeleteOrder() throws Exception {

    }

    @Test
    public void testRemoveOrderFromBill() throws Exception {
        setUpSession();
        staff1.setRestaurantId(restaurant1.getId());
        String token = getTokenForStaff(staff1);

        OrderRemovalAdjustmentRequest removalAdjustmentRequest = new OrderRemovalAdjustmentRequest();
        removalAdjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(removalAdjustmentRequest)
                .pathParam("id", order1.getId())
                .put("Order/RemoveOrderFromBill/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Order order = orderRepository.findOne(order1.getId());
        assertEquals(adjustmentType1.getId(), order.getAdjustment().getAdjustmentType().getId());
        assertEquals(order.getPriceOverride(), order.getAdjustment().getValue());

        order1.setDeleted(System.currentTimeMillis());
        orderRepository.save(order1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(removalAdjustmentRequest)
                .pathParam("id", order1.getId())
                .put("Order/RemoveOrderFromBill/{id}");

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(removalAdjustmentRequest)
                .pathParam("id", "foobar")
                .put("Order/RemoveOrderFromBill/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testRemoveAllOrdersFromSession() throws Exception {
        setUpSession();
        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        batch1.setOrderIds(Collections.singleton(order1.getId()));
        Set<String> set = new HashSet<>();
        set.add(order2.getId());
        set.add(order3.getId());
        batch2.setOrderIds(set);
        batch2.setOrderIds(Collections.singleton(order3.getId()));
        batch1.setSessionId(session1.getId());
        batch2.setSessionId(session1.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Order/RemoveAllOrdersFromSession/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNull(orderRepository.findOne(order1.getId()));
        assertNull(orderRepository.findOne(order2.getId()));
        assertNull(orderRepository.findOne(order3.getId()));
        assertNull(batchRepository.findOne(batch1.getId()));
        assertNull(batchRepository.findOne(batch2.getId()));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .put("Order/RemoveAllOrdersFromSession/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testReallocateOrders() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpSession();

        OrderAttributionView orderAttributionView = new OrderAttributionView();
        orderAttributionView.setDinerId(diner2.getId());
        orderAttributionView.getOrderIds().add(order1.getId());
        orderAttributionView.getOrderIds().add(order2.getId());
        orderAttributionView.getOrderIds().add("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(orderAttributionView)
                .pathParam("id", session1.getId())
                .put("Order/allocate/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(diner2.getId(), orderRepository.findOne(order1.getId()).getDinerId());
        assertEquals(diner2.getId(), orderRepository.findOne(order2.getId()).getDinerId());
        assertNotEquals(diner2.getId(), orderRepository.findOne(order3.getId()).getDinerId());
    }

    @Test
    public void testReallocateOrdersToDefaultDiner() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpSession();

        OrderAttributionView orderAttributionView = new OrderAttributionView();
        orderAttributionView.setDinerId(diner2.getId());
        orderAttributionView.getOrderIds().add(order1.getId());
        orderAttributionView.getOrderIds().add(order2.getId());
        orderAttributionView.getOrderIds().add("foo");
        orderAttributionView.getUnassignedOrderIds().add(order1.getId());
        orderAttributionView.getUnassignedOrderIds().add(order3.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(orderAttributionView)
                .pathParam("id", session1.getId())
                .put("Order/allocate/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(diner2.getId(), orderRepository.findOne(order1.getId()).getDinerId());
        assertEquals(diner2.getId(), orderRepository.findOne(order2.getId()).getDinerId());
        assertEquals(diner1.getId(), orderRepository.findOne(order3.getId()).getDinerId());
        assertEquals(3, liveDataService.getOrders(session1.getId()).size());
    }
}