package uk.co.epicuri.serverapi.service;

import com.stripe.model.Charge;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SessionPaymentServiceTest extends BaseIT{

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

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
    }

    @Test
    public void processPayment() throws Exception{
        setUpOrder(order1, session1);
        setUpOrder(order2, session1);
        setUpOrder(order3, session1);

        CreditCardData ccData = new CreditCardData();
        ccData.setCcToken("foo");

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);

        Charge charge = new Charge();
        charge.setAmount(1100L);
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);
        List<Adjustment> adjustmentList = sessionPaymentService.processPayment(session1, ccData, 1000, 100, restaurant1, adjustmentType1, adjustmentType2, true);
        verify(stripeService);

        assertEquals(2, adjustmentList.size());
        assertEquals(StripeConstants.STRIPE_PAYMENT_TYPE, adjustmentList.get(0).getAdjustmentType().getName());
        assertEquals(1000, adjustmentList.get(0).getValue());
        assertTrue(adjustmentList.get(0).getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY));
        assertEquals(StripeConstants.STRIPE_GRATUITY_TYPE, adjustmentList.get(1).getAdjustmentType().getName());
        assertEquals(100, adjustmentList.get(1).getValue());
        assertTrue(adjustmentList.get(1).getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY));
    }

    @Test
    public void testEnsurePreAuthsAreProcessed_NoCaptureWhenNoAdjustments() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        replay(stripeService);

        session1.getAdjustments().clear();
        sessionPaymentService.ensurePreAuthsAreProcessed(session1);

        verify(stripeService);
    }

    @Test
    public void testEnsurePreAuthsAreProcessed_NoCaptureWhenNoStripeAdjustments() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        replay(stripeService);

        Adjustment adjustment = createAdjustment(false);
        adjustment.getSpecialAdjustmentData().clear();
        sessionPaymentService.ensurePreAuthsAreProcessed(session1);

        verify(stripeService);
    }

    @Test
    public void testEnsurePreAuthsAreProcessed_NoCaptureWhenStripeAdjustmentIsVoided() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        replay(stripeService);

        Adjustment adjustment = createAdjustment(true);
        sessionPaymentService.ensurePreAuthsAreProcessed(session1);

        verify(stripeService);
    }

    @Test
    public void testEnsurePreAuthsAreProcessed() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.capturePayment(anyObject())).andReturn(new Charge());
        replay(stripeService);

        Adjustment adjustment = createAdjustment(false);
        sessionPaymentService.ensurePreAuthsAreProcessed(session1);

        verify(stripeService);
        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().stream().anyMatch(a -> a.getId().equals(adjustment.getId())));
    }

    @Test
    public void testEnsureAuthsCancelled() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.hasPreAuth(anyObject())).andReturn(true);
        replay(stripeService);

        Adjustment adjustment = createAdjustment(false);
        sessionPaymentService.ensurePreAuthsAreCancelled(session1);

        verify(stripeService);
        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().stream().filter(a -> a.getId().equals(adjustment.getId())).findFirst().orElse(null).isVoided());
    }

    @Test
    public void testEnsureAuthsCancelled_NotWhenVoided() throws Exception {
        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        replay(stripeService);

        Adjustment adjustment = createAdjustment(true);
        sessionPaymentService.ensurePreAuthsAreCancelled(session1);

        verify(stripeService);
        assertTrue(sessionRepository.findOne(session1.getId()).getAdjustments().stream().filter(a -> a.getId().equals(adjustment.getId())).findFirst().orElse(null).isVoided());
    }

    private Adjustment createAdjustment(boolean voided) {
        session1.getAdjustments().clear();
        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(1);
        adjustment.setVoided(voided);
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, new ChargeSummary());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);
        return adjustment;
    }

    private void setUpOrder(Order order, Session session) {
        order.setSessionId(session.getId());
        session.setRestaurantId(restaurant1.getId());

        orderRepository.save(order);
        sessionRepository.save(session);
    }
}