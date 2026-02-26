package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.Payment;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderAndAdjustmentsPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class WaitingControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        Service adhoc = new Service(restaurant1);
        adhoc.setName("Ad hoc");
        adhoc.setSessionType(SessionType.ADHOC);
        restaurant1.getServices().add(adhoc);
        restaurant1.getServices().forEach(s -> {
            if(s.getCourses().size() == 0) {
                Course course = new Course(s);
                course.setName(RestaurantConstants.IMMEDIATE_COURSE_NAME);
                s.getCourses().add(course);
            }
        });
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testPostWaitingNoCreateSession() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyPayload payload = new WaitingPartyPayload();
        payload.setAdHoc(false);
        payload.setCreateSession(false);
        payload.setNumberOfPeople(3);
        payload.setName("foobar");

        postWaiting(token, payload);
    }

    private Response postWaiting(String token, WaitingPartyPayload payload) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        PartyResponse partyResponse = response.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        Party party = partyRepository.findOne(partyResponse.getId());

        assertEquals("0", partyResponse.getSessionId());
        assertEquals("0", partyResponse.getServiceId());

        assertEquals(payload.getNumberOfPeople(), party.getNumberOfPeople());
        assertEquals(payload.getName(), party.getName());
        assertFalse(partyResponse.isAdHoc());
        assertFalse(partyResponse.isCreateSession());
        assertEquals(party.getName(), partyResponse.getName());

        return response;
    }

    @Test
    public void testPostWaitingNoCreateSessionWithCustomer() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyPayload payload = new WaitingPartyPayload();
        payload.setAdHoc(false);
        payload.setCreateSession(false);
        payload.setNumberOfPeople(3);
        payload.setName("foobar");
        HostCustomerView hostCustomerView = new HostCustomerView();
        hostCustomerView.setId(customer1.getId());
        payload.setCustomer(hostCustomerView);

        postWaiting(token, payload);
    }

    @Test
    public void testPostWaitingNoCreateSessionWithCustomerCheckin() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyPayload payload = new WaitingPartyPayload();
        payload.setAdHoc(false);
        payload.setCreateSession(false);
        payload.setNumberOfPeople(3);
        payload.setName("foobar");
        HostCustomerView hostCustomerView = new HostCustomerView();
        hostCustomerView.setId(customer1.getId());
        payload.setCustomer(hostCustomerView);

        CheckIn checkIn = new CheckIn();
        checkIn.setCustomerId(customer1.getId());
        checkIn.setTime(System.currentTimeMillis());
        checkIn.setRestaurantId(staff1.getRestaurantId());
        checkIn = checkInRepository.save(checkIn);

        postWaiting(token, payload);
        assertNotNull(checkInRepository.findOne(checkIn.getId()).getPartyId());
    }

    @Test
    public void testPostWaitingCreateSession() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyPayload payload = createWaitingPartyPayload();
        List<String> list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        PartyResponse partyResponse = response.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        Party party = partyRepository.findOne(partyResponse.getId());
        assertNotNull(party);
        Session session = sessionRepository.findOne(partyResponse.getSessionId());

        assertEquals(payload.getNumberOfPeople(), session.getDiners().size()-1);
        assertEquals(payload.getServiceId(), session.getService().getId());
        assertEquals(table1.getId(), session.getTables().get(0));
        assertEquals(1, session.getTables().size());

        //clear tables
        session.getTables().clear();
        sessionRepository.save(session);

        payload.setCustomer(new HostCustomerView(customer1, new HashMap<>()));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        partyResponse = response.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        party = partyRepository.findOne(partyResponse.getId());
        session = sessionRepository.findByOriginalPartyId(party.getId());

        assertEquals(payload.getNumberOfPeople(), session.getDiners().size()-1);
        assertEquals(payload.getServiceId(), session.getService().getId());
        assertEquals(table1.getId(), session.getTables().get(0));
        assertEquals(1, session.getTables().size());
        assertTrue(session.getDiners().stream().anyMatch(d -> d.getCustomerId() != null && d.getCustomerId().equals(customer1.getId())));

        payload.setAdHoc(true);
        payload.setTables(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        partyResponse = response.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        party = partyRepository.findOne(partyResponse.getId());
        assertNotNull(party);
        session = sessionRepository.findOne(partyResponse.getSessionId());
        assertNotNull(session.getService());
        assertEquals(SessionType.ADHOC, session.getSessionType());
    }

    @Test
    public void testPostWaitingCreateSessionWithCustomerCheckin() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyPayload payload = createWaitingPartyPayload();
        List<String> list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);
        payload.setCustomer(new HostCustomerView(customer1, new HashMap<>()));

        CheckIn checkIn = new CheckIn();
        checkIn.setRestaurantId(staff1.getRestaurantId());
        checkIn.setCustomerId(customer1.getId());
        checkIn.setTime(System.currentTimeMillis());
        checkInRepository.save(checkIn);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        PartyResponse partyResponse = response.getBody().as(PartyResponse.class, ObjectMapperType.JACKSON_2);
        Party party = partyRepository.findOne(partyResponse.getId());
        Session session = sessionRepository.findByOriginalPartyId(party.getId());

        assertEquals(payload.getNumberOfPeople(), session.getDiners().size()-1);
        assertEquals(payload.getServiceId(), session.getService().getId());
        assertEquals(table1.getId(), session.getTables().get(0));
        assertEquals(1, session.getTables().size());
        assertTrue(session.getDiners().stream().anyMatch(d -> d.getCustomerId() != null && d.getCustomerId().equals(customer1.getId())));
        CheckIn testCheckin = checkInRepository.findOne(checkIn.getId());
        assertEquals(session.getId(), testCheckin.getSessionId());
        assertEquals(party.getId(), testCheckin.getPartyId());
    }


    @Test
    public void testPostPartyWithOrder() throws Exception {
        setUpPrintersAndItems();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        OrderResponse orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(orderResponse.getSessionId());

        List<Order> orders = orderRepository.findBySessionId(session.getId());
        assertEquals(1, orders.size());
        assertEquals(orderPayload.getCourseId(), orders.get(0).getCourseId());
        assertEquals(orderPayload.getInstantiatedFromId(), orders.get(0).getInstantiatedFrom().getId());
        assertEquals(orderPayload.getMenuItemId(), orders.get(0).getMenuItemId());
        assertEquals(orderPayload.getModifiers().get(0), orders.get(0).getModifiers().get(0).getId());
        assertEquals(orderPayload.getNote(), orders.get(0).getNote());
        assertEquals(orderPayload.getQuantity(), orders.get(0).getQuantity());
        assertNull(orders.get(0).getDeliveryLocation());
        List<Batch> batches = batchRepository.findBySessionId(session.getId());
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).isAwaitingImmediatePrint());
        assertEquals(1, orderResponse.getBatches().size());
        assertEquals(batches.get(0).getId(), orderResponse.getBatches().get(0).getId());
        assertEquals(waitingPartyPayload.getNumberOfPeople() + 1, session.getDiners().size());

        // test 0 quantity
        orderPayload.setQuantity(0);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        session = sessionRepository.findOne(orderResponse.getSessionId());

        orders = orderRepository.findBySessionId(session.getId());
        assertEquals(0, orders.size());

        //adhoc converted to tab
        waitingPartyPayload.setNumberOfPeople(0);
        waitingPartyPayload.setTables(new ArrayList<>());
        waitingPartyPayload.setAdHoc(false);
        waitingPartyPayload.setServiceId(service1.getId());
        orderPayload.setQuantity(1);
        orderPayload.setCourseId("-1");
        orderPayload.setDinerId("-1");

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        session = sessionRepository.findOne(orderResponse.getSessionId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(2, session.getDiners().size());
        orders = orderRepository.findBySessionId(session.getId());
        assertEquals(1, orders.size());
        assertNotEquals("-1", orders.get(0).getCourseId());
        assertNotEquals("-1", orders.get(0).getDinerId());
    }

    @Test
    public void testPostPartyWithOrderWithLocation() throws Exception {
        setUpPrintersAndItems();
        String token = getTokenForStaff(staff1);

        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        String location = "Foobare12";
        payload.setOrderLocation(location);
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        OrderResponse orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(orderResponse.getSessionId());
        List<Order> orders = orderRepository.findBySessionId(session.getId());

        assertEquals(location, orders.get(0).getDeliveryLocation());
        assertEquals(location, orderResponse.getBatches().get(0).getDeliveryLocation());
    }

    @Test
    public void testPostPartyWithOrderNoPrint() throws Exception {
        setUpPrintersAndItems();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .queryParam("orderPrintsRequired", false)
                .post("Waiting/PostWaitingWithOrder");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        OrderResponse orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(orderResponse.getSessionId());

        List<Order> orders = orderRepository.findBySessionId(session.getId());
        assertEquals(1, orders.size());
        assertEquals(orderPayload.getCourseId(), orders.get(0).getCourseId());
        assertEquals(orderPayload.getInstantiatedFromId(), orders.get(0).getInstantiatedFrom().getId());
        assertEquals(orderPayload.getMenuItemId(), orders.get(0).getMenuItemId());
        assertEquals(orderPayload.getModifiers().get(0), orders.get(0).getModifiers().get(0).getId());
        assertEquals(orderPayload.getNote(), orders.get(0).getNote());
        assertEquals(orderPayload.getQuantity(), orders.get(0).getQuantity());
        List<Batch> batches = batchRepository.findBySessionId(session.getId());
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).isAwaitingImmediatePrint());
        assertEquals(1, batches.get(0).getSpoolTime().size());
        assertNotNull(batches.get(0).getPrintedTime());
        assertEquals(0, orderResponse.getBatches().size());
        assertEquals(waitingPartyPayload.getNumberOfPeople() + 1, session.getDiners().size());
    }

    @Test
    public void testPostPartyWithOrderRefund() throws Exception {
        setUpPrintersAndItems();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        waitingPartyPayload.setRefund(true);
        waitingPartyPayload.setAdHoc(true);
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        OrderResponse orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(orderResponse.getSessionId());
        List<HostBatchView> batches = orderResponse.getBatches();

        assertEquals(SessionType.REFUND, session.getSessionType());
        assertEquals(0, batches.size());
    }

    public WaitingPartyOrderPayload.OrderPayload createOrderPayload() {
        WaitingPartyOrderPayload.OrderPayload orderPayload = new WaitingPartyOrderPayload.OrderPayload();
        orderPayload.setCourseId(course1.getId());
        orderPayload.setInstantiatedFromId(ActivityInstantiationConstant.WAITER.getId());
        orderPayload.setMenuItemId(menuItem1.getId());
        orderPayload.getModifiers().add(modifier1.getId());
        orderPayload.setNote("foobar");
        orderPayload.setQuantity(1);
        return orderPayload;
    }

    public void setUpPrintersAndItems() {
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem1.setTaxTypeId(tax1.getId());
        menuItemRepository.save(menuItem1);
        printer1.setRestaurantId(restaurant1.getId());
        printer1.setIp("192.168.0.1");
        printerRepository.save(printer1);
    }

    public WaitingPartyPayload createWaitingPartyPayload() {
        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setAdHoc(false);
        waitingPartyPayload.setCreateSession(true);
        waitingPartyPayload.setServiceId(service1.getId());
        waitingPartyPayload.setNumberOfPeople(3);
        return waitingPartyPayload;
    }

    @Test
    public void testPostPartyWithOrderImmediatePrint() throws Exception {
        setUpPrintersAndItems();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("willAttemptImmediatePrint", true)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");

        OrderResponse orderResponse = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(orderResponse.getSessionId());

        List<Batch> batches = batchRepository.findBySessionId(session.getId());
        assertEquals(1, batches.size());
    }

    @Test
    public void testPostPartyWithOrderAndAdjustments() throws Exception {
        setUpItemsAndOrdersAndAdjustments();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderAndAdjustmentsPayload payload = new WaitingPartyOrderAndAdjustmentsPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        AdjustmentRequest request1 = createAdjustmentRequest(0.01);
        AdjustmentRequest request2 = createAdjustmentRequest(0.09);

        payload.getAdjustments().add(request1);
        payload.getAdjustments().add(request2);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrderAndAdjustments");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        OrderResponse sessionPayload = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        assertTrue(sessionPayload.getHostSessionView().getRemainingTotal() != sessionPayload.getHostSessionView().getTotal());

        Session session = sessionRepository.findOne(sessionPayload.getSessionId());

        List<Order> orders = orderRepository.findBySessionId(session.getId());
        assertEquals(1, orders.size());
        assertEquals(orderPayload.getCourseId(), orders.get(0).getCourseId());
        assertEquals(orderPayload.getInstantiatedFromId(), orders.get(0).getInstantiatedFrom().getId());
        assertEquals(orderPayload.getMenuItemId(), orders.get(0).getMenuItemId());
        assertEquals(orderPayload.getModifiers().get(0), orders.get(0).getModifiers().get(0).getId());
        assertEquals(orderPayload.getNote(), orders.get(0).getNote());
        assertEquals(orderPayload.getQuantity(), orders.get(0).getQuantity());
        List<Batch> batches = batchRepository.findBySessionId(session.getId());
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).isAwaitingImmediatePrint());
        assertEquals(1, sessionPayload.getBatches().size());
        assertEquals(batches.get(0).getId(), sessionPayload.getBatches().get(0).getId());
        assertEquals(waitingPartyPayload.getNumberOfPeople() + 1, session.getDiners().size());
        assertEquals(2, session.getAdjustments().size());
        assertNull(session.getClosedTime());

        request1.setValue(0.04);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrderAndAdjustments");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        sessionPayload = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        session = sessionRepository.findOne(sessionPayload.getSessionId());

        assertNotNull(session.getClosedTime());
        assertNotNull(session.getClosedBy());
    }

    public AdjustmentRequest createAdjustmentRequest(double v) {
        AdjustmentRequest request1 = new AdjustmentRequest();
        request1.setAdjustmentTypeId(adjustmentType2.getId());
        request1.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        request1.setSessionId(null);
        request1.setValue(v);
        return request1;
    }

    public void setUpItemsAndOrdersAndAdjustments() {
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem1.setTaxTypeId(tax1.getId());
        menuItem1.setPrice(10);
        menuItemRepository.save(menuItem1);
        printer1.setRestaurantId(restaurant1.getId());
        printer1.setIp("192.168.0.1");
        printerRepository.save(printer1);
        modifier1.setPrice(3);
        modifier1.setPriceOverride(3);
        modifier1.setTaxTypeId(tax1.getId());
        modifierRepository.save(modifier1);
    }

    @Test
    public void testPostPartyWithOrderAndAdjustmentsImmediatePrint() throws Exception {
        setUpItemsAndOrdersAndAdjustments();
        String token = getTokenForStaff(staff1);


        WaitingPartyOrderAndAdjustmentsPayload payload = new WaitingPartyOrderAndAdjustmentsPayload();
        WaitingPartyPayload waitingPartyPayload = createWaitingPartyPayload();
        payload.setParty(waitingPartyPayload);

        AdjustmentRequest request1 = createAdjustmentRequest(0.01);
        AdjustmentRequest request2 = createAdjustmentRequest(0.09);

        payload.getAdjustments().add(request1);
        payload.getAdjustments().add(request2);

        WaitingPartyOrderPayload.OrderPayload orderPayload = createOrderPayload();
        payload.getOrder().add(orderPayload);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("willAttemptImmediatePrint", true)
                .body(payload)
                .post("Waiting/PostWaitingWithOrderAndAdjustments");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        OrderResponse sessionPayload = response.getBody().as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(sessionPayload.getSessionId());
        List<Batch> batches = batchRepository.findBySessionId(session.getId());
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).isAwaitingImmediatePrint());
    }

    @Ignore
    @Test
    public void testPutParty() throws Exception {
        String token = getTokenForStaff(staff1);
    }

    @Test
    public void testDeleteParty() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .delete("Waiting/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        party1.setRestaurantId(restaurant1.getId());
        party1.setArrivedTime(System.currentTimeMillis());
        partyRepository.save(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .delete("Waiting/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(partyRepository.findOne(party1.getId()));

        int numberOfBlackMarks = customer1.getBlackMarks().size();
        party1.setCustomerId(customer1.getId());
        partyRepository.save(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .delete("Waiting/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(partyRepository.findOne(party1.getId()));
        assertEquals(numberOfBlackMarks, customerRepository.findOne(customer1.getId()).getBlackMarks().size());

        numberOfBlackMarks = customer1.getBlackMarks().size();
        party1.setCustomerId(customer1.getId());
        partyRepository.save(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .queryParam("withPrejudice", true)
                .delete("Waiting/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(partyRepository.findOne(party1.getId()));
        assertEquals(numberOfBlackMarks+1, customerRepository.findOne(customer1.getId()).getBlackMarks().size());

        checkIn1.setCustomerId(customer1.getId());
        checkIn1.setTime(System.currentTimeMillis());
        checkIn1.setRestaurantId(restaurant1.getId());
        checkInRepository.save(checkIn1);
        partyRepository.save(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .queryParam("withPrejudice", true)
                .delete("Waiting/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertNull(partyRepository.findOne(party1.getId()));
        assertNotNull(checkInRepository.findOne(checkIn1.getId()).getDeleted());

    }
}