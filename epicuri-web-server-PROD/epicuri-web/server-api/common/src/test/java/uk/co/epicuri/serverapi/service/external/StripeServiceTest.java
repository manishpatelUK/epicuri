package uk.co.epicuri.serverapi.service.external;

import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static org.junit.Assert.*;

public class StripeServiceTest extends BaseIT {

    @Autowired
    private StripeService stripeService;

    @Test
    public void testChargeWhenNoIntegration() throws Exception{
        boolean exceptionThrown = false;
        try {
            Charge charge = stripeService.charge(restaurant1, "tok_visa", 100, true, false);
        } catch(IllegalArgumentException ex) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testCharge() throws Exception{
        KVData data = new KVData();
        data.setToken("acct_1BrVdYDf1zH7p7nC");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");

        Charge charge = stripeService.charge(restaurant1, "tok_visa", 100,true, false);
        assertNotNull(charge);
        assertTrue(charge.getCaptured());
    }

    @Test
    public void testCapturePayment() throws Exception {
        KVData data = new KVData();
        data.setToken("acct_1BrVdYDf1zH7p7nC");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");

        Charge charge = stripeService.charge(restaurant1, "tok_visa", 100,false, false);

        assertFalse(charge.getCaptured());

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, StripeService.createSummary(charge));

        charge = stripeService.capturePayment(adjustment);
        assertTrue(charge.getCaptured());
    }

    @Test
    public void testHasPreAuth() throws Exception {
        KVData data = new KVData();
        data.setToken("acct_1BrVdYDf1zH7p7nC");
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, data);
        restaurant1.setISOCurrency("usd");

        Charge charge = stripeService.charge(restaurant1, "tok_visa", 100,false, false);

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, StripeService.createSummary(charge));

        assertTrue(stripeService.hasPreAuth(adjustment));
    }

    @Test
    public void testAcquire() throws Exception {
        Customer customer = stripeService.acquireCustomer("tok_visa", null, "test");
        assertNotNull(customer);
    }

    @Ignore
    @Test
    public void testCreateAccount() throws Exception {
        Account account = stripeService.createAccount("US", "test@epicuri.co.uk", "custom");
        assertNotNull(account);
    }
}