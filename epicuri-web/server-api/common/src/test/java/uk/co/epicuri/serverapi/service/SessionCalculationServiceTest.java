package uk.co.epicuri.serverapi.service;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SessionCalculationServiceTest extends SessionSetupBaseIT {

    @Test
    public void testIsPaid() throws Exception {
        setUpSession();

        assertFalse(sessionCalculationService.isPaid(session1));

        int expectedTotal = 366;
        int expectedTotalPlusTip = 403;
        double tipPC = 10D;

        AdjustmentType adjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT).findFirst().get();

        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(adjustmentType);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment1.setValue(expectedTotal-1);
        session1.getAdjustments().add(adjustment1);
        session1 = sessionRepository.save(session1);

        assertFalse(sessionCalculationService.isPaid(session1));

        Adjustment adjustment2 = new Adjustment();
        adjustment2.setAdjustmentType(adjustmentType);
        adjustment2.setCreated(System.currentTimeMillis());
        adjustment2.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment2.setValue(1);
        session1.getAdjustments().add(adjustment2);
        session1 = sessionRepository.save(session1);

        assertTrue(sessionCalculationService.isPaid(session1));

        setUpSession();
        assertFalse(sessionCalculationService.isPaid(session1));
        session1.setTipPercentage(tipPC);
        session1 = sessionRepository.save(session1);

        adjustment1.setValue(expectedTotal);
        session1.getAdjustments().add(adjustment2);
        session1 = sessionRepository.save(session1);

        assertFalse(sessionCalculationService.isPaid(session1));

        adjustment1.setValue(expectedTotalPlusTip);
        session1.getAdjustments().clear();
        session1.getAdjustments().add(adjustment1);
        session1 = sessionRepository.save(session1);

        assertTrue(sessionCalculationService.isPaid(session1));
    }

    @Test
    public void testIsPaidWhenValueIs0() throws Exception {
        setUpSession();
        orderRepository.deleteAll();

        assertTrue(sessionCalculationService.isPaid(session1));

        Map<CalculationKey,Number> numbers = sessionCalculationService.calculateValues(session1);
        assertEquals(0, numbers.get(CalculationKey.REMAINING_TOTAL).intValue());
        assertEquals(0, numbers.get(CalculationKey.TOTAL).intValue());
    }

    @Test
    public void testCalculateValues() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        checkValues(expectedRevenueTotal,
                expectedVATTotal,
                0,
                0,
                values.get(CalculationKey.TOTAL),
                values.get(CalculationKey.TOTAL_BEFORE_TIP),
                values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS),
                values.get(CalculationKey.SUB_TOTAL),
                values.get(CalculationKey.VAT_TOTAL),
                values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS),
                values.get(CalculationKey.TIP_TOTAL),
                Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()),
                values.get(CalculationKey.DELIVERY_TOTAL),
                values.get(CalculationKey.SESSION_TOTAL),
                values.get(CalculationKey.DISCOUNT_TOTAL),
                expectedRevenueTotal,
                values.get(CalculationKey.REMAINING_TOTAL),
                values.get(CalculationKey.OVER_PAYMENTS),
                values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWhenAdjustmentsVoided() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 400;
        addChangeablePayment(paymentAmount);

        for(Adjustment adjustment : session1.getAdjustments()) {
            adjustment.setVoided(true);
        }

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        checkValues(expectedRevenueTotal, expectedVATTotal, 0, 0, values.get(CalculationKey.TOTAL), values.get(CalculationKey.TOTAL_BEFORE_TIP), values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.SUB_TOTAL), values.get(CalculationKey.VAT_TOTAL), values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.TIP_TOTAL), Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()), values.get(CalculationKey.DELIVERY_TOTAL), values.get(CalculationKey.SESSION_TOTAL), values.get(CalculationKey.DISCOUNT_TOTAL), expectedRevenueTotal, values.get(CalculationKey.REMAINING_TOTAL), values.get(CalculationKey.OVER_PAYMENTS), values.get(CalculationKey.TOTAL_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
        assertEquals(paymentAmount, values.get(CalculationKey.TOTAL_PAYMENTS_VOIDED));
    }

    @Test
    public void testCalculateValuesWithDeliveryCharge() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        setUpDeliveryData();

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(150, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(25, values.get(CalculationKey.VAT_DELIVERY));
        checkValues(expectedRevenueTotal+150, expectedVATTotal+25, 0, 150, values.get(CalculationKey.TOTAL), values.get(CalculationKey.TOTAL_BEFORE_TIP), values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS).intValue()+150, values.get(CalculationKey.SUB_TOTAL).intValue()+150, values.get(CalculationKey.VAT_TOTAL), values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.TIP_TOTAL), Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()), values.get(CalculationKey.DELIVERY_TOTAL), values.get(CalculationKey.SESSION_TOTAL).intValue()+150, values.get(CalculationKey.DISCOUNT_TOTAL), expectedRevenueTotal, values.get(CalculationKey.REMAINING_TOTAL), values.get(CalculationKey.OVER_PAYMENTS), values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    private void setUpDeliveryData() {
        session1.setOriginalBooking(booking1);
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setTakeawayType(TakeawayType.DELIVERY);
        booking1.setRestaurantId(session1.getRestaurantId());
        booking1.setTakeawayType(TakeawayType.DELIVERY);
        Address deliveryAddress = new Address();
        deliveryAddress.setPostcode("HA6 1AU");
        booking1.setDeliveryAddress(deliveryAddress);
        bookingRepository.save(booking1);
        sessionRepository.save(session1);

        Restaurant restaurant = restaurantRepository.findOne(session1.getRestaurantId());
        Address address = new Address();
        address.setPostcode("LE7 9UD");
        restaurant.setAddress(address);
        restaurantRepository.save(restaurant);
    }

    @Test
    public void testCalculateValuesWithPaymentThatSupportsChange1() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 10;
        addChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        checkValues(expectedRevenueTotal, expectedVATTotal, paymentAmount, 0, values.get(CalculationKey.TOTAL), values.get(CalculationKey.TOTAL_BEFORE_TIP), values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.SUB_TOTAL), values.get(CalculationKey.VAT_TOTAL), values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.TIP_TOTAL), Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()), values.get(CalculationKey.DELIVERY_TOTAL), values.get(CalculationKey.SESSION_TOTAL), values.get(CalculationKey.DISCOUNT_TOTAL), expectedRevenueTotal - paymentAmount, values.get(CalculationKey.REMAINING_TOTAL), values.get(CalculationKey.OVER_PAYMENTS), values.get(CalculationKey.TOTAL_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithPaymentThatSupportsChange2() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 400;
        addChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        checkValues(expectedRevenueTotal, expectedVATTotal, paymentAmount, 0, values.get(CalculationKey.TOTAL), values.get(CalculationKey.TOTAL_BEFORE_TIP), values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.SUB_TOTAL), values.get(CalculationKey.VAT_TOTAL), values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS), values.get(CalculationKey.TIP_TOTAL), Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()), values.get(CalculationKey.DELIVERY_TOTAL), values.get(CalculationKey.SESSION_TOTAL), values.get(CalculationKey.DISCOUNT_TOTAL), expectedRevenueTotal - paymentAmount, values.get(CalculationKey.REMAINING_TOTAL), values.get(CalculationKey.OVER_PAYMENTS), values.get(CalculationKey.TOTAL_PAYMENTS));
        assertEquals(paymentAmount-expectedRevenueTotal, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithPaymentThatDoesNotSupportChange1() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 150;
        addNonChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       expectedVATTotal , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(366-150, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithPaymentThatDoesNotSupportChange2() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 1000;
        addNonChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       expectedVATTotal , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(-634, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(634, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithMixedChangeSupport1() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 10;
        addChangeablePayment(expectedRevenueTotal);
        addNonChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       expectedVATTotal , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(-1 * paymentAmount, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(paymentAmount, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount + expectedRevenueTotal, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithMixedChangeSupport2() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int paymentAmount = 1000;
        addChangeablePayment(expectedRevenueTotal);
        addNonChangeablePayment(paymentAmount);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       expectedVATTotal , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(-1 * paymentAmount, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(1000-366, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(366, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(paymentAmount + expectedRevenueTotal, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithDiscount() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int discountAmount = 20;
        NumericalAdjustmentType type = NumericalAdjustmentType.ABSOLUTE;
        addDiscount(discountAmount, type);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal - discountAmount, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal - discountAmount, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       55 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(discountAmount, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals((expectedRevenueTotal) - discountAmount, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWith100PercentDiscount() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int discountAmount = 1000;
        NumericalAdjustmentType type = NumericalAdjustmentType.PERCENTAGE;
        addDiscount(discountAmount, type);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(0, values.get(CalculationKey.TOTAL));
        assertEquals(0, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       0 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(0, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithDiscountOnCertainItems() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        int discountAmount = 100;
        NumericalAdjustmentType type = NumericalAdjustmentType.PERCENTAGE;
        addDiscount(discountAmount, type, ItemType.FOOD);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(339, values.get(CalculationKey.TOTAL));
        assertEquals(339, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       54 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(27, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(339, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithOrderDeleted() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;

        int order2Value = 266;
        order2.setDeleted(0L);
        orderRepository.save(order2);
        Map<CalculationKey,Number> values = values = sessionCalculationService.calculateValues(session1, orders);

        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       13 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(13, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(10 + (3*30), values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals((10 + (3*30)), values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithOrderRemoveFromReports() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;

        int order2Value = 266;
        order2.setRemoveFromReports(true);
        orderRepository.save(order2);
        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);

        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal - order2Value, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       13 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(13, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(10 + (3*30), values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals((10 + (3*30)), values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithMenuItemDeleted1() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        menuItem2.setDeleted(0L);
        menuItemRepository.save(menuItem2);
        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);

        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       58 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithMenuItemDeleted2() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        menuItemRepository.delete(menuItem2);
        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);

        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       58 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithTipPercentage1() throws Exception {
        setUpSession();

        testSeatedSessionWithTip();
    }

    @Test
    public void testCalculateValuesWithTipPercentage2() throws Exception {
        setUpSession();
        session1.setSessionType(SessionType.ADHOC);
        sessionRepository.save(session1);

        testSeatedSessionWithTip();
    }

    private void testSeatedSessionWithTip() {
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;
        double tipPercentage = 10D;
        session1.setTipPercentage(tipPercentage);
        sessionRepository.save(session1);
        int expectedTipAmount = 37;

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedTipAmount + expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(       58 , values.get(CalculationKey.VAT_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(expectedTipAmount, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(tipPercentage, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals((expectedTipAmount + 10 + (2*20) + (2*113) + (3*30)), values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    private void checkValues(int expectedRevenueTotal, int expectedVATTotal, int payment1Amount, int expectedDeliveryCost, Number total, Number totalBeforeTip, Number totalBeforeAdjustments, Number subTotal, Number vatTotal, Number vatTotalBeforeAdjustments, Number tipTotal, int percentageTip, Number actualDeliveryCost, Number sessionTotal, Number discountTotal, int expectedRevenue, Number remainingTotal, Number overpayments, Number totalPayments) {
        assertEquals(expectedRevenueTotal, total);
        assertEquals(expectedRevenueTotal, totalBeforeTip);
        assertEquals(expectedRevenueTotal, totalBeforeAdjustments);
        assertEquals(expectedRevenueTotal, subTotal);
        assertEquals(expectedVATTotal, vatTotal); //using http://vatcalconline.com/
        assertEquals(expectedVATTotal, vatTotalBeforeAdjustments); //using http://vatcalconline.com/
        assertEquals(0, tipTotal);
        assertEquals(0, percentageTip);
        assertEquals(expectedDeliveryCost, actualDeliveryCost);
        assertEquals(expectedRevenueTotal, sessionTotal);
        assertEquals(0, discountTotal);
        assertEquals(expectedRevenue+expectedDeliveryCost, remainingTotal);
        assertEquals(0, overpayments);
        assertEquals(payment1Amount, totalPayments);
    }

    private void addChangeablePayment(int paymentAmount) {
        AdjustmentType paymentAdjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && a.isSupportsChange()).findFirst().get();
        Adjustment adjustment = new Adjustment();
        adjustment.setAdjustmentType(paymentAdjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(paymentAmount);
        session1.getAdjustments().add(adjustment);
        session1 = sessionRepository.save(session1);
    }

    private void addNonChangeablePayment(int paymentAmount) {
        AdjustmentType paymentAdjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.PAYMENT && !a.isSupportsChange()).findFirst().get();
        Adjustment adjustment = new Adjustment();
        adjustment.setAdjustmentType(paymentAdjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(paymentAmount);
        session1.getAdjustments().add(adjustment);
        session1 = sessionRepository.save(session1);
    }

    private void addDiscount(int discountAmount, NumericalAdjustmentType numericalType) {
        addDiscount(discountAmount, numericalType, ItemType.ALL);
    }

    private void addDiscount(int discountAmount, NumericalAdjustmentType numericalType, ItemType onItemType) {
        AdjustmentType discountAdjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.DISCOUNT).findFirst().get();
        Adjustment adjustment = new Adjustment();
        adjustment.setAdjustmentType(discountAdjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setNumericalType(numericalType);
        adjustment.setValue(discountAmount);
        adjustment.setApplicableToItems(onItemType);
        session1.getAdjustments().add(adjustment);
        session1 = sessionRepository.save(session1);
    }


    @Test
    public void testCalculateValuesWithGratuityOverpayment() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        setUpOverPayment(10);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL)); //using http://vatcalconline.com/
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(0, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(10, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    public void setUpOverPayment(int value) {
        AdjustmentType gratuity = new AdjustmentType();
        gratuity.setType(AdjustmentTypeType.GRATUITY);
        gratuity.setSupportsChange(false);
        gratuity.setName("GRATUITY");
        gratuity = adjustmentTypeRepository.insert(gratuity);

        Adjustment adjustment = new Adjustment();
        adjustment.setAdjustmentType(gratuity);
        adjustment.setValue(value);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);
    }

    @Test
    public void testCalculateValuesWithGratuityOverpayment2() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        setUpOverPayment(400);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(400, values.get(CalculationKey.OVER_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithPercentageAdjustmentDiscount() throws Exception{
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        int expectedRevenueTotal = 366;
        int expectedVATTotal = 58;

        AdjustmentType paymentAdjustmentType1 = adjustmentTypeRepository.findAll().stream().filter(a -> a.getType() == AdjustmentTypeType.DISCOUNT).findFirst().get();
        Adjustment adjustment1 = new Adjustment();
        adjustment1.setAdjustmentType(paymentAdjustmentType1);
        adjustment1.setCreated(System.currentTimeMillis());
        adjustment1.setNumericalType(NumericalAdjustmentType.PERCENTAGE);
        int percentageAmount = 500; //==50%
        adjustment1.setValue(percentageAmount);
        session1.getAdjustments().add(adjustment1);
        session1 = sessionRepository.save(session1);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(expectedRevenueTotal / 2, values.get(CalculationKey.TOTAL));
        assertEquals(expectedRevenueTotal / 2, values.get(CalculationKey.TOTAL_BEFORE_TIP));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SUB_TOTAL));
        assertEquals(expectedVATTotal / 2, values.get(CalculationKey.VAT_TOTAL)); //using http://vatcalconline.com/
        assertEquals(expectedVATTotal, values.get(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS)); //using http://vatcalconline.com/
        assertEquals(0, values.get(CalculationKey.TIP_TOTAL));
        assertEquals(0, Double.compare(0D, values.get(CalculationKey.TIP_PERCENTAGE).doubleValue()));
        assertEquals(0, values.get(CalculationKey.DELIVERY_TOTAL));
        assertEquals(expectedRevenueTotal, values.get(CalculationKey.SESSION_TOTAL));
        assertEquals(expectedRevenueTotal / 2, values.get(CalculationKey.DISCOUNT_TOTAL));
        assertEquals(expectedRevenueTotal / 2, values.get(CalculationKey.REMAINING_TOTAL));
        assertEquals(0, values.get(CalculationKey.OVER_PAYMENTS));
        assertEquals(0, values.get(CalculationKey.CHANGE_DUE));
        assertEquals(0, values.get(CalculationKey.TOTAL_PAYMENTS));
    }

    @Test
    public void testCalculateValuesWithOrderAdjustments() throws Exception{
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        order1.setAdjustment(adjustment1);

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(356, values.get(CalculationKey.TOTAL));
        assertEquals(58, values.get(CalculationKey.VAT_TOTAL));

        order3.setAdjustment(adjustment1);
        values = sessionCalculationService.calculateValues(session1, orders);
        assertEquals(266, values.get(CalculationKey.TOTAL));
        assertEquals(45, values.get(CalculationKey.VAT_TOTAL));
    }

    @Test
    public void testCalculateDeliveryCost() throws Exception {
        setUpSession();
        setUpDeliveryData();

        assertEquals(150, sessionCalculationService.calculateDeliveryCost(session1));

        booking1.setTakeawayType(TakeawayType.COLLECTION);
        bookingRepository.save(booking1);
        assertEquals(0, sessionCalculationService.calculateDeliveryCost(session1));

        booking1.setTakeawayType(TakeawayType.DELIVERY);
        session1.setOriginalBooking(null);
        session1.setOriginalBookingId(null);
        assertEquals(0, sessionCalculationService.calculateDeliveryCost(session1));
    }

    @Test
    public void testSummarise() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        order1.getTaxRate().setName("rate 1");
        order2.getTaxRate().setName("rate 2");
        order3.getTaxRate().setName("rate 3");

        int expectedRevenueTotal = 366;
        int expectedDrinkTotal = 10;
        int expectedFoodTotal = 266;
        int expectedOtherTotal = 90;
        int expectedVATTotal = 58;
        int expectedFoodVATTotal = 45;
        int expectedDrinkVATTotal = 0;
        int expectedOtherVATTotal = 13;

        OrderSummary summary = SessionCalculationService.summarise(orders);

        checkOrderSummaryValues(expectedRevenueTotal, expectedDrinkTotal, expectedFoodTotal, expectedOtherTotal, expectedVATTotal, expectedFoodVATTotal, expectedDrinkVATTotal, expectedOtherVATTotal, 1, summary);
        assertEquals(3, summary.getVatTypeTotal().size());
    }

    private void checkOrderSummaryValues(int expectedRevenueTotal, int expectedDrinkTotal, int expectedFoodTotal, int expectedOtherTotal, int expectedVATTotal, int expectedFoodVATTotal, int expectedDrinkVATTotal, int expectedOtherVATTotal, int expectedNumberOfDrinks, OrderSummary summary) {
        assertEquals(expectedRevenueTotal, summary.sumTotal());
        assertEquals(expectedDrinkTotal, summary.getItemTypeTotal().get(ItemType.DRINK).intValue());
        assertEquals(expectedFoodTotal, summary.getItemTypeTotal().get(ItemType.FOOD).intValue());
        assertEquals(expectedOtherTotal, summary.getItemTypeTotal().get(ItemType.OTHER).intValue());

        assertEquals(expectedVATTotal, summary.getSumVAT());
        assertEquals(expectedFoodVATTotal, summary.getItemTypeVatTotal().get(ItemType.FOOD).intValue());
        assertEquals(expectedDrinkVATTotal, summary.getItemTypeVatTotal().get(ItemType.DRINK).intValue());
        assertEquals(expectedOtherVATTotal, summary.getItemTypeVatTotal().get(ItemType.OTHER).intValue());

        assertEquals(expectedNumberOfDrinks, summary.getItemTypeCount().get(ItemType.DRINK).intValue());
        assertEquals(2, summary.getItemTypeCount().get(ItemType.FOOD).intValue());
        assertEquals(3, summary.getItemTypeCount().get(ItemType.OTHER).intValue());
    }

    @Test
    public void testSummariseWithZerodOrders() throws Exception {
        setUpSession();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        order1.setRemoveFromReports(true);

        int expectedRevenueTotal = 366-10;
        int expectedDrinkTotal = 10-10;
        int expectedFoodTotal = 266;
        int expectedOtherTotal = 90;
        int expectedVATTotal = 58;
        int expectedFoodVATTotal = 45;
        int expectedDrinkVATTotal = 0;
        int expectedOtherVATTotal = 13;

        checkOrderSummaryValues(expectedRevenueTotal, expectedDrinkTotal, expectedFoodTotal, expectedOtherTotal, expectedVATTotal, expectedFoodVATTotal, expectedDrinkVATTotal, expectedOtherVATTotal, 0, SessionCalculationService.summarise(orders));

        order1.setRemoveFromReports(false);
        order1.setDeleted(0L);
        checkOrderSummaryValues(expectedRevenueTotal, expectedDrinkTotal, expectedFoodTotal, expectedOtherTotal, expectedVATTotal, expectedFoodVATTotal, expectedDrinkVATTotal, expectedOtherVATTotal, 0, SessionCalculationService.summarise(orders));
    }

    @Test
    public void testPaymentBreakdown() throws Exception {
        setUpPaymentsAndAdjustments();

        Tuple<Map<AdjustmentType,Integer>,Map<Adjustment,Integer>> payments = SessionCalculationService.paymentBreakdown(session1, 30);
        Map<AdjustmentType,Integer> paymentsByType = payments.getA();
        assertEquals(2, paymentsByType.size());
        assertEquals(10, paymentsByType.get(adjustmentType1).intValue());
        assertEquals(20, paymentsByType.get(adjustmentType3).intValue());

        Map<Adjustment,Integer> amountByAdjustment = payments.getB();
        assertEquals(2, amountByAdjustment.size());
        assertEquals(10, amountByAdjustment.get(adjustment1).intValue());
        assertEquals(20, amountByAdjustment.get(adjustment3).intValue());
    }

    @Test
    public void testPaymentBreakdownWithChange() throws Exception {
        setUpPaymentsAndAdjustments();
        session1.getAdjustments().clear();
        session1.getAdjustments().add(adjustment1);
        adjustment1.getAdjustmentType().setSupportsChange(true);
        adjustment1.setValue(20);

        Tuple<Map<AdjustmentType,Integer>,Map<Adjustment,Integer>> payments = SessionCalculationService.paymentBreakdown(session1, 10);

        Map<AdjustmentType,Integer> paymentsByType = payments.getA();
        assertEquals(1, paymentsByType.size());
        assertEquals(10, paymentsByType.get(adjustmentType1).intValue());

        Map<Adjustment,Integer> amountByAdjustment = payments.getB();
        assertEquals(1, amountByAdjustment.size());
        assertEquals(20, amountByAdjustment.get(adjustment1).intValue());
    }

    private void setUpPaymentsAndAdjustments() throws Exception {
        setUpSession();
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType2.setType(AdjustmentTypeType.DISCOUNT);
        adjustmentType3.setType(AdjustmentTypeType.PAYMENT);
        adjustment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment1.setValue(10);
        adjustment2.setNumericalType(NumericalAdjustmentType.PERCENTAGE);
        adjustment2.setValue(100);
        adjustment3.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment3.setValue(20);
        session1.getAdjustments().add(adjustment1);
        session1.getAdjustments().add(adjustment2);
        session1.getAdjustments().add(adjustment3);
    }

    @Test
    public void testAdjustmentBreakdown() throws Exception {
        setUpPaymentsAndAdjustments();

        Map<AdjustmentType,Integer> adjustmentByType = SessionCalculationService.adjustmentBreakdown(session1, 110);
        assertEquals(1, adjustmentByType.size());
        assertEquals(11, adjustmentByType.get(adjustmentType2).intValue());
    }

    @Test
    public void testCalculateDinerSplits() throws Exception {
        setUpSession();
        // diner1 (table) -- 1 x menu item 1 (tax @ 0%, DRINK, 10) -> 10
        // diner2 -- 2 x menu item 2 (tax @ 20%, FOOD, 20) + Modifiers (113) -> 40 + 226
        // diner3 -- 3 x menu item 3 (tax @ 17.5%, OTHER, 30) -> 90
        BillSplit billSplit = sessionCalculationService.calculateDinerSplits(session1, Arrays.asList(order1, order2, order3));
        assertSplits(billSplit);
    }

    @Test
    public void testCalculateDinerSplitsWithTip() throws Exception {
        setUpSession();
        // diner1 (table) -- 1 x menu item 1 (tax @ 0%, DRINK, 10) -> 10
        // diner2 -- 2 x menu item 2 (tax @ 20%, FOOD, 20) + Modifiers (113) -> 40 + 226
        // diner3 -- 3 x menu item 3 (tax @ 17.5%, OTHER, 30) -> 90
        // tip = 10%, (10+266+90)*1.1 == 37
        session1.setTipPercentage(10D);

        BillSplit billSplit = sessionCalculationService.calculateDinerSplits(session1, Arrays.asList(order1, order2, order3));
        assertSplits(billSplit);
        assertNull(billSplit.getTipSplits().get(diner1.getId()));
        assertEquals(19, billSplit.getTipSplits().get(diner2.getId()), 0.01); //tip /2 (round up)
        assertEquals(18, billSplit.getTipSplits().get(diner3.getId()), 0.01); //tip /2 (round down)
    }

    public void assertSplits(BillSplit billSplit) {
        assertEquals(10, billSplit.getItemSplits().get(diner1.getId()).intValue());
        assertEquals(40+226, billSplit.getItemSplits().get(diner2.getId()).intValue());
        assertEquals(90, billSplit.getItemSplits().get(diner3.getId()).intValue());
        assertNull(billSplit.getTableItemSplits().get(diner1.getId()));
        assertEquals(5, billSplit.getTableItemSplits().get(diner2.getId()).intValue());
        assertEquals(5, billSplit.getTableItemSplits().get(diner3.getId()).intValue());
    }

    @Test
    public void testCalculateDinerSplitsWithDiscount() throws Exception {
        setUpSession();
        // diner1 (table) -- 1 x menu item 1 (tax @ 0%, DRINK, 10) -> 10
        // diner2 -- 2 x menu item 2 (tax @ 20%, FOOD, 20) + Modifiers (113) -> 40 + 226
        // diner3 -- 3 x menu item 3 (tax @ 17.5%, OTHER, 30) -> 90
        // adjustment of -10% (10+266+90)*0.1 == 37
        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.setNumericalType(NumericalAdjustmentType.PERCENTAGE);
        adjustment.setValue(100);//10%
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        BillSplit billSplit = sessionCalculationService.calculateDinerSplits(session1, Arrays.asList(order1, order2, order3));
        assertSplits(billSplit);
        assertNull(billSplit.getDiscountSplits().get(diner1.getId()));
        assertEquals(19, billSplit.getDiscountSplits().get(diner2.getId()).intValue());
        assertEquals(18, billSplit.getDiscountSplits().get(diner3.getId()).intValue());
    }

    @Test
    public void testPriceOverrideOnOrder() {
        Order order = new Order();
        order.setMenuItemId(menuItem1.getId());
        order.setPriceOverride(200);
        order.setQuantity(2);

        assertEquals((2*200), SessionCalculationService.getOrderValue(order));
    }

    @Test
    public void testPriceOverrideOnOrderWithModifier() {
        Order order = new Order();
        order.setMenuItemId(menuItem1.getId());
        order.getModifiers().add(modifier1);
        modifier1.setPriceOverride(113);
        modifier1.setPrice(113);
        order.setPriceOverride(200);
        order.setQuantity(2);

        assertEquals((2*200) + (2*113), SessionCalculationService.getOrderValue(order));
    }

    @Test
    public void testCalculateNet() {
        assertEquals(1000, SessionCalculationService.calculateNet(1200, 0.2));
    }
}