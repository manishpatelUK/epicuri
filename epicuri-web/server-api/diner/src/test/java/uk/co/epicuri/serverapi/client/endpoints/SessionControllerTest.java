package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import com.stripe.model.Charge;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.customer.*;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.BatchService;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SessionControllerTest extends MenuSetupBaseIT {

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private BatchService batchService;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        service1.setId(IDAble.generateId(restaurant1.getId()));
        service1.getCourses().clear();
        service1.getCourses().add(course1);
        course1.setId(IDAble.generateId(service1.getId()));
        service1.getCourses().add(course2);
        course2.setName(RestaurantConstants.IMMEDIATE_COURSE_NAME);
        course2.setId(IDAble.generateId(service1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        table1.setId(IDAble.generateId(restaurant1.getId()));
        adjustmentType1.setName(StripeConstants.STRIPE_PAYMENT_TYPE);
        adjustmentType1.setSupportsChange(false);
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType1 = adjustmentTypeRepository.save(adjustmentType1);
        restaurant1.getAdjustmentTypes().add(adjustmentType1.getId());
        adjustmentType2.setName(StripeConstants.STRIPE_GRATUITY_TYPE);
        adjustmentType2.setSupportsChange(false);
        adjustmentType2.setType(AdjustmentTypeType.GRATUITY);
        adjustmentType2 = adjustmentTypeRepository.save(adjustmentType2);
        restaurant1.getAdjustmentTypes().add(adjustmentType2.getId());
        restaurantRepository.save(restaurant1);

        session1.setRestaurantId(restaurant1.getId());
        session1.setService(service1);
        session1.setStartTime(System.currentTimeMillis());
        session1.getTables().add(table1.getId());
        session1.setSessionType(SessionType.SEATED);
        Diner defaultDiner = new Diner(session1);
        defaultDiner.setDefaultDiner(true);
        session1.getDiners().add(defaultDiner);
        Diner diner = new Diner(session1);
        diner.setCustomerId(customerLogin.getId());
        session1.getDiners().add(diner);
        sessionRepository.save(session1);

        CheckIn checkIn = new CheckIn();
        checkIn.setTime(System.currentTimeMillis());
        checkIn.setCustomerId(customerLogin.getId());
        checkIn.setRestaurantId(restaurant1.getId());
        checkIn.setSessionId(session1.getId());
        checkInRepository.save(checkIn);

        notificationRepository.deleteAll();

        menu1.setRestaurantId(restaurant1.getId());
        menu1.getCategories().clear();
        menu1.getCategories().add(category1);
        category1.getGroups().clear();
        category1.getGroups().add(group1);
        group1.getItems().clear();
        group1.getItems().add(menuItem1.getId());
        menuRepository.save(menu1);

        customerLogin.setFirstName("foo");
        customerLogin.setLastName("bar");
        customerRepository.save(customerLogin);

        orderRepository.deleteAll();
    }

    private String getToken() {
        return getTokenForCustomer(customerLogin);
    }

    @Test
    public void testPostServiceRequestNegatives() {
        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", "foo")
                .header(Params.AUTHORIZATION, token)
                .post("/Session/ServiceRequest/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        session1.setBillRequested(true);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/ServiceRequest/{id}");
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }

    @Test
    public void testPostServiceRequest() {
        String token = getToken();
        checkForNotificationCreation("ServiceRequest", token, NotificationConstant.TEXT_SERVICE_CALL);
    }

    @Test
    public void testPostBillRequest() {
        String token = getToken();
        checkForNotificationCreation("BillRequest", token, NotificationConstant.TEXT_BILL_REQUEST);
    }

    @Test
    public void testPostOrdersNegatives() {
        String token = getToken();

        SelfServiceRequest selfServiceRequest = new SelfServiceRequest();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(selfServiceRequest)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        CustomerOrderItemView customerOrderItemView = getCustomerOrderItemView(0);
        List<CustomerOrderItemView> list = new ArrayList<>();
        list.add(customerOrderItemView);
        selfServiceRequest.setOrders(list);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(selfServiceRequest)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        customerOrderItemView.setQuantity(3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", "foo")
                .body(selfServiceRequest)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        customerOrderItemView.setMenuItemId(menuItem2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(selfServiceRequest)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        session1.setBillRequested(true);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(selfServiceRequest)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPostOrders() {
        String token = getToken();

        CustomerOrderItemView customerOrderItemView = getCustomerOrderItemView(2);

        List<CustomerOrderItemView> list = new ArrayList<>();
        list.add(customerOrderItemView);
        SelfServiceRequest request = new SelfServiceRequest();
        request.setOrders(list);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        SelfServiceResponse selfServiceResponse = response.as(SelfServiceResponse.class);
        assertEquals(list.size(), selfServiceResponse.getOrderIds().size());
        assertEquals(list.size(), selfServiceResponse.getOrders().size());

        List<Order> orders = orderRepository.findBySessionId(session1.getId());
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(customerOrderItemView.getInstantiatedFromId(), order.getInstantiatedFrom().getId());
        assertEquals(customerOrderItemView.getMenuItemId(), order.getMenuItemId());
        assertEquals(customerOrderItemView.getQuantity(), order.getQuantity());
        assertEquals(customerOrderItemView.getNote(), order.getNote());
        assertEquals(customerOrderItemView.getModifiers(), order.getModifiers().stream().map(Modifier::getId).collect(Collectors.toList()));
        assertEquals(course2.getId(), order.getCourseId());
        List<Batch> batches = batchRepository.findBySessionId(session1.getId());
        assertEquals(1, batches.size());
        assertNull(order.getDeliveryLocation());
    }

    @Test
    public void testPostOrdersWithFailedPayment() throws Exception {
        String token = getToken();

        CustomerOrderItemView customerOrderItemView = getCustomerOrderItemView(3);
        List<CustomerOrderItemView> list = new ArrayList<>();
        list.add(customerOrderItemView);
        SelfServiceRequest request = new SelfServiceRequest();
        request.setOrders(list);
        request.setDeliveryLocation("up ya bum");

        CreditCardData ccData = setUpCreditCardAndStripe();

        session1.setSessionType(SessionType.TAB);
        sessionRepository.save(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(60L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andThrow(new RuntimeException("Some error"));
        replay(stripeService);

        request.setCcData(ccData);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        verify(stripeService);

        List<Order> orders = orderRepository.findBySessionId(session1.getId());
        assertEquals(0, orders.size());
    }

    @Test
    public void testPostOrdersWithPayment() throws Exception {
        String token = getToken();

        CustomerOrderItemView customerOrderItemView = getCustomerOrderItemView(3);
        List<CustomerOrderItemView> list = new ArrayList<>();
        list.add(customerOrderItemView);
        SelfServiceRequest request = new SelfServiceRequest();
        request.setOrders(list);
        request.setDeliveryLocation("up ya bum");

        CreditCardData ccData = setUpCreditCardAndStripe();

        session1.setSessionType(SessionType.TAB);
        sessionRepository.save(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(60L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);

        request.setCcData(ccData);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Order/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        verify(stripeService);
        SelfServiceResponse selfServiceResponse = response.as(SelfServiceResponse.class);
        assertEquals(5, selfServiceResponse.getPublicOrderId().length());
        List<Order> orders = orderRepository.findBySessionId(session1.getId());
        orders.forEach(o -> assertTrue(selfServiceResponse.getOrderIds().contains(o.getId())));
        orders.forEach(o -> assertEquals(request.getDeliveryLocation(), o.getDeliveryLocation()));

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertEquals(selfServiceResponse.getAmountPaid(), session.getAdjustments().get(0).getValue());
        assertTrue(session.getAdjustments().get(0).getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY));

        //ensure batches is created and has location for those orders
        List<Batch> batches = batchRepository.findBySessionId(session1.getId());
        assertEquals(1, batches.size());
        // update the printer to have an ip so the batch creation works
        Printer printer = masterDataService.getPrinter(batches.get(0).getPrinterId());
        printer.setIp("192.192.192.919");
        masterDataService.upsert(printer);
        List<HostBatchView> batchViews = batchService.getHostBatchViews(session1.getRestaurantId());
        assertEquals(1, batchViews.size());
        assertEquals("up ya bum", batchViews.get(0).getDeliveryLocation());
    }

    private CreditCardData setUpCreditCardAndStripe() {
        CreditCardData ccData = new CreditCardData();
        ccData.setCcToken("tok_visa");
        ccData.setDigits("342");
        ccData.setMonthExpiry("3wr4");
        ccData.setYearExpiry("32");

        customerLogin.setCcData(ccData);
        customerLogin = customerRepository.save(customerLogin);

        //add stripe
        KVData data = new KVData();
        data.setToken("acct_1BnDFDHwGlyz1eAl");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");
        restaurantRepository.save(restaurant1);
        return ccData;
    }

    private CustomerOrderItemView getCustomerOrderItemView(int quantity) {
        CustomerOrderItemView customerOrderItemView = new CustomerOrderItemView();
        customerOrderItemView.setInstantiatedFromId(ActivityInstantiationConstant.ANDROID.getId());
        customerOrderItemView.setMenuItemId(menuItem1.getId());
        customerOrderItemView.setQuantity(quantity);
        customerOrderItemView.setNote("foobar");
        customerOrderItemView.getModifiers().add(modifier1.getId());
        return customerOrderItemView;
    }

    @Test
    public void testGetSessionNegatives() {
        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", "foo")
                .header(Params.AUTHORIZATION, token)
                .get("/Session/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        session1.getDiners().get(1).setDefaultDiner(true);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .get("/Session/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        session1.getDiners().get(1).setDefaultDiner(false);
        session1.setService(null);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .get("/Session/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testGetSession() {
        String token = getToken();

        Order order = new Order();
        order.setMenuItemId(menuItem1.getId());
        order.setMenuItem(menuItem1);
        order.setSessionId(session1.getId());
        order.setDinerId(session1.getDiners().get(0).getId());
        order.setQuantity(1);
        order.setCourseId(course1.getId());
        order.setPriceOverride(menuItem1.getPrice());
        order.setModifiers(Collections.singletonList(modifier1));
        order.setInstantiatedFrom(ActivityInstantiationConstant.ANDROID);
        order = orderRepository.save(order);

        session1.setTipPercentage(10D);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .get("/Session/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CustomerSessionView customerSessionView = response.getBody().as(CustomerSessionView.class);

        assertSessionEquals(session1, customerSessionView, order);
    }

    @Test
    public void testPostTab() throws Exception{
        String token = getToken();
        createCheckin(token);

        assertEquals(1, checkInRepository.findAll().size());
        assertEquals(1, partyRepository.findAll().size());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", restaurant1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/tab/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CustomerTabCreationView customerSessionView = response.getBody().as(CustomerTabCreationView.class);

        Session session = sessionRepository.findOne(customerSessionView.getSessionId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(restaurant1.getId(), session.getRestaurantId());
        assertEquals(2, session.getDiners().size());
        assertEquals(null, session.getDiners().get(0).getCustomerId());
        assertTrue(session.getDiners().get(0).isDefaultDiner());
        assertEquals(customerLogin.getId(), session.getDiners().get(1).getCustomerId());

        assertEquals(1, checkInRepository.findAll().size());
        assertEquals(1, partyRepository.findAll().size());
        assertEquals(1, sessionRepository.findAll().size());
        CheckIn checkIn = checkInRepository.findOne(customerSessionView.getCheckInId());
        assertEquals(restaurant1.getId(), checkIn.getRestaurantId());
        assertEquals(customerLogin.getId(), checkIn.getCustomerId());
        assertEquals(session.getId(), checkIn.getSessionId());
        assertEquals(session.getOriginalPartyId(), checkIn.getPartyId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", restaurant1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/tab/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    public void createCheckin(String token) {
        checkInRepository.deleteAll();
        partyRepository.deleteAll();
        sessionRepository.deleteAll();

        restaurant1.getServices().get(0).setSessionType(SessionType.SEATED);
        restaurantRepository.save(restaurant1);

        CustomerCheckInView customerCheckInView = new CustomerCheckInView();
        customerCheckInView.setRestaurantId(restaurant1.getId());
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCheckInView)
                .post("/Checkin");
    }

    @Test
    public void testPostTabMultipleSessions() throws Exception{
        String token = getToken();
        createCheckin(token);

        List<String> list = new ArrayList<>();
        for(int i = 0; i < 50; i++) {
            list.add("");
        }
        list.parallelStream().forEach(x -> {
            given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .pathParam("id", restaurant1.getId())
                    .header(Params.AUTHORIZATION, token)
                    .post("/Session/tab/{id}");
        });

        assertEquals(1, sessionRepository.findAll().size());
        assertEquals(1, partyRepository.findAll().size());
        assertEquals(1, checkInRepository.findAll().size());
    }

    @Test
    public void testPostPaymentForSeatedSessionNegatives() throws Exception{
        String token = getToken();

        setUpOrderValues();
        CreditCardData ccData = setUpCreditCardAndStripe();

        PaymentRequestView request = new PaymentRequestView();
        request.setAmount(30);
        request.setTipAmount(100);
        request.setCcToken("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Payment/{id}/cc");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        request.setCcToken(ccData.getCcToken());
        customerLogin.getCcData().setCcToken("foo");
        customerRepository.save(customerLogin);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Payment/{id}/cc");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPostPaymentForSeatedSessionStripeThrowsAnError() throws Exception{
        String token = getToken();

        setUpOrderValues();
        CreditCardData ccData = setUpCreditCardAndStripe();

        PaymentRequestView request = new PaymentRequestView();
        request.setAmount(30);
        request.setTipAmount(100);
        request.setCcToken(ccData.getCcToken());

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(130L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andThrow(new RuntimeException("Some error"));
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Payment/{id}/cc");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        verify(stripeService);

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(0, session.getAdjustments().size());
    }

    @Test
    public void testPostPaymentForSeatedSession() throws Exception{
        String token = getToken();

        setUpOrderValues();
        CreditCardData ccData = setUpCreditCardAndStripe();

        PaymentRequestView request = new PaymentRequestView();
        request.setAmount(30);
        request.setTipAmount(100);
        request.setCcToken(ccData.getCcToken());

        createStripeMockAndDoPaymentCall(token, request);

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertEquals(30, session.getAdjustments().get(0).getValue());
        assertEquals(StripeConstants.STRIPE_PAYMENT_TYPE, session.getAdjustments().get(0).getAdjustmentType().getName());
        assertEquals(100, session.getAdjustments().get(1).getValue());
        assertEquals(StripeConstants.STRIPE_GRATUITY_TYPE, session.getAdjustments().get(1).getAdjustmentType().getName());
    }

    public void setUpOrderValues() {
        order1.setSessionId(session1.getId());
        order1.setMenuItem(menuItem1);
        order1.setPriceOverride(menuItem1.getPrice());
        order2.setSessionId(session1.getId());
        order2.setMenuItem(menuItem1);
        order2.setPriceOverride(menuItem1.getPrice());
        order3.setSessionId(session1.getId());
        order3.setMenuItem(menuItem1);
        order3.setPriceOverride(menuItem1.getPrice());
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
    }

    public void createStripeMockAndDoPaymentCall(String token, PaymentRequestView request) throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(1100L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .body(request)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/Payment/{id}/cc");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        verify(stripeService);
    }

    @Test
    public void testPostPaymentForSeatedSessionWithExtraTotalValue() throws Exception{
        String token = getToken();

        setUpOrderValues();
        CreditCardData ccData = setUpCreditCardAndStripe();

        PaymentRequestView request = new PaymentRequestView();
        request.setAmount(40);
        request.setTipAmount(100);
        request.setCcToken(ccData.getCcToken());

        createStripeMockAndDoPaymentCall(token, request);

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertEquals(30, session.getAdjustments().get(0).getValue());
        assertEquals(StripeConstants.STRIPE_PAYMENT_TYPE, session.getAdjustments().get(0).getAdjustmentType().getName());
        assertEquals(110, session.getAdjustments().get(1).getValue());
        assertEquals(StripeConstants.STRIPE_GRATUITY_TYPE, session.getAdjustments().get(1).getAdjustmentType().getName());
    }

    @Test
    public void testTabClosureWhenPaymentsDue() throws Exception {
        String token = getToken();
        setUpOrderValues();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/tab/close/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testTabClosureSeated() throws Exception {
        String token = getToken();
        setUpOrderValues();

        Adjustment adjustment = new Adjustment();
        adjustment.setValue(30);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/tab/close/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testTabClosure() throws Exception {
        String token = getToken();
        setUpOrderValues();

        Adjustment adjustment = new Adjustment();
        adjustment.setValue(30);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(adjustment);
        session1.setSessionType(SessionType.TAB);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .header(Params.AUTHORIZATION, token)
                .post("/Session/tab/close/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNotNull(sessionRepository.findOne(session1.getId()).getClosedTime());
    }

    private void assertSessionEquals(Session session, CustomerSessionView customerSessionView, Order order) {
        assertEquals(session.getId(), customerSessionView.getId());
        assertEquals(session.getService().getSelfServiceMenuId(), customerSessionView.getSelfServiceMenuId());
        assertEquals(session.getRestaurantId(), customerSessionView.getRestaurant().getId());
        assertNotNull(customerSessionView.getClosedMessage());
        assertNotNull(customerSessionView.getSocialMessage());
        assertEquals(session.getStartTime()/1000, customerSessionView.getTime());
        assertEquals(0, customerSessionView.getClosedTime());
        assertEquals(session.isBillRequested(), customerSessionView.isRequestedBill());
        assertEquals(session.getTables(), customerSessionView.getTables());
        assertEquals(1, customerSessionView.getOrders().size());
        assertEquals(0, customerSessionView.getTotalDiscount(), 0.001);
        assertEquals(10D, customerSessionView.getTipPercentage(), 0.001);

        //do orders
        CustomerOrderView customerOrderView = customerSessionView.getOrders().get(0);
        assertEquals(order.getId(), customerOrderView.getId());
        assertEquals(menuItem1.getId(), customerOrderView.getMenuItemId());
        assertEquals(menuItem1.getId(), customerOrderView.getItem().getId());
        assertEquals(menuItem1.getName(), customerOrderView.getItem().getName());
        assertEquals(menuItem1.getPrice(), customerOrderView.getItem().getPrice() * 100, 0.001);
        assertEquals(menuItem1.getDescription(), customerOrderView.getItem().getDescription());
        assertEquals(menuItem1.isUnavailable(), customerOrderView.getItem().isUnavailable());
        assertEquals(menuItem1.getModifierGroupIds(), customerOrderView.getItem().getModifierGroups());
        assertEquals(customerOrderView.getModifiers().size(), customerOrderView.getModifierDescriptions().size());
        if(customerOrderView.getModifiers().size() > 0) {
            assertEquals(customerOrderView.getModifiers().get(0), customerOrderView.getModifierDescriptions().get(0).getId());
        }

        //do diners
        assertEquals(2, customerSessionView.getDiners().size());
        Diner hostDefaultDiner = session.getDiners().get(0);
        Diner hostRealDiner = session.getDiners().get(1);
        CustomerDinerView customerDefaultDiner = customerSessionView.getDiners().get(0);
        CustomerDinerView customerRealDiner = customerSessionView.getDiners().get(1);

        assertEquals(hostDefaultDiner.getId(), customerDefaultDiner.getId());
        assertEquals(1, customerDefaultDiner.getOrders().size());
        assertEquals(order.getId(), customerDefaultDiner.getOrders().get(0).getId());

        assertEquals(hostRealDiner.getId(), customerRealDiner.getId());
        assertEquals(0, customerRealDiner.getSubTotal(), 0.001);
        assertEquals(0, customerRealDiner.getOrders().size());
        assertEquals(customerLogin.getFirstName(), customerRealDiner.getEpicuriUser().getName().getFirstName());
        assertEquals(customerLogin.getLastName(), customerRealDiner.getEpicuriUser().getName().getLastName());
    }

    private void checkForNotificationCreation(String endpoint, String token, NotificationConstant notificationConstant) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .pathParam("endpoint", endpoint)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/{endpoint}/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        List<Notification> notifications = notificationRepository.findByRestaurantId(restaurant1.getId());
        assertTrue(notifications.stream().anyMatch(n ->
                n.getSessionId().equals(session1.getId())
                && n.getNotificationType() == NotificationType.ADHOC
                && n.getTarget().equals(NotificationConstant.TARGET_WAITER_ACTION.getConstant())
                && n.getText().equals(notificationConstant.getConstant())));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", session1.getId())
                .pathParam("endpoint", endpoint)
                .header(Params.AUTHORIZATION, token)
                .post("/Session/{endpoint}/{id}");
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());
    }
}
