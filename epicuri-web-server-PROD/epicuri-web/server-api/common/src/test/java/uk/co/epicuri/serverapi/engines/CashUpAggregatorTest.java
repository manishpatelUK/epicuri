package uk.co.epicuri.serverapi.engines;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CashUpAggregatorTest extends SessionSetupBaseIT {

    protected CashUpAggregator aggregator;

    protected void setUpAggregator() throws Exception{
        super.setUpSession();

        aggregator = new CashUpAggregator(sessionCalculationService);
    }

    @Test
    public void testGetSessionIds() throws Exception {
        setUpAggregator();

        session2.setRestaurantId(restaurant1.getId());
        session3.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session2);
        sessionRepository.save(session3);
        aggregator.addSession(session1, new ArrayList<>());
        aggregator.addSession(session2, new ArrayList<>());

        List<String> sessionIds = aggregator.getSessionIds();
        assertEquals(2, sessionIds.size());

        aggregator.addSession(session3, new ArrayList<>());
        sessionIds = aggregator.getSessionIds();
        assertEquals(3, sessionIds.size());
        aggregator.addSession(session3, new ArrayList<>());
        sessionIds = aggregator.getSessionIds();
        assertEquals(3, sessionIds.size());

        assertTrue(sessionIds.contains(session1.getId()));
        assertTrue(sessionIds.contains(session2.getId()));
        assertTrue(sessionIds.contains(session3.getId()));
    }

    @Ignore("Simple add")
    @Test
    public void testAddSession() throws Exception {

    }

    @Test
    public void testAggregateCreatesValues() throws Exception {
        setUpAggregator();

        aggregator.aggregate();
        assertTrue(aggregator.getReportValues().size() > 0);
        for(Integer integer : aggregator.getReportValues().values()) {
            assertEquals(0, integer.intValue());
        }
    }

    @Test
    public void testAggregateBasicValuesCalculated() throws Exception {
        setUpAggregator();

        session2.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session2);
        addOrders(session1);
        aggregator.aggregate();

        int expectedRevenueTotal = 366;
        int expectedFoodRevenueTotal = 266;
        int expectedDrinkRevenueTotal = 10;
        int expectedOtherRevenueTotal = 90;
        int expectedVATTotal = 58;
        int expectedFoodVAT = 45;
        int expectedDrinkVAT = 0;
        int expectedOtherVAT = 13;

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(1, reportValues.get(CashUpKeys.SEATED_SESSIONS_COUNT).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.SEATED_SESSIONS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE).intValue());
        assertEquals(session1.getDiners().size()-1, reportValues.get(CashUpKeys.COVERS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT).intValue());
        assertEquals(expectedFoodRevenueTotal, reportValues.get(CashUpKeys.FOOD_VALUE).intValue());
        assertEquals(expectedFoodVAT, reportValues.get(CashUpKeys.FOOD_VAT).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.FOOD_COUNT).intValue());
        assertEquals(expectedDrinkRevenueTotal, reportValues.get(CashUpKeys.DRINK_VALUE).intValue());
        assertEquals(expectedDrinkVAT, reportValues.get(CashUpKeys.DRINK_VAT).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.DRINK_COUNT).intValue());
        assertEquals(expectedOtherRevenueTotal, reportValues.get(CashUpKeys.OTHER_VALUE).intValue());
        assertEquals(expectedOtherVAT, reportValues.get(CashUpKeys.OTHER_VAT).intValue());
        assertEquals(3, reportValues.get(CashUpKeys.OTHER_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.GUESTS).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(expectedVATTotal, reportValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals(expectedRevenueTotal - expectedVATTotal, reportValues.get(CashUpKeys.NET_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_DELIVERY).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.TOTAL_SALES).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());

        for(Integer value : aggregator.getRefundValues().values()) {
            assertEquals(0, value.intValue());
        }

        for(Integer value : aggregator.getRefundPaymentValues().values()) {
            assertEquals(0, value.intValue());
        }
    }

    @Test
    public void testAggregateWhenSessionVoidAndClosed() throws Exception {
        setUpAggregator();
        session1.setVoidReason(new VoidReason());
        session1.setClosedTime(0L);
        addOrders(session1);
        aggregator.aggregate();

        int expectedRevenueTotal = 366;

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(0, reportValues.get(CashUpKeys.SEATED_SESSIONS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.SEATED_SESSIONS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE).intValue());
        assertEquals(session1.getDiners().size()-1, reportValues.get(CashUpKeys.COVERS_COUNT).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.VOID_COUNT).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.VOID_VALUE).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_VALUE).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.GUESTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.NET_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_DELIVERY).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_SALES).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());
    }

    @Test
    public void testAggregateWithRefund1() throws Exception {
        setUpAggregator();

        session2.setRestaurantId(session1.getRestaurantId());
        session2.setSessionType(SessionType.REFUND);
        sessionRepository.save(session2);
        addOrders(session1);
        addOrders(session2);
        aggregator.aggregate();

        int expectedRevenueTotal = 366;
        int expectedFoodRevenueTotal = 266;
        int expectedDrinkRevenueTotal = 10;
        int expectedOtherRevenueTotal = 90;
        int expectedVATTotal = 58;
        int expectedFoodVAT = 45;
        int expectedDrinkVAT = 0;
        int expectedOtherVAT = 13;

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(1, reportValues.get(CashUpKeys.SEATED_SESSIONS_COUNT).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.TOTAL_SALES).intValue());

        assertEquals(expectedFoodRevenueTotal, reportValues.get(CashUpKeys.FOOD_VALUE).intValue());
        assertEquals(expectedFoodVAT, reportValues.get(CashUpKeys.FOOD_VAT).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.FOOD_COUNT).intValue());
        assertEquals(expectedDrinkRevenueTotal, reportValues.get(CashUpKeys.DRINK_VALUE).intValue());
        assertEquals(expectedDrinkVAT, reportValues.get(CashUpKeys.DRINK_VAT).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.DRINK_COUNT).intValue());
        assertEquals(expectedOtherRevenueTotal, reportValues.get(CashUpKeys.OTHER_VALUE).intValue());
        assertEquals(expectedOtherVAT, reportValues.get(CashUpKeys.OTHER_VAT).intValue());
        assertEquals(3, reportValues.get(CashUpKeys.OTHER_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.NET_VALUE).intValue());

    }

    @Test
    public void testAggregateWithRefund2() throws Exception {
        setUpAggregator();

        session1.setSessionType(SessionType.REFUND);
        sessionRepository.save(session1);
        addOrders(session1);
        aggregator.aggregate();

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(1, aggregator.getSessionIds().size());
        assertEquals(session1.getId(), aggregator.getSessionIds().get(0));

        int expectedRevenueTotal = -366;
        int expectedFoodRevenueTotal = -266;
        int expectedDrinkRevenueTotal = -10;
        int expectedOtherRevenueTotal = -90;
        int expectedVATTotal = -58;
        int expectedFoodVAT = -45;
        int expectedDrinkVAT = 0;
        int expectedOtherVAT = -13;

        assertEquals(0, reportValues.get(CashUpKeys.SEATED_SESSIONS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.SEATED_SESSIONS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.COVERS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.FOOD_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.DRINK_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_VAT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OTHER_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.GUESTS).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(expectedVATTotal, reportValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals(expectedRevenueTotal - expectedVATTotal, reportValues.get(CashUpKeys.NET_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_DELIVERY).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_SALES).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());

        Map<String, Integer> refundValues = aggregator.getRefundValues();
        assertEquals(0, refundValues.get(CashUpKeys.VOID_REFUND_SESSION_COUNT).intValue());
        assertEquals(0, refundValues.get(CashUpKeys.VOID_REFUND_SESSION_VALUE).intValue());
        assertEquals(1, refundValues.get(CashUpKeys.REFUND_SESSIONS_COUNT).intValue());
        assertEquals(expectedRevenueTotal, refundValues.get(CashUpKeys.REFUND_SESSIONS_VALUE).intValue());
        assertEquals(expectedFoodRevenueTotal, refundValues.get(CashUpKeys.FOOD_REFUND_VALUE).intValue());
        assertEquals(expectedFoodVAT, refundValues.get(CashUpKeys.FOOD_REFUND_VAT).intValue());
        assertEquals(2, refundValues.get(CashUpKeys.FOOD_REFUND_COUNT).intValue());
        assertEquals(expectedDrinkRevenueTotal, refundValues.get(CashUpKeys.DRINK_REFUND_VALUE).intValue());
        assertEquals(expectedDrinkVAT, refundValues.get(CashUpKeys.DRINK_REFUND_VAT).intValue());
        assertEquals(1, refundValues.get(CashUpKeys.DRINK_REFUND_COUNT).intValue());
        assertEquals(expectedOtherRevenueTotal, refundValues.get(CashUpKeys.OTHER_REFUND_VALUE).intValue());
        assertEquals(expectedOtherVAT, refundValues.get(CashUpKeys.OTHER_REFUND_VAT).intValue());
        assertEquals(3, refundValues.get(CashUpKeys.OTHER_REFUND_COUNT).intValue());
        assertEquals(expectedRevenueTotal, refundValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(expectedVATTotal, refundValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals(expectedRevenueTotal - expectedVATTotal, refundValues.get(CashUpKeys.NET_VALUE).intValue());

        assertEquals(0, refundValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, aggregator.getRefundPaymentValues().size());
    }

    @Test
    public void testAggregateWithRefundWithPayments() throws Exception {
        setUpAggregator();
        int expectedRevenueTotal = 366;
        Adjustment adjustment1 = addAdjustment(expectedRevenueTotal, session1);

        session1.setSessionType(SessionType.REFUND);
        sessionRepository.save(session1);
        addOrders(session1);
        aggregator.aggregate();

        Map<String, Integer> reportValues = aggregator.getReportValues();
        assertEquals(-expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        Map<String, Integer> refundValues = aggregator.getRefundValues();
        assertEquals(-expectedRevenueTotal, refundValues.get(CashUpKeys.PAYMENTS).intValue());
        Map<String, Integer> refundPaymentValues = aggregator.getRefundPaymentValues();
        assertEquals(1, refundPaymentValues.size());
        assertEquals(-expectedRevenueTotal, refundPaymentValues.get(adjustment1.getAdjustmentType().getName()).intValue());
    }

    @Test
    public void testAggregateWithRefundWithVoid() throws Exception {
        setUpAggregator();
        int expectedRevenueTotal = 366;

        Adjustment adjustment1 = addAdjustment(expectedRevenueTotal, session1);
        session1.setSessionType(SessionType.REFUND);
        sessionRepository.save(session1);
        session1.setVoidReason(new VoidReason());
        session1.setClosedTime(0L);
        addOrders(session1);
        aggregator.aggregate();

        Map<String, Integer> reportValues = aggregator.getReportValues();
        Map<String, Integer> refundValues = aggregator.getRefundValues();
        Map<String, Integer> refundPaymentValues = aggregator.getRefundPaymentValues();
        assertEquals(0, reportValues.get(CashUpKeys.VOID_COUNT).intValue());
        assertEquals(1, refundValues.get(CashUpKeys.VOID_REFUND_SESSION_COUNT).intValue());
        assertEquals(expectedRevenueTotal, refundValues.get(CashUpKeys.VOID_REFUND_SESSION_VALUE).intValue());
        assertEquals(1, refundPaymentValues.size());
        assertEquals(0, refundPaymentValues.get(adjustment1.getAdjustmentType().getName()).intValue());
    }

    @Test
    public void testAggregateWithTakeawaySessions() throws Exception {
        setUpAggregator();
        addOrders(session1);
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.COLLECTION);
        addOrders(session2);

        int expectedRevenueTotal = 366;
        int expectedFoodRevenueTotal = 266;
        int expectedDrinkRevenueTotal = 10;
        int expectedOtherRevenueTotal = 90;
        int expectedVATTotal = 58;
        int expectedFoodVAT = 45;
        int expectedDrinkVAT = 0;
        int expectedOtherVAT = 13;

        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(1, reportValues.get(CashUpKeys.SEATED_SESSIONS_COUNT).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.SEATED_SESSIONS_VALUE).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE).intValue());
        assertEquals(session1.getDiners().size()-1, reportValues.get(CashUpKeys.COVERS_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_SEATED_SESSION_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT).intValue());
        assertEquals(expectedFoodRevenueTotal * 2, reportValues.get(CashUpKeys.FOOD_VALUE).intValue());
        assertEquals(expectedFoodVAT * 2, reportValues.get(CashUpKeys.FOOD_VAT).intValue());
        assertEquals(4, reportValues.get(CashUpKeys.FOOD_COUNT).intValue());
        assertEquals(expectedDrinkRevenueTotal * 2, reportValues.get(CashUpKeys.DRINK_VALUE).intValue());
        assertEquals(expectedDrinkVAT * 2, reportValues.get(CashUpKeys.DRINK_VAT).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.DRINK_COUNT).intValue());
        assertEquals(expectedOtherRevenueTotal * 2, reportValues.get(CashUpKeys.OTHER_VALUE).intValue());
        assertEquals(expectedOtherVAT * 2, reportValues.get(CashUpKeys.OTHER_VAT).intValue());
        assertEquals(6, reportValues.get(CashUpKeys.OTHER_COUNT).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(2, reportValues.get(CashUpKeys.GUESTS).intValue());
        assertEquals(expectedRevenueTotal * 2, reportValues.get(CashUpKeys.GROSS_VALUE).intValue());
        assertEquals(expectedVATTotal * 2, reportValues.get(CashUpKeys.VAT_VALUE).intValue());
        assertEquals((expectedRevenueTotal - expectedVATTotal) * 2, reportValues.get(CashUpKeys.NET_VALUE).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_DELIVERY).intValue());
        assertEquals(expectedRevenueTotal * 2, reportValues.get(CashUpKeys.TOTAL_SALES).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());
    }

    @Test
    public void testAggregateWithPayments() throws Exception {
        setUpAggregator();
        int expectedRevenueTotal = 366;
        Adjustment adjustment1 = addAdjustment(expectedRevenueTotal, session1);

        addOrders(session1);
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.COLLECTION);

        addOrders(session2);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();

        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());

        // add an overpayment
        setUpAggregator();
        session1.getAdjustments().add(adjustment1);
        adjustment1.setValue(expectedRevenueTotal+1);

        addOrders(session1);
        aggregator.aggregate();
        reportValues = aggregator.getReportValues();

        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());

        //add an adjustment that doesn't support change
        setUpAggregator();
        session1.getAdjustments().add(adjustment1);
        adjustment1.setAdjustmentType( adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && !a.isSupportsChange()).findFirst().get());
        addOrders(session1);
        aggregator.aggregate();
        reportValues = aggregator.getReportValues();

        assertEquals(expectedRevenueTotal+1, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(1, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
    }

    private Adjustment addAdjustment(int expectedRevenueTotal, Session session) {
        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && a.isSupportsChange()).findFirst().get();
        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment1.setValue(expectedRevenueTotal);
        session.getAdjustments().add(adjustment1);
        return adjustment1;
    }

    @Test
    public void testAggregateWithPaymentsVoided() throws Exception {
        setUpAggregator();
        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && a.isSupportsChange()).findFirst().get();
        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        int expectedRevenueTotal = 366;
        adjustment1.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment1);
        session1.setVoidReason(new VoidReason());

        addOrders(session1);
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.COLLECTION);

        addOrders(session2);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();

        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());

        // add an overpayment
        setUpAggregator();
        session1.getAdjustments().add(adjustment1);
        adjustment1.setValue(expectedRevenueTotal+1);
        session1.setVoidReason(new VoidReason());

        addOrders(session1);
        aggregator.aggregate();
        reportValues = aggregator.getReportValues();

        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());

        //add an adjustment that doesn't support change
        setUpAggregator();
        session1.getAdjustments().add(adjustment1);
        adjustment1.setAdjustmentType( adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && !a.isSupportsChange()).findFirst().get());
        addOrders(session1);
        session1.setVoidReason(new VoidReason());

        aggregator.aggregate();
        reportValues = aggregator.getReportValues();

        assertEquals(0, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
    }

    @Test
    public void testAggregateWithPayments_NonChangeableAdjustments() throws Exception {
        setUpAggregator();
        int expectedRevenueTotal = 366;

        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && !a.isSupportsChange()).findFirst().get();
        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment1.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment1);

        Adjustment adjustment2 = new Adjustment();
        adjustment2.setAdjustmentType(paymentAdjustmentType1);
        adjustment2.setCreated(System.currentTimeMillis());
        adjustment2.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment2.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment2);

        addOrders(session1);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();

        assertEquals(expectedRevenueTotal*2, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
    }

    @Test
    public void testAggregateWithPayments_NonChangeableAdjustmentsAndGratuity() throws Exception {
        setUpAggregator();
        int expectedRevenueTotal = 366;

        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && !a.isSupportsChange()).findFirst().get();
        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment1.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment1);

        AdjustmentType gratuityType = new AdjustmentType();
        gratuityType.setType(AdjustmentTypeType.GRATUITY);
        gratuityType.setSupportsChange(false);
        gratuityType.setName("Gratuity");
        gratuityType = adjustmentTypeRepository.insert(gratuityType);

        Adjustment adjustment2 = new Adjustment();
        adjustment2.setAdjustmentType(gratuityType);
        adjustment2.setCreated(System.currentTimeMillis());
        adjustment2.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment2.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment2);

        addOrders(session1);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();

        assertEquals(expectedRevenueTotal*2, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
    }

    @Test
    public void testAggregateWithPayments_WithTipOnChangeablePayment1() throws Exception {
        int expectedRevenueTotal = 366 + 37;
        setUpAggregator();
        addPaymentAnd10PcTip(true, expectedRevenueTotal);
        addOrders(session1);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(37, reportValues.get(CashUpKeys.TOTAL_TIP).intValue());
    }

    @Test
    public void testAggregateWithPayments_WithTipOnChangeablePayment2() throws Exception {
        int expectedRevenueTotal = 366 + 37;
        setUpAggregator();
        addPaymentAnd10PcTip(true, expectedRevenueTotal);
        addPaymentAnd10PcTip(true, 10);
        addOrders(session1);
        aggregator.aggregate();

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(37, reportValues.get(CashUpKeys.TOTAL_TIP).intValue());
    }

    @Test
    public void testAggregateWithPayments_WithTipOnNonChangeablePayment1() throws Exception {
        int expectedRevenueTotal = 366 + 37;
        setUpAggregator();
        addPaymentAnd10PcTip(false, expectedRevenueTotal);
        addOrders(session1);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
        assertEquals(37, reportValues.get(CashUpKeys.TOTAL_TIP).intValue());
    }

    @Test
    public void testAggregateWithPayments_WithTipOnNonChangeablePayment2() throws Exception {
        int expectedRevenueTotal = 366 + 37;
        setUpAggregator();
        addPaymentAnd10PcTip(false, expectedRevenueTotal);
        addPaymentAnd10PcTip(false, 10);
        addOrders(session1);
        aggregator.aggregate();

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(expectedRevenueTotal + 10, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(37, reportValues.get(CashUpKeys.TOTAL_TIP).intValue());
        assertEquals(10, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue());
    }

    @Test
    public void testAggregateWithPayments_WithTipOnMixedChangeablePayment1() throws Exception {
        int expectedRevenueTotal = 366 + 37;
        setUpAggregator();
        addPaymentAnd10PcTip(true, expectedRevenueTotal);
        addPaymentAnd10PcTip(false, 10);
        addOrders(session1);
        aggregator.aggregate();

        Map<String,Integer> reportValues = aggregator.getReportValues();
        assertEquals(expectedRevenueTotal, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(37, reportValues.get(CashUpKeys.TOTAL_TIP).intValue());
        assertEquals(0, reportValues.get(CashUpKeys.OVER_PAYMENTS).intValue()); // because the 10 becomes changeable
    }

    private void addPaymentAnd10PcTip(boolean adjustmentIsChangeable, int amount) throws Exception {
        Adjustment adjustment = new Adjustment();

        AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setSupportsChange(adjustmentIsChangeable);
        adjustmentType.setName(RandomStringUtils.randomAlphabetic(5));
        adjustmentType.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType.setVisible(true);
        adjustmentType = adjustmentTypeRepository.save(adjustmentType);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(amount);
        session1.getAdjustments().add(adjustment);
        session1.setTipPercentage(10D);
    }

    @Test
    public void testAggregateWithPaymentsAndDiscounts() throws Exception {
        setUpAggregator();
        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && a.isSupportsChange()).findFirst().get();
        adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        int expectedRevenueTotal = 366;
        adjustment1.setValue(expectedRevenueTotal);
        session1.getAdjustments().add(adjustment1);
        AdjustmentType discountAdjustmentType2 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.DISCOUNT).findFirst().get();
        Adjustment adjustment2 = new Adjustment();
        adjustment2.setAdjustmentType(discountAdjustmentType2);
        adjustment2.setCreated(System.currentTimeMillis());
        adjustment2.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        int discount = 10;
        adjustment2.setValue(discount);
        session1.getAdjustments().add(adjustment2);

        Adjustment adjustment3 = new Adjustment();
        adjustment3.setAdjustmentType(discountAdjustmentType2);
        adjustment3.setCreated(System.currentTimeMillis());
        adjustment3.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        order1.setAdjustment(adjustment3);
        order1.setVoided(true);

        addOrders(session1);
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.COLLECTION);

        addOrders(session2);
        aggregator.aggregate();
        Map<String,Integer> reportValues = aggregator.getReportValues();
        Map<String,Integer> itemLossReport = aggregator.getItemAdjustmentLossReport();
        Map<String,Integer> paymentReport = aggregator.getPaymentReport();
        Map<String,Integer> adjustmentReport = aggregator.getAdjustmentReport();

        assertEquals(expectedRevenueTotal-20, reportValues.get(CashUpKeys.PAYMENTS).intValue());
        assertEquals(discount, reportValues.get(CashUpKeys.TOTAL_ADJUSTMENTS).intValue());
        assertEquals(346, paymentReport.get(adjustment1.getAdjustmentType().getName()).intValue());
        assertEquals(discount, adjustmentReport.get(adjustment2.getAdjustmentType().getName()).intValue());
    }

    private void addOrders(Session session) {
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        aggregator.addSession(session, orders);
    }

    @Ignore("Simple getter")
    @Test
    public void testGetReportValues() throws Exception {

    }

    @Ignore("Simple getter")
    @Test
    public void testGetPaymentReport() throws Exception {

    }

    @Ignore("Simple getter")
    @Test
    public void testGetAdjustmentReport() throws Exception {

    }

    @Ignore("Simple getter")
    @Test
    public void testGetItemAdjustmentLossReport() throws Exception {

    }
}