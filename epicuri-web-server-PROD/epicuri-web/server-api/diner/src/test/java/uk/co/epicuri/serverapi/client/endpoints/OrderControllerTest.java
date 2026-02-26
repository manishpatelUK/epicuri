package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import com.stripe.model.Charge;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

public class OrderControllerTest extends TakeawayControllersTestsBase {

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Test
    public void testPutTakeawayCheck() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        String token = getToken();
        Response response = putTakeawayCheck(request, token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        CustomerReservationCheck customerReservationCheck = response.getBody().as(CustomerReservationCheck.class);
        assertEquals(10D, customerReservationCheck.getCost(), 0.01);
        assertEquals(0, customerReservationCheck.getWarning().size());
    }

    @Test
    public void testPutTakeawayCheckThrowsBadRequestOnBlackout() throws Exception {
        String token = getToken();

        long now = System.currentTimeMillis();
        addBlackout(now);

        long bookingTime = now + (60 * 1000 * 60 * 5);

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(bookingTime);

        Response response = putTakeawayCheck(request, token);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPostTakeawayThrowsNotAcceptable1() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setTelephone(null);

        String token = getToken();

        postTakeaway(request, token, HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void testPostTakeawayThrowsNotAcceptable2() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setAddress(null);
        request.setDelivery(true);

        String token = getToken();

        postTakeaway(request, token, HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void testPostTakeawayThrowsBadRequestOnCCCheck() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setPayByCC(true);

        String token = getToken();

        postTakeaway(request, token, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testTakeawayPaymentFailure() throws Exception {
        String token = getToken();

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setPayByCC(true);
        //add stripe
        KVData data = new KVData();
        data.setToken("acct_1BnDFDHwGlyz1eAl");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");
        restaurantRepository.save(restaurant1);
        customerLogin.setCcData(new CreditCardData());
        customerLogin.getCcData().setCcToken("tok_visa");
        customerRepository.save(customerLogin);

        bookingRepository.deleteAll();

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(50*30L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andThrow(new RuntimeException("Some exception"));
        replay(stripeService);

        postTakeaway(request, token, HttpStatus.BAD_REQUEST);
        verify(stripeService);

        assertEquals(0, sessionRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> s.getSessionType() == SessionType.TAKEAWAY).count());
        assertEquals(0, bookingRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> s.getBookingType() == BookingType.TAKEAWAY).count());

    }

    @Test
    public void testTakeawayGetsPaid() throws Exception {
        String token = getToken();

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setPayByCC(true);
        //add stripe
        KVData data = new KVData();
        data.setToken("acct_1BnDFDHwGlyz1eAl");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");
        restaurantRepository.save(restaurant1);
        customerLogin.setCcData(new CreditCardData());
        customerLogin.getCcData().setCcToken("tok_visa");
        customerRepository.save(customerLogin);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(50*30L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);


        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("/Order/Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        verify(stripeService);

        CustomerTakeawayResponseView customerTakeawayResponseView = response.as(CustomerTakeawayResponseView.class);
        Session session = sessionRepository.findAll().stream().filter(s -> s.getOriginalBookingId() != null && s.getOriginalBookingId().equals(customerTakeawayResponseView.getId())).findFirst().orElse(null);
        assertNotNull(session);
        assertEquals(1, session.getAdjustments().size());
        assertNotNull(session.getAdjustments().get(0).getSpecialAdjustmentData().get(StripeConstants.PAYMENT_KEY));
    }

    @Test
    public void testPostTakeaway() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());

        String token = getToken();

        postTakeaway(request, token, HttpStatus.OK);

        Session session = sessionRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> customerLogin.getId().equals(s.getOriginalBooking().getCustomerId())).findFirst().orElse(null);
        assertNotNull(session);
        assertTrue(session.getOriginalBooking().isAccepted());
    }

    @Test
    public void testDeleteTakeaway() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis() + (1000*60*60*6));
        String token = getToken();
        postTakeaway(request, token, HttpStatus.OK);

        Session session = sessionRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> customerLogin.getId().equals(s.getOriginalBooking().getCustomerId())).findFirst().orElse(null);
        String id = session.getOriginalBookingId();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", id)
                .delete("/Order/Takeaway/{id}");

        Booking booking = bookingRepository.findOne(id);
        assertTrue(booking.isCancelled());
        assertNotNull(sessionRepository.findOne(session.getId()).getDeleted());
    }

    @Test
    public void testDeleteTakeawayCancelsPreAuth() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis() + (1000*60*60*6));
        String token = getToken();
        postTakeaway(request, token, HttpStatus.OK);


        Session session = sessionRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> customerLogin.getId().equals(s.getOriginalBooking().getCustomerId())).findFirst().orElse(null);
        Adjustment adjustment = new Adjustment(session.getId());
        adjustment.setValue(10000);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, new ChargeSummary());
        session.getAdjustments().add(adjustment);
        sessionRepository.save(session);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session.getOriginalBookingId())
                .delete("/Order/Takeaway/{id}");

        verify(stripeService);
        session = sessionRepository.findOne(session.getId());
        assertTrue(session.getAdjustments().get(0).isVoided());
    }


}
