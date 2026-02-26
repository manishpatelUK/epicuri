package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import com.stripe.model.Charge;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.AsyncOrderHandlerService;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.SessionServiceTest;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class SessionControllerTest extends SessionSetupBaseIT {

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        //whitebox PaymentSense
        PaymentSenseRestService mock = EasyMock.createMock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService,mock);
    }

    @Test
    public void testGetAllSessions() throws Exception {
        setUpMixedSessions();

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Session/All");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostSessionView> list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(4, list.size());

        list.forEach(h -> assertTrue(h.getDiners().size() > 0));
    }

    @Test
    public void testHostView1() throws Exception {
        setUpSession();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        HostSessionView view = getHostSessionView(token);
        SessionServiceTest.assertEqual(view, session1, null);
    }

    @Test
    public void testHostView2() throws Exception {
        setUpSession();

        session1.setSessionType(SessionType.TAB);
        sessionRepository.save(session1);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        HostSessionView view = getHostSessionView(token);
        SessionServiceTest.assertEqual(view, session1, null);
    }

    @Test
    public void testHostView3() throws Exception {
        setUpSession();

        Booking booking = new Booking();
        booking.setTargetTime(session1.getStartTime());
        booking.setBookingType(BookingType.RESERVATION);
        booking.setRestaurantId(restaurant1.getId());
        booking = bookingRepository.insert(booking);
        session1.setOriginalBooking(booking);
        sessionRepository.save(session1);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        HostSessionView view = getHostSessionView(token);
        SessionServiceTest.assertEqual(view, session1, booking);
    }

    @Test
    public void testHostView4() throws Exception {
        setUpSession();

        Booking booking = new Booking();
        booking.setTargetTime(session1.getStartTime());
        booking.setBookingType(BookingType.TAKEAWAY);
        booking.setTakeawayType(TakeawayType.COLLECTION);
        booking.setRestaurantId(restaurant1.getId());
        booking.setCustomerId(customer1.getId());
        booking = bookingRepository.insert(booking);
        session1.setOriginalBooking(booking);
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setTakeawayType(TakeawayType.COLLECTION);
        Diner diner = session1.getDiners().get(0);
        diner.setDefaultDiner(false);
        Party party = session1.getOriginalParty();
        session1.setOriginalParty(party);
        session1.setOriginalPartyId(null);
        partyRepository.delete(party);
        session1.setDiners(Collections.singletonList(diner));
        sessionRepository.save(session1);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        HostSessionView view = getHostSessionView(token);
        SessionServiceTest.assertEqual(view, session1, booking);
    }

    private HostSessionView getHostSessionView(String token) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Session/All");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostSessionView> list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        return list.get(0);
    }

    @Test
    public void testHostViewWithBirthday() throws Exception {
        setUpSession();
        customer1.setBirthday(System.currentTimeMillis());
        customerRepository.save(customer1);
        session1.getFirstNonDefaultDiner().setCustomerId(customer1.getId());
        sessionRepository.save(session1);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        HostSessionView view = getHostSessionView(token);
        assertTrue(view.getDiners().stream().anyMatch(HostDinerView::isBirthday));
    }

    @Test
    public void testPostConvertAdHocToTab() throws Exception {
        convertAdhocToTab(3);
    }

    public void convertAdhocToTab(int numberOfDiners) {
        String token = getTokenForStaff(staff1);

        session1.setRestaurantId(restaurant1.getId());
        session1.setSessionType(SessionType.ADHOC);
        session1.setStartTime(10L);
        sessionRepository.save(session1);

        PartyUpdateRequest payload = new PartyUpdateRequest();
        payload.setSessionId(session1.getId());
        PartyUpdateRequest.Update update = new PartyUpdateRequest.Update();
        update.setName("Foobar");
        update.setNumberOfPeople(numberOfDiners);
        update.setServiceId(service1.getId());
        PartyUpdateRequest.CustomerIdView leadCustomer = new PartyUpdateRequest.CustomerIdView();
        leadCustomer.setId(customer1.getId());
        update.setLeadCustomer(leadCustomer);
        payload.setUpdate(update);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Session/ConvertAdHocToTab");

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(update.getNumberOfPeople()+1, session.getDiners().size());
        assertEquals(update.getName(), session.getName());
        if(numberOfDiners > 0) {
            assertTrue(session.getDiners().stream().anyMatch(d -> d.getCustomerId() != null && d.getCustomerId().equals(customer1.getId())));
        }
    }

    @Test
    public void testPostConvertAdHocToTab2() throws Exception {
        convertAdhocToTab(0);
    }


    @Test
    public void testGetAllSessionsNotInCashUp() throws Exception {
        setUpMixedSessions();
        long time = System.currentTimeMillis();
        session1.setStartTime(time-100);
        session2.setStartTime(time-100);
        session3.setStartTime(time-100);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Session/NotInCashup");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostSessionView> list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, list.size());

        session1.setClosedTime(time);
        session2.setClosedTime(time);
        session3.setClosedTime(time);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Session/NotInCashup");

        list = Arrays.asList(response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, list.size());
    }

    @Test
    public void testPutAccept() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setAccepted(false);
        bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1.setSessionType(SessionType.TAKEAWAY);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Accept/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertTrue(bookingRepository.findOne(booking1.getId()).isAccepted());
    }

    @Test
    public void testPutReject() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRejected(false);
        bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);

        StringMessage stringMessage = new StringMessage();
        stringMessage.setNotice("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(stringMessage)
                .put("Session/Reject/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        Booking booking = bookingRepository.findOne(booking1.getId());
        assertTrue(booking.isRejected());
        assertTrue(session.isRemoveFromReports());
        assertEquals(stringMessage.getNotice(), booking.getRejectionNotice());
    }

    @Test
    public void testPutRejectCancelsPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        booking1.setRejected(false);
        bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);

        addStripeAuthPaymentToSession(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        StringMessage stringMessage = new StringMessage();
        stringMessage.setNotice("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(stringMessage)
                .put("Session/Reject/{id}");

        verify(stripeService);
        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().get(0).isVoided());
    }

    public void addStripeAuthPaymentToSession(Session session) {
        AdjustmentType adjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT).findAny().orElse(null);

        Adjustment adjustment = new Adjustment(session.getId());
        adjustment.setValue(10000);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, new ChargeSummary());
        session.getAdjustments().add(adjustment);
        sessionRepository.save(session);
    }

    @Test
    public void testPutRemoveFromReports() throws Exception {
        String token = getTokenForStaff(staff1);

        session1.setClosedTime(System.currentTimeMillis());
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/RemoveFromReports/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        assertTrue(sessionRepository.findOne(session1.getId()).isRemoveFromReports());
    }

    @Test
    public void testGetSession() throws Exception {
        setUpSession();
        staff1.setRestaurantId(restaurant1.getId());
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .get("Session/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostSessionView view = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        SessionServiceTest.assertEqual(view, session1, table1, order1, order2, order3, diner1, diner2, diner3);
    }

    @Test
    public void testPutRequestBill() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/RequestBill/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertTrue(sessionRepository.findOne(session1.getId()).isBillRequested());
        assertNotNull(notificationRepository.findAll().stream().filter(n -> n.getSessionId().equals(session1.getId()) && n.getText().equals(NotificationConstant.TEXT_BILL_REQUEST.getConstant())).findFirst().orElse(null));
    }

    @Test
    public void testPutUnRequestBill() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setBillRequested(true);
        sessionRepository.save(session1);

        Notification notification = new Notification();
        notification.setNotificationType(NotificationType.ADHOC);
        notification.setRestaurantId(restaurant1.getId());
        notification.setSessionId(session1.getId());
        notification.setText(NotificationConstant.TEXT_BILL_REQUEST.getConstant());
        notificationRepository.save(notification);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/UnrequestBill/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertFalse(sessionRepository.findOne(session1.getId()).isBillRequested());
        assertNull(notificationRepository.findOne(notification.getId()));
    }

    @Test
    public void testPutVoid() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);

        order1.setSessionId(session1.getId());
        order2.setSessionId(session1.getId());
        order3.setSessionId(session1.getId());

        VoidReasonPayload payload = new VoidReasonPayload();
        payload.setReason("foo");
        payload.setStaff(new StaffView(staff1));
        payload.setVoidTime(System.currentTimeMillis());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Void/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getVoidReason());
        assertNotNull(session.getClosedTime());
        assertTrue(session.isRemoveFromReports());

        List<Order> orders = orderRepository.findBySessionId(session1.getId());
        assertEquals(3, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getId().equals(order1.getId()) || o.getId().equals(order2.getId()) || o.getId().equals(order3.getId())));
        assertTrue(orders.stream().allMatch(Order::isVoided));
    }

    @Test
    public void testPutVoidForceClose() throws Exception {
        String token = getTokenForStaff(staff1);
        String staffId = staff1.getId();

        setUpSession();
        sessionRepository.save(session1);

        VoidReasonPayload payload = new VoidReasonPayload();
        payload.setReason("foo");
        payload.setStaff(new StaffView(staff1));
        payload.setVoidTime(System.currentTimeMillis());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .queryParam("forceClose", true)
                .body(payload)
                .put("Session/Void/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getVoidReason());
        assertNotNull(session.getClosedTime());
        assertEquals(staffId, session.getClosedBy());
        assertFalse(session.isRemoveFromReports());
    }

    @Test
    public void testPutVoidForceCloseAdjustmentsAreVoided() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        Adjustment adjustment = new Adjustment();
        adjustment.setValue(100);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentTypeRepository.save(adjustmentType1);
        adjustment.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        VoidReasonPayload payload = new VoidReasonPayload();
        payload.setReason("foo");
        payload.setStaff(new StaffView(staff1));
        payload.setVoidTime(System.currentTimeMillis());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .queryParam("forceClose", true)
                .body(payload)
                .put("Session/Void/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertTrue(session.getAdjustments().get(0).isVoided());
    }

    @Test
    public void testPutVoidCancelsPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);

        addStripeAuthPaymentToSession(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        VoidReasonPayload payload = new VoidReasonPayload();
        payload.setReason("foo");
        payload.setStaff(new StaffView(staff1));
        payload.setVoidTime(System.currentTimeMillis());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Void/{id}");

        verify(stripeService);
        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().get(0).isVoided());
    }

    @Test
    public void testPutUnVoid() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setClosedTime(System.currentTimeMillis());
        session1.setVoidReason(new VoidReason());
        session1.setRemoveFromReports(true);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Unvoid/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertNull(session.getVoidReason());
        assertFalse(session.isRemoveFromReports());
    }

    @Test
    public void testPutUnVoidReinstatesPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setClosedTime(System.currentTimeMillis());
        session1.setVoidReason(new VoidReason());
        sessionRepository.save(session1);

        addStripeAuthPaymentToSession(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Unvoid/{id}");

        verify(stripeService);
        assertFalse(sessionRepository.findOne(session1.getId()).getAdjustments().get(0).isVoided());
    }

    private String getToken() {
        staff1.setRestaurantId(restaurant1.getId());
        return getTokenForStaff(staff1);
    }

    @Test
    public void testPutOpen() throws Exception {
        setUpSession();
        Party originalParty = party1;
        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        session1.setClosedTime(System.currentTimeMillis());
        session1.setCashUpId(cashUp1.getId());
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        session1.setCashUpId(null);
        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session saved = sessionRepository.findOne(session1.getId());

        assertNull(saved.getClosedTime());
        assertEquals(originalParty, partyRepository.findOne(originalParty.getId()));

        setUpSession();
        originalParty = party1;

        token = getToken();
        session1.setSessionType(SessionType.SEATED);
        session1.getTables().clear();
        session1.getTables().add(table1.getId());
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        saved = sessionRepository.findOne(session1.getId());

        assertNull(saved.getClosedTime());
        assertEquals(SessionType.SEATED, saved.getSessionType());
        assertEquals(1, saved.getTables().size());
        assertEquals(originalParty, partyRepository.findOne(originalParty.getId()));

        //occupy the table and then reopen
        setUpSession();
        originalParty = party1;

        token = getToken();
        session1.setSessionType(SessionType.SEATED);
        session1.getTables().clear();
        session1.getTables().add(table1.getId());
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);
        session2.getTables().clear();
        session2.getTables().add(table1.getId());
        session2.setClosedTime(null);
        session2.setRestaurantId(restaurant1.getId());
        session2.setSessionType(SessionType.SEATED);
        sessionRepository.save(session2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Open/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        saved = sessionRepository.findOne(session1.getId());

        assertNull(saved.getClosedTime());
        assertEquals(SessionType.TAB, saved.getSessionType());
        assertEquals(0, saved.getTables().size());
        assertEquals(originalParty, partyRepository.findOne(originalParty.getId()));
    }

    @Test
    public void testPutPayBill() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setOriginalPartyId(party1.getId());
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .put("Session/PayBill/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/PayBill/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        Adjustment payment = new Adjustment();
        payment.setAdjustmentType(adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT).findFirst().get());
        payment.setValue(10000000);
        payment.setStaffId(staff1.getId());
        payment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        session1.getAdjustments().add(payment);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/PayBill/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNull(partyRepository.findOne(party1.getId()));

        assertNotNull(response.getBody().as(HostSessionView.class));
        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getClosedTime());
        assertNotNull(session.getClosedBy());
        assertTrue(session.isMarkedAsPaid());
    }

    @Test
    public void testPutPayBillProcessesPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        session1.setOriginalPartyId(party1.getId());
        sessionRepository.save(session1);

        addStripeAuthPaymentToSession(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.capturePayment(anyObject())).andReturn(new Charge());
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/PayBill/{id}");

        verify(stripeService);
    }

    @Test
    public void testPutClose() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();

        CloseSessionRequest closeSessionRequest = new CloseSessionRequest();
        closeSessionRequest.setGiveBlackMark(false);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .body(closeSessionRequest)
                .put("Session/Close/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(closeSessionRequest)
                .put("Session/Close/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getClosedTime());
        assertNotNull(session.getClosedBy());
        assertNull(partyRepository.findOne(party1.getId()));

        setUpSession();
        session1.setClosedTime(null);
        session1.setClosedBy(null);
        session1.getDiners().get(1).setCustomerId(customer1.getId());
        sessionRepository.save(session1);

        assertEquals(0, customerRepository.findOne(customer1.getId()).getBlackMarks().size());
        closeSessionRequest.setGiveBlackMark(true);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(closeSessionRequest)
                .put("Session/Close/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getClosedTime());
        assertNotNull(session.getClosedBy());
        assertEquals(1, customerRepository.findOne(customer1.getId()).getBlackMarks().size());
    }

    @Test
    public void testPutCloseVoidsAdjustments() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();
        Adjustment adjustment = new Adjustment();
        adjustment.setValue(100);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentTypeRepository.save(adjustmentType1);
        adjustment.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        CloseSessionRequest closeSessionRequest = new CloseSessionRequest();
        closeSessionRequest.setGiveBlackMark(false);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(closeSessionRequest)
                .put("Session/Close/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertTrue(session.getAdjustments().get(0).isVoided());
    }

    @Test
    public void testPutCloseProcessesPreAuth() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpSession();

        CloseSessionRequest closeSessionRequest = new CloseSessionRequest();
        closeSessionRequest.setGiveBlackMark(false);

        addStripeAuthPaymentToSession(session1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.capturePayment(anyObject())).andReturn(new Charge());
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(closeSessionRequest)
                .put("Session/Close/{id}");

        verify(stripeService);
    }

    @Test
    public void testPostFromParty() throws Exception {
        String token = getTokenForStaff(staff1);

        SessionPayload sessionPayload = new SessionPayload();
        sessionPayload.setServiceId(service1.getId());
        sessionPayload.setAdHoc(false);
        sessionPayload.setTables(Collections.singletonList(table1.getId()));

        party1.setRestaurantId(restaurant1.getId());
        partyRepository.save(party1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .body(sessionPayload)
                .post("Session/FromParty/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        session1.setOriginalParty(party1);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .body(sessionPayload)
                .post("Session/FromParty/{id}");
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode());

        session1.setOriginalParty(null);
        session1.setOriginalPartyId(null);
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party1.getId())
                .body(sessionPayload)
                .post("Session/FromParty/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        SessionIdPojo sessionIdPojo = response.getBody().as(SessionIdPojo.class, ObjectMapperType.JACKSON_2);
        assertNotNull(sessionRepository.findOne(sessionIdPojo.getId()));
        assertNull(partyRepository.findOne(party1.getId()));

        //tab
        party2.setRestaurantId(restaurant1.getId());
        party2.setNumberOfPeople(3);
        partyRepository.save(party2);
        sessionPayload.setTables(new ArrayList<>());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", party2.getId())
                .body(sessionPayload)
                .post("Session/FromParty/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        sessionIdPojo = response.getBody().as(SessionIdPojo.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(sessionIdPojo.getId());
        assertNotNull(session);
        assertEquals(4, session.getDiners().size());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertNotNull(partyRepository.findOne(party2.getId()));
    }

    @Test
    public void testPutReopen() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .put("Session/Reopen/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Reopen/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertNull(session.getClosedTime());
        assertNull(session.getClosedBy());

        session1.setClosedTime(System.currentTimeMillis());
        session1.setClosedBy(staff1.getId());
        session1.setOriginalPartyId(party1.getId());
        sessionRepository.save(session1);
        archiveDataService.archiveParty(session1, party1);
        partyRepository.delete(party1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .put("Session/Reopen/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        session = sessionRepository.findOne(session1.getId());
        assertNull(session.getClosedTime());
        assertNull(session.getClosedBy());
        assertNotNull(partyRepository.findOne(party1.getId()));
    }

    @Test
    public void testPutChairs() throws Exception {
        String token = getTokenForStaff(staff1);

        Random random = new Random();

        List<ChairData> chairDataList = new ArrayList<>();
        for(int i = 0; i < 2; i++) {
            ChairData chairData = new ChairData();
            chairData.setBreadth(random.nextDouble());
            chairData.setDinerId(i == 0 ? diner1.getId() : diner2.getId());
            chairData.setRotation(random.nextDouble());
            chairData.setWidth(random.nextDouble());
            chairData.setX(random.nextDouble());
            chairData.setY(random.nextDouble());
            chairDataList.add(chairData);
        }

        ChairData[] array = chairDataList.toArray(new ChairData[2]);
        String json = ControllerUtil.OBJECT_MAPPER.writeValueAsString(array);
        ChairPayload payload = new ChairPayload();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .body(payload)
                .put("Session/Chairs/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Chairs/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        payload.setChairData(json);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Chairs/{id}");
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertTrue(Arrays.deepEquals(array, sessionRepository.findOne(session1.getId()).getChairData().toArray(new ChairData[2])));
    }

    @Ignore
    @Test
    public void testPutPriceOffset() throws Exception {

    }

    @Ignore
    @Test
    public void testPutPercentageOffset() throws Exception {

    }

    @Ignore
    @Test
    public void testPutAlterTip() throws Exception {

    }

    @Test
    public void testPutSetTip() throws Exception {
        String token = getTokenForStaff(staff1);

        TipPayload payload = new TipPayload();
        payload.setTip(16);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .body(payload)
                .put("Session/SetTip/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/SetTip/{id}");
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        assertEquals(payload.getTip(), sessionRepository.findOne(session1.getId()).getTipPercentage(), 0.01);
    }

    @Test
    public void testPutDelay() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpNotifications();
        DelayPayload payload = new DelayPayload();
        payload.setDelay(60); //seconds

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Delay/{id}");
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());


    }

    @Test
    public void testPutTablesChange() throws Exception {
        String token = getTokenForStaff(staff1);

        table3.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table3);
        restaurantRepository.save(restaurant1);

        SessionPayload payload = createTabToTablePayload();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foo")
                .body(payload)
                .put("Session/Tables/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Tables/{id}");
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        assertEquals(table3.getId(), sessionRepository.findOne(session1.getId()).getTables().get(0));
    }

    @Test
    public void testPutTablesTabToSeated() throws Exception {
        setUpTabSession();

        String token = getTokenForStaff(staff1);
        SessionPayload payload = createTabToTablePayload();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Tables/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(table3.getId(), session.getTables().get(0));
        assertEquals(SessionType.SEATED, session.getSessionType());
        assertNull(partyRepository.findOne(party1.getId()));
    }

    private SessionPayload createTabToTablePayload() {
        SessionPayload payload = new SessionPayload();
        payload.setAdHoc(false);
        payload.setServiceId(service1.getId());
        payload.setTables(Lists.newArrayList(table3.getId()));
        return payload;
    }

    private void setUpTabSession() {
        session1.setSessionType(SessionType.TAB);
        party1.setRestaurantId(restaurant1.getId());
        session1.setOriginalParty(party1);
        sessionRepository.save(session1);
        partyRepository.save(party1);

        table3.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table3);
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testPutTablesTabToSeatedTabIsSet() throws Exception {
        restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.COVERS_BEFORE_AUTOTIP)).findFirst().orElse(null).setValue(1);
        restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.DEFAULT_TIP_PERCENTAGE)).findFirst().orElse(null).setValue(10);
        restaurantRepository.save(restaurant1);

        setUpTabSession();
        session1.getDiners().add(new Diner(session1));
        session1.getDiners().add(new Diner(session1));
        sessionRepository.save(session1);

        assertNull(sessionRepository.findOne(session1.getId()).getTipPercentage());

        String token = getTokenForStaff(staff1);
        SessionPayload payload = createTabToTablePayload();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Tables/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(10, session.getTipPercentage(), 0.001);
    }

    @Test
    public void testPutTablesSeatedToTabWithArchivedParty() throws Exception {
        session1.setSessionType(SessionType.SEATED);
        session1.setRestaurantId(restaurant1.getId());
        party1.setRestaurantId(restaurant1.getId());
        session1.setOriginalParty(party1);
        table3.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table3);
        restaurantRepository.save(restaurant1);
        session1.getTables().add(table3.getId());
        sessionRepository.save(session1);
        partyRepository.save(party1);

        sessionService.clear(session1,false,false,false,true, true);

        Response response = putTables();

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(0, session.getTables().size());
        assertEquals(party1.getId(), session.getOriginalPartyId());
        assertNotNull(partyRepository.findOne(party1.getId()));
    }

    @Test
    public void testPutTablesSeatedToTabWithLiveParty() throws Exception {
        setUpSeatedSession();

        Response response = putTables();

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(0, session.getTables().size());
        assertEquals(party1.getId(), session.getOriginalPartyId());
        assertNotNull(partyRepository.findOne(party1.getId()));
    }

    private void setUpSeatedSession() {
        session1.setSessionType(SessionType.SEATED);
        party1.setRestaurantId(restaurant1.getId());
        session1.setOriginalParty(party1);
        table3.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table3);
        restaurantRepository.save(restaurant1);
        session1.getTables().add(table3.getId());
        sessionRepository.save(session1);
        partyRepository.save(party1);
    }

    @Test
    public void testPutTablesSeatedToTabAutotipSetEquals0() throws Exception {
        setUpSeatedSession();
        Response response = putTables();
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        assertNull(sessionRepository.findOne(session1.getId()).getTipPercentage());
    }

    @Test
    public void testPutTablesSeatedToTabAutotipSetNotEquals0() throws Exception {
        setUpSeatedSession();
        session1.setTipPercentage(10D);
        sessionRepository.save(session1);

        Response response = putTables();
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        assertEquals(10, sessionRepository.findOne(session1.getId()).getTipPercentage(), 0.01);
    }

    @Test
    public void testPutTablesSeatedToTabWithNoParty() throws Exception {
        session1.setSessionType(SessionType.SEATED);
        table3.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table3);
        restaurantRepository.save(restaurant1);
        session1.getTables().add(table3.getId());
        sessionRepository.save(session1);

        Response response = putTables();

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(SessionType.TAB, session.getSessionType());
        assertEquals(0, session.getTables().size());
        assertNotNull(session.getOriginalPartyId());
        assertNotNull(partyRepository.findOne(session.getOriginalPartyId()));
    }

    @Test
    public void testPostSplitSession() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpSession();
        restaurant1.getServices().get(0).setDefaultService(true);
        restaurantRepository.save(restaurant1);

        SessionSplitView sessionSplitView = new SessionSplitView();
        sessionSplitView.setSessionType(SessionType.SEATED);
        sessionSplitView.getOrderIds().add(order1.getId());
        sessionSplitView.getOrderIds().add(order3.getId());
        sessionSplitView.getOrderIds().add("foo");

        Response response = postSplit(token, sessionSplitView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        sessionSplitView.setSessionType(SessionType.TAB);
        response = postSplit(token, sessionSplitView);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        IdPojo idPojo = response.getBody().as(IdPojo.class);
        Session session = sessionRepository.findOne(idPojo.getId());

        order1 = orderRepository.findOne(this.order1.getId());
        assertEquals(session.getId(), order1.getSessionId());
        assertEquals(session.getDiners().get(0).getId(), order1.getDinerId());
        order3 = orderRepository.findOne(this.order3.getId());
        assertEquals(session.getId(), order3.getSessionId());
        assertEquals(session.getDiners().get(0).getId(), order3.getDinerId());
        assertNotEquals(session.getId(), orderRepository.findOne(order2.getId()).getSessionId());
        assertNotEquals(session.getId(), session1.getId());
        assertNotNull(session.getOriginalParty());
        assertTrue(session.getName().endsWith("[Split 1]"));

        //another split should cause [Split 2]
        sessionSplitView.getOrderIds().clear();
        sessionSplitView.getOrderIds().add(order2.getId());
        response = postSplit(token, sessionSplitView);
        idPojo = response.getBody().as(IdPojo.class);
        session = sessionRepository.findOne(idPojo.getId());
        assertTrue(session.getName().endsWith("[Split 2]"));

        //another split should cause [Split 3]
        sessionSplitView.getOrderIds().clear();
        response = postSplit(token, sessionSplitView);
        idPojo = response.getBody().as(IdPojo.class);
        session = sessionRepository.findOne(idPojo.getId());
        assertTrue(session.getName().endsWith("[Split 3]"));
    }

    public Response postSplit(String token, SessionSplitView sessionSplitView) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(sessionSplitView)
                .post("Session/split/{id}");
    }

    @Test
    public void testPutSession() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpSession();
        int nDiners = session1.getDiners().size();

        HostPartyChangeRequest request = new HostPartyChangeRequest();
        request.setNumberOfDiners(5);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(request)
                .put("Session/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(5, session.getNumberOfRealDiners());
        assertEquals(session1.getName(), session.getName());
        Party party = session.getOriginalParty();
        assertEquals(5, party.getNumberOfPeople());

        request.setName("foomanchu");
        request.setNumberOfDiners(10);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(request)
                .put("Session/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        session = sessionRepository.findOne(session1.getId());
        assertEquals(10, session.getNumberOfRealDiners());
        assertEquals("foomanchu", session.getName());
        party = session.getOriginalParty();
        assertEquals("foomanchu", party.getName());

        request.setName("foomanchu");
        request.setNumberOfDiners(10);


    }

    private Response putTables() {
        String token = getTokenForStaff(staff1);
        SessionPayload payload = new SessionPayload();
        payload.setAdHoc(false);
        payload.setServiceId(service1.getId());

        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(payload)
                .put("Session/Tables/{id}");
    }

    @Ignore
    @Test
    public void testPostAcknowledge() throws Exception {

    }

    @Ignore
    @Test
    public void testPostAcknowledgeAdhoc() throws Exception {

    }

    @Test
    public void testPutCourseAwaySent() throws Exception {
        String token = getTokenForStaff(staff1);
        Course course = new Course(service1);
        service1.getCourses().add(course);
        restaurantRepository.save(restaurant1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(new IdPojo(null))
                .put("Session/{id}/courseAwaySent");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .body(new IdPojo(course.getId()))
                .put("Session/{id}/courseAwaySent");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getCourseAwayMessagesSent().size());
        assertEquals(1, session.getCourseAwayMessagesSent().get(course.getId()).intValue());
    }
}
