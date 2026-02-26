package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manish
 */
public class CashUpResponse {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("StartTime")
    private long startTime;

    @JsonProperty("EndTime")
    private long endTime;

    @JsonProperty("WrapUp")
    private boolean wrapUp = false;

    @JsonProperty("Report")
    private Map<String,Double> report = new HashMap<>();

    @JsonProperty("RefundReport")
    private Map<String,Double> refundReport = new HashMap<>();

    @JsonProperty("RefundPaymentReport")
    private Map<String,Double> refundPaymentReport = new HashMap<>();

    @JsonProperty("PaymentReport")
    private Map<String,Double> paymentReport = new HashMap<>();

    @JsonProperty("AdjustmentReport")
    private Map<String,Double> adjustmentReport = new HashMap<>();

    @JsonProperty("ItemAdjustmentLossReport")
    private Map<String,Double> itemAdjustmentLossReport = new HashMap<>();

    public CashUpResponse(){}

    public CashUpResponse(long startTime, long endTime,
                          Map<String,Integer> reportValues,
                          Map<String, Integer> payments,
                          Map<String, Integer> adjustments,
                          Map<String, Integer> itemAdjustmentLoss,
                          Map<String, Integer> refundValues,
                          Map<String, Integer> refundPaymentValues) {
        this.id = RandomStringUtils.randomAlphanumeric(8); //id not valid for simulations
        this.startTime = startTime / 1000;
        this.endTime = endTime / 1000;

        updateValues(reportValues, payments, adjustments, itemAdjustmentLoss, refundValues, refundPaymentValues);
    }

    public CashUpResponse(CashUp cashUp) {
        this.id = cashUp.getId();
        this.startTime = cashUp.getStartTime() / 1000;
        this.endTime = cashUp.getEndTime() / 1000;

        updateValues(cashUp.getReport(), cashUp.getPaymentReport(), cashUp.getAdjustmentReport(), cashUp.getItemAdjustmentLossReport(), cashUp.getRefundReport(), cashUp.getRefundPaymentReport());
    }

