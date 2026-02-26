package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import com.stripe.model.Charge;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.AsyncOrderHandlerService;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CashUpControllerTest extends SessionSetupBaseIT {
    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Before
    public void setUp() throws Exception {
        super.setUpSession();

        long currentTime = System.currentTimeMillis();
        long twelveMonthsAgo = currentTime - 31557600000L;
        cashUp1.setStartTime(twelveMonthsAgo);
        cashUp1.setEndTime(twelveMonthsAgo + (1000*60*60*24));
        cashUp2.setStartTime(currentTime - (1000*60*60*24*3));
        cashUp2.setEndTime(currentTime - (1000*60*60*24*2));
        cashUp3.setStartTime(currentTime - (1000*60*60*24*2));
        cashUp3.setEndTime(currentTime - (1000*60*60*24));
        cashUpRepository.save(cashUp1);
        cashUpRepository.save(cashUp2);
        cashUpRepository.save(cashUp3);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        session1.setStartTime(System.currentTimeMillis() - (1000*60*60));
        party1.setRestaurantId(session1.getRestaurantId());
        session1.setOriginalParty(party1);

        order1.setSessionId(session1.getId());
        batch1.setSessionId(session1.getId());
        batch1.setOrderIds(Collections.singleton(order1.getId()));

        order1 = orderRepository.save(order1);
        batch1 = batchRepository.save(batch1);
        party1 = partyRepository.save(party1);
        session1 = sessionRepository.save(session1);

        //whitebox PaymentSense
        PaymentSenseRestService mock = EasyMock.createMock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService,mock);
    }

    @Test
    public void testGetCashUps() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("CashUp");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CashUpResponse[] entity  = response.getBody().as(CashUpResponse[].class, ObjectMapperType.JACKSON_2);
        assertEquals(3, entity.length);
        for (CashUpResponse cashUpResponse : entity) {
            assertFalse(cashUpResponse.isWrapUp());
        }

        cashUpRepository.deleteAll();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("CashUp");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        entity  = response.getBody().as(CashUpResponse[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, entity.length);
    }

    @Test
    public void testIsCheckStatus() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("CashUp/CheckStatus");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("CashUp/CheckStatus");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testPostSimulate() throws Exception {
        String token = getTokenForStaff(staff1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        request.setEndTime(System.currentTimeMillis() / 1000);

        int nSessionsBefore = sessionRepository.findByRestaurantId(staff1.getRestaurantId()).size();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp/Simulate");

        CashUpResponse cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(request.getStartTime().longValue(), cashUpResponse.getStartTime());
        assertTrue(cashUpResponse.getReport().values().stream().anyMatch(d -> d>0));
        assertTrue(cashUpResponse.isWrapUp());
        assertEquals(nSessionsBefore, sessionRepository.findByRestaurantId(staff1.getRestaurantId()).size());

        request.setStartTime((cashUp3.getEndTime()-10000) / 1000);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp/Simulate");

        cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(request.getStartTime().longValue(), cashUpResponse.getStartTime());
        assertTrue(cashUpResponse.getReport().values().stream().anyMatch(d -> d>0));

        cashUpRepository.deleteAll();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        request.setEndTime(System.currentTimeMillis() / 1000);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp/Simulate");

        cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(restaurantRepository.findOne(staff1.getRestaurantId()).getCreationTime()/1000, cashUpResponse.getStartTime());
    }

    @Test
    public void testPostCashUp1() throws Exception {
        String token = getTokenForStaff(staff1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        long now = System.currentTimeMillis();
        request.setEndTime(now / 1000);

        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        CashUp cashUp = cashUpRepository.findAll().stream().filter(c -> c.getRestaurantId().equals(session1.getRestaurantId()) && c.getStartTime() == (request.getStartTime()*1000)).findFirst().get();
        assertEquals(0, cashUp.getSessionIds().size());
        cashUpRepository.delete(cashUp);

        session1.setClosedTime(now-1001);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        CashUpResponse cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(request.getStartTime().longValue(), cashUpResponse.getStartTime());
        assertTrue(cashUpResponse.getReport().values().stream().anyMatch(d -> d>0));
        assertFalse(cashUpResponse.isWrapUp());
        cashUp = cashUpRepository.findAll().stream().filter(c -> c.getRestaurantId().equals(session1.getRestaurantId()) && c.getStartTime() == (request.getStartTime()*1000)).findFirst().get();
        assertNotNull(cashUp);
        assertNull(partyRepository.findOne(party1.getId()));
        assertNull(orderRepository.findOne(order1.getId()));
        assertNull(batchRepository.findOne(batch1.getId()));
        cashUpRepository.delete(cashUp.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        cashUp = cashUpRepository.findAll().stream().filter(c -> c.getRestaurantId().equals(session1.getRestaurantId()) && c.getStartTime() == (request.getStartTime()*1000)).findFirst().get();
        assertEquals(0, cashUp.getSessionIds().size());

        Session test = sessionRepository.findOne(session1.getId());
        assertNull(test);

        List<Order> orders = liveDataService.getAllLiveOrders(session1.getId());
        assertEquals(0, orders.size());
        SessionArchive sessionArchive = sessionArchiveRepository.findBySessionId(session1.getId());
        assertNotNull(sessionArchive);
        assertEquals(3, sessionArchive.getOrders().size());
        assertEquals(order1, sessionArchive.getOrders().stream().filter(o-> o.getId().equals(order1.getId())).findFirst().orElse(null));
    }

    @Test
    public void testPostCashUp2() throws Exception {
        String token = getTokenForStaff(staff1);
        long now = System.currentTimeMillis();
        session1.setClosedTime(now-1001);
        sessionRepository.save(session1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()-10000) / 1000);
        request.setEndTime(now / 1000);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        CashUpResponse cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(request.getStartTime().longValue(), cashUpResponse.getStartTime());
        assertTrue(cashUpResponse.getReport().values().stream().anyMatch(d -> d>0));
        CashUp cashUp = cashUpRepository.findAll().stream().filter(c -> c.getRestaurantId().equals(session1.getRestaurantId()) && c.getStartTime() == (request.getStartTime()*1000)).findFirst().get();
        assertNotNull(cashUp);
        cashUpRepository.delete(cashUp.getId());
    }

    @Test
    public void testPostCashUp3() throws Exception {
        String token = getTokenForStaff(staff1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        request.setEndTime(System.currentTimeMillis() / 1000);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        CashUpResponse cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(request.getStartTime().longValue(), cashUpResponse.getStartTime());
        CashUp cashUp = cashUpRepository.findAll().stream().filter(c -> c.getRestaurantId().equals(session1.getRestaurantId()) && c.getStartTime() == (request.getStartTime()*1000)).findFirst().get();
        assertNotNull(cashUp);
    }

    @Test
    public void testPostCashUp4() throws Exception {
        String token = getTokenForStaff(staff1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        long now = System.currentTimeMillis();
        request.setEndTime(now / 1000);
        session1.setClosedTime(now-1001);
        session1.setRemoveFromReports(true);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        CashUpResponse cashUpResponse = response.getBody().as(CashUpResponse.class, ObjectMapperType.JACKSON_2);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(cashUpResponse);
        assertEquals(0D, cashUpResponse.getReport().get(CashUpKeys.SEATED_SESSIONS_COUNT), 0.1);
    }

    @Test
    public void testPostCashUpCausesPaymentAuth() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.capturePayment(anyObject())).andReturn(new Charge());
        replay(stripeService);

        String token = getTokenForStaff(staff1);

        CashUpRequest request = new CashUpRequest();
        request.setStartTime((cashUp3.getEndTime()+1) / 1000);
        long now = System.currentTimeMillis();
        request.setEndTime(now / 1000);
        session1.setClosedTime(now-1001);
        Adjustment adjustment = new Adjustment();
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(1);
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, new ChargeSummary());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("CashUp");

        verify(stripeService);
    }
}