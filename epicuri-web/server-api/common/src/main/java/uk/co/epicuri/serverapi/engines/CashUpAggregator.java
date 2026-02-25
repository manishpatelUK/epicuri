package uk.co.epicuri.serverapi.engines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.SessionCalculationService;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by manish
 */

public class CashUpAggregator extends SessionAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CashUpAggregator.class);

    private final Map<String,Integer> reportValues = new HashMap<>();
    private final Map<String,Integer> refundValues = new HashMap<>();
    private final Map<String,Integer> paymentValues = new HashMap<>();
    private final Map<String,Integer> refundPaymentValues = new HashMap<>();
    private final Map<String,Integer> adjustmentValues = new HashMap<>();
    private final Map<String,Integer> itemAdjustmentValues = new HashMap<>();

    private List<Long> unfulfilledCheckIns;

    public CashUpAggregator(SessionCalculationService sessionCalculationService){
        super(sessionCalculationService);
        try {
            reset();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot reflect fields to prepopulate cashup");
        }
    }

    private void reset() throws IllegalAccessException {
        reportValues.clear();
        refundValues.clear();

        CashUpKeys stub = new CashUpKeys();
        Field[] fields = CashUpKeys.class.getDeclaredFields();

        for(Field field : fields) {
            field.setAccessible(true);
            if(field.isAccessible()) {
                reportValues.put((String) field.get(stub), 0);
                refundValues.put((String) field.get(stub), 0);
            }
        }

        paymentValues.clear();
        adjustmentValues.clear();
        itemAdjustmentValues.clear();
        refundPaymentValues.clear();
    }

    @Override
    public void aggregate() {
        try {
            reset();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot reflect fields to prepopulate cashup");
        }

        for(Map.Entry<Session,Map<CalculationKey,Number>> entry : calculatedValues.entrySet()) {
            Session session = entry.getKey();
            Map<CalculationKey,Number> valueMap = entry.getValue();

            boolean voided = (session.getClosedTime() != null && !SessionCalculationService.isPaid(valueMap)) || session.getVoidReason() != null;
            int sessionTotal = valueMap.get(CalculationKey.SESSION_TOTAL).intValue();
            if(voided && session.getSessionType() != SessionType.REFUND) {
                updateMap(reportValues,CashUpKeys.VOID_COUNT, 1);
                updateMap(reportValues,CashUpKeys.VOID_VALUE, sessionTotal);
            } else if(voided && session.getSessionType() == SessionType.REFUND) {
                updateMap(refundValues, CashUpKeys.VOID_REFUND_SESSION_COUNT, 1);
                updateMap(refundValues, CashUpKeys.VOID_REFUND_SESSION_VALUE, sessionTotal);
            }

            if(session.getSessionType() == SessionType.ADHOC
                    || session.getSessionType() == SessionType.SEATED
                    || session.getSessionType() == SessionType.TAB
                    ) {
                if(voided) {
                    updateMap(reportValues,CashUpKeys.VOID_SEATED_SESSION_COUNT, 1);
                    updateMap(reportValues,CashUpKeys.VOID_SEATED_SESSION_VALUE, sessionTotal);
                } else {
                    updateMap(reportValues,CashUpKeys.SEATED_SESSIONS_COUNT, 1);
                    updateMap(reportValues,CashUpKeys.SEATED_SESSIONS_VALUE, sessionTotal);
                    updateMap(reportValues,CashUpKeys.TOTAL_SALES, sessionTotal);
                }

                if (session.getSessionType() == SessionType.ADHOC) {
                    updateMap(reportValues,CashUpKeys.COVERS_COUNT, 1);
                } else {
                    updateMap(reportValues,CashUpKeys.COVERS_COUNT, session.getNumberOfRealDiners());
                }
            } else if(session.getSessionType() == SessionType.TAKEAWAY){
                if(voided) {
                    updateMap(reportValues,CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT, 1);
                    updateMap(reportValues,CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE, sessionTotal);
                } else {
                    updateMap(reportValues,CashUpKeys.TAKEAWAY_SESSIONS_COUNT, 1);
                    updateMap(reportValues,CashUpKeys.TAKEAWAY_SESSIONS_VALUE, sessionTotal);
                    updateMap(reportValues,CashUpKeys.TOTAL_SALES, sessionTotal);
                    updateMap(reportValues,CashUpKeys.TOTAL_SALES, valueMap.get(CalculationKey.DELIVERY_TOTAL).intValue());
                }
            } else if(session.getSessionType() == SessionType.REFUND && !voided) {
                updateMap(refundValues, CashUpKeys.REFUND_SESSIONS_COUNT, 1);
                updateMap(refundValues, CashUpKeys.REFUND_SESSIONS_VALUE, -sessionTotal);
            }

            OrderSummary orderSummary = SessionCalculationService.summarise(allOrders.get(session.getId()));

            // type breakdown
            if(!voided && session.getSessionType() != SessionType.REFUND && !session.isLinked()) {
                updateMap(reportValues, CashUpKeys.FOOD_VALUE, orderSummary.getItemTypeTotal().get(ItemType.FOOD));
                updateMap(reportValues, CashUpKeys.FOOD_VAT, orderSummary.getItemTypeVatTotal().get(ItemType.FOOD));
                updateMap(reportValues, CashUpKeys.FOOD_COUNT, orderSummary.getItemTypeCount().get(ItemType.FOOD));
                updateMap(reportValues, CashUpKeys.DRINK_VALUE, orderSummary.getItemTypeTotal().get(ItemType.DRINK));
                updateMap(reportValues, CashUpKeys.DRINK_VAT, orderSummary.getItemTypeVatTotal().get(ItemType.DRINK));
                updateMap(reportValues, CashUpKeys.DRINK_COUNT, orderSummary.getItemTypeCount().get(ItemType.DRINK));
                updateMap(reportValues, CashUpKeys.OTHER_VALUE, orderSummary.getItemTypeTotal().get(ItemType.OTHER));
                updateMap(reportValues, CashUpKeys.OTHER_VAT, orderSummary.getItemTypeVatTotal().get(ItemType.OTHER));
                updateMap(reportValues, CashUpKeys.OTHER_COUNT, orderSummary.getItemTypeCount().get(ItemType.OTHER));
            }

            if(!voided && session.getSessionType() == SessionType.REFUND && !session.isLinked()) {
                updateMap(refundValues, CashUpKeys.FOOD_REFUND_VALUE, -orderSummary.getItemTypeTotal().get(ItemType.FOOD));
                updateMap(refundValues, CashUpKeys.FOOD_REFUND_VAT, -orderSummary.getItemTypeVatTotal().get(ItemType.FOOD));
                updateMap(refundValues, CashUpKeys.FOOD_REFUND_COUNT, orderSummary.getItemTypeCount().get(ItemType.FOOD));
                updateMap(refundValues, CashUpKeys.DRINK_REFUND_VALUE, -orderSummary.getItemTypeTotal().get(ItemType.DRINK));
                updateMap(refundValues, CashUpKeys.DRINK_REFUND_VAT, -orderSummary.getItemTypeVatTotal().get(ItemType.DRINK));
                updateMap(refundValues, CashUpKeys.DRINK_REFUND_COUNT, orderSummary.getItemTypeCount().get(ItemType.DRINK));
                updateMap(refundValues, CashUpKeys.OTHER_REFUND_VALUE, -orderSummary.getItemTypeTotal().get(ItemType.OTHER));
                updateMap(refundValues, CashUpKeys.OTHER_REFUND_VAT, -orderSummary.getItemTypeVatTotal().get(ItemType.OTHER));
                updateMap(refundValues, CashUpKeys.OTHER_REFUND_COUNT, orderSummary.getItemTypeCount().get(ItemType.OTHER));
            }

            int negate = session.getSessionType() == SessionType.REFUND ? -1 : 1;
            //totals
            if(!voided) {
                int gross = negate * valueMap.get(CalculationKey.TOTAL_BEFORE_TIP).intValue();
                updateMap(reportValues, CashUpKeys.GROSS_VALUE, gross);
                int sumVat = negate * valueMap.get(CalculationKey.VAT_TOTAL).intValue();
                updateMap(reportValues,CashUpKeys.VAT_VALUE, sumVat);
                int sumNet = negate * (Math.abs(gross) - Math.abs(sumVat));
                updateMap(reportValues,CashUpKeys.NET_VALUE, sumNet);

                if(session.getSessionType() == SessionType.REFUND) {
                    updateMap(refundValues, CashUpKeys.GROSS_VALUE, gross);
                    updateMap(refundValues, CashUpKeys.VAT_VALUE, sumVat);
                    updateMap(refundValues,CashUpKeys.NET_VALUE, sumNet);
                }
            }

            if(!voided) {
                updateMap(reportValues, CashUpKeys.OVER_PAYMENTS, valueMap.get(CalculationKey.OVER_PAYMENTS).intValue());
                updateMap(reportValues, CashUpKeys.TOTAL_TIP, valueMap.get(CalculationKey.TIP_TOTAL).intValue());
                updateMap(reportValues, CashUpKeys.GUESTS, valueMap.get(CalculationKey.NUMBER_OF_GUESTS).intValue());
                updateMap(reportValues, CashUpKeys.TOTAL_DELIVERY, valueMap.get(CalculationKey.DELIVERY_TOTAL).intValue());
                int payments = negate * ((valueMap.get(CalculationKey.TOTAL_PAYMENTS).intValue() - valueMap.get(CalculationKey.CHANGE_DUE).intValue()) + valueMap.get(CalculationKey.GRATUITIES).intValue());
                updateMap(reportValues, CashUpKeys.PAYMENTS, payments);
                if(session.getSessionType() == SessionType.REFUND) {
                    updateMap(refundValues, CashUpKeys.PAYMENTS, payments);
                }
                updateMap(reportValues, CashUpKeys.TOTAL_ADJUSTMENTS, valueMap.get(CalculationKey.DISCOUNT_TOTAL).intValue());
            }

            Map<AdjustmentType,Integer> paymentBreakdown = SessionCalculationService.paymentBreakdown(session, valueMap.get(CalculationKey.TOTAL).intValue()).getA();
            for(Map.Entry<AdjustmentType,Integer> paymentEntry : paymentBreakdown.entrySet()) {
                int value = negate * (voided ? 0 : paymentEntry.getValue());
                updateMap(paymentValues, paymentEntry.getKey().getName(), value);
                if(session.getSessionType() == SessionType.REFUND) {
                    updateMap(refundPaymentValues, paymentEntry.getKey().getName(), value);
                }
            }

            Map<AdjustmentType,Integer> adjustmentBreakdown = SessionCalculationService.adjustmentBreakdown(session, sessionTotal);
            for(Map.Entry<AdjustmentType,Integer> adjustmentEntry : adjustmentBreakdown.entrySet()) {
                updateMap(adjustmentValues,adjustmentEntry.getKey().getName(), negate * (voided ? 0 : adjustmentEntry.getValue()));
            }

            Map<String,Integer> itemAdjustments = orderSummary.getAdjustments();
            for(Map.Entry<String,Integer> itemAdjustmentEntry : itemAdjustments.entrySet()) {
                updateMap(itemAdjustmentValues,itemAdjustmentEntry.getKey(), negate * (voided ? 0 : itemAdjustmentEntry.getValue()));
            }
        }

        // fill zeros
        Field[] allFields = CashUpKeys.class.getFields();
        for(Field field : allFields) {
            field.setAccessible(true);
            try {
                String key = field.get(null).toString();
                if(field.isAccessible() && !reportValues.containsKey(key)) {
                    reportValues.put(key,0);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Could not cash up keys", e);
            }
        }
    }

    private static <K> void updateMap(Map<K,Integer> map, K key, Integer value) {
        if(map.containsKey(key)) {
            map.compute(key, (k,v) -> v+value);
        } else {
            map.put(key, value);
        }
    }

    public Map<String,Integer> getReportValues() {
        return reportValues;
    }

    public Map<String,Integer> getPaymentReport() {
        return paymentValues;
    }

    public Map<String,Integer> getAdjustmentReport() {
        return adjustmentValues;
    }

    public Map<String,Integer> getItemAdjustmentLossReport() {
        return itemAdjustmentValues;
    }

    public List<Long> getUnfulfilledCheckIns() {
        return unfulfilledCheckIns;
    }

    public void setUnfulfilledCheckIns(List<Long> unfulfilledCheckIns) {
        this.unfulfilledCheckIns = unfulfilledCheckIns;
    }

    public Map<String, Integer> getRefundValues() {
        return refundValues;
    }

    public Map<String, Integer> getRefundPaymentValues() {
        return refundPaymentValues;
    }
}