    private void updateValues(Map<String,Integer> reportValues,
                              Map<String, Integer> payments,
                              Map<String, Integer> adjustments,
                              Map<String, Integer> itemAdjustmentLoss,
                              Map<String, Integer> refundValues,
                              Map<String, Integer> refundPaymentValues) {
        updateInteger(report,reportValues,CashUpKeys.SEATED_SESSIONS_COUNT);
        updateMoney(report,reportValues,CashUpKeys.SEATED_SESSIONS_VALUE);
        updateInteger(report,reportValues,CashUpKeys.TAKEAWAY_SESSIONS_COUNT);
        updateMoney(report,reportValues,CashUpKeys.TAKEAWAY_SESSIONS_VALUE);
        updateInteger(report,reportValues,CashUpKeys.COVERS_COUNT);
        updateInteger(report,reportValues,CashUpKeys.VOID_COUNT);
        updateMoney(report,reportValues,CashUpKeys.VOID_VALUE);
        updateMoney(report,reportValues,CashUpKeys.VOID_SEATED_SESSION_VALUE);
        updateInteger(report,reportValues,CashUpKeys.VOID_SEATED_SESSION_COUNT);
        updateMoney(report,reportValues,CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE);
        updateInteger(report,reportValues,CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT);
        updateMoney(report,reportValues,CashUpKeys.FOOD_VALUE);
        updateMoney(report,reportValues,CashUpKeys.FOOD_VAT);
        updateInteger(report,reportValues,CashUpKeys.FOOD_COUNT);
        updateMoney(report,reportValues,CashUpKeys.DRINK_VALUE);
        updateMoney(report,reportValues,CashUpKeys.DRINK_VAT);
        updateInteger(report,reportValues,CashUpKeys.DRINK_COUNT);
        updateMoney(report,reportValues,CashUpKeys.OTHER_VALUE);
        updateMoney(report,reportValues,CashUpKeys.OTHER_VAT);
        updateInteger(report,reportValues,CashUpKeys.OTHER_COUNT);
        updateMoney(report,reportValues,CashUpKeys.OVER_PAYMENTS);
        updateMoney(report,reportValues,CashUpKeys.TOTAL_TIP);
        updateInteger(report,reportValues,CashUpKeys.GUESTS);
        updateMoney(report,reportValues,CashUpKeys.GROSS_VALUE);
        updateMoney(report,reportValues,CashUpKeys.VAT_VALUE);
        updateMoney(report,reportValues,CashUpKeys.NET_VALUE);
        updateMoney(report,reportValues,CashUpKeys.TOTAL_DELIVERY);
        updateMoney(report,reportValues,CashUpKeys.TOTAL_SALES);
        updateMoney(report,reportValues,CashUpKeys.PAYMENTS);
        updateMoney(report,reportValues,CashUpKeys.TOTAL_ADJUSTMENTS);

        //refunds
        updateInteger(refundReport, refundValues, CashUpKeys.VOID_REFUND_SESSION_COUNT);
        updateMoney(refundReport, refundValues, CashUpKeys.VOID_REFUND_SESSION_VALUE);
        updateInteger(refundReport, refundValues, CashUpKeys.REFUND_SESSIONS_COUNT);
        updateMoney(refundReport, refundValues, CashUpKeys.REFUND_SESSIONS_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.FOOD_REFUND_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.FOOD_REFUND_VAT);
        updateMoney(refundReport, refundValues, CashUpKeys.FOOD_REFUND_COUNT);
        updateMoney(refundReport, refundValues, CashUpKeys.DRINK_REFUND_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.DRINK_REFUND_VAT);
        updateMoney(refundReport, refundValues, CashUpKeys.DRINK_REFUND_COUNT);
        updateMoney(refundReport, refundValues, CashUpKeys.OTHER_REFUND_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.OTHER_REFUND_VAT);
        updateMoney(refundReport, refundValues, CashUpKeys.OTHER_REFUND_COUNT);
        updateMoney(refundReport, refundValues, CashUpKeys.GROSS_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.VAT_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.NET_VALUE);
        updateMoney(refundReport, refundValues, CashUpKeys.PAYMENTS);


        payments.keySet().stream().sorted().forEach(s -> updateMoney(paymentReport, payments, s));
        adjustments.keySet().stream().sorted().forEach(s -> updateMoney(adjustmentReport, adjustments, s));
        itemAdjustmentLoss.keySet().stream().sorted().forEach(s -> updateMoney(itemAdjustmentLossReport, itemAdjustmentLoss, s));
        refundPaymentValues.keySet().stream().sorted().forEach(s -> updateMoney(refundPaymentReport, refundPaymentValues, s));
    }

    private static void updateMoney(Map<String,Double> target, Map<String,Integer> allValues, String key) {
        target.put(key, MoneyService.toMoneyRoundNearest(allValues.getOrDefault(key, 0)));
    }

    private static void updateInteger(Map<String,Double> target, Map<String,Integer> allValues, String key) {
        target.put(key, allValues.getOrDefault(key, 0).doubleValue());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isWrapUp() {
        return wrapUp;
    }

    public void setWrapUp(boolean wrapUp) {
        this.wrapUp = wrapUp;
    }

    public Map<String, Double> getReport() {
        return report;
    }

    public void setReport(Map<String, Double> report) {
        this.report = report;
    }

    public Map<String, Double> getPaymentReport() {
        return paymentReport;
    }

    public void setPaymentReport(Map<String, Double> paymentReport) {
        this.paymentReport = paymentReport;
    }

    public Map<String, Double> getAdjustmentReport() {
        return adjustmentReport;
    }

    public void setAdjustmentReport(Map<String, Double> adjustmentReport) {
        this.adjustmentReport = adjustmentReport;
    }

    public Map<String, Double> getItemAdjustmentLossReport() {
        return itemAdjustmentLossReport;
    }

    public void setItemAdjustmentLossReport(Map<String, Double> itemAdjustmentLossReport) {
        this.itemAdjustmentLossReport = itemAdjustmentLossReport;
    }

    public Map<String, Double> getRefundPaymentReport() {
        return refundPaymentReport;
    }

    public void setRefundPaymentReport(Map<String, Double> refundPaymentReport) {
        this.refundPaymentReport = refundPaymentReport;
    }

    public Map<String, Double> getRefundReport() {
        return refundReport;
    }

    public void setRefundReport(Map<String, Double> refundReport) {
        this.refundReport = refundReport;
    }
}
