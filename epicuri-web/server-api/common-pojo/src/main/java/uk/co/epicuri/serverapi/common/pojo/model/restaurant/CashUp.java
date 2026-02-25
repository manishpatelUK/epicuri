package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.*;

@Document(collection = TableNames.CASH_UPS)
public class CashUp extends Deletable {
    @Indexed
    private String restaurantId;

    private long startTime; // has ? in cs //was double

    @Indexed
    private long endTime; // was double

    private  Map<String, Integer> report = new HashMap<>();
    private  Map<String, Integer> refundReport = new HashMap<>();
    private  Map<String, Integer> paymentReport = new HashMap<>();
    private  Map<String, Integer> refundPaymentReport = new HashMap<>();
    private  Map<String, Integer> adjustmentReport = new HashMap<>();
    private  Map<String, Integer> itemAdjustmentLossReport = new HashMap<>();

    private Set<String> sessionIds = new HashSet<>();
    private List<Long> unfulfilledCheckIns = new ArrayList<>();

    private Map<String,Boolean> flags = new HashMap<>();

    public CashUp() {}

    public CashUp(String restaurantId, long startTime, long endTime, Map<String, Integer> report, Map<String, Integer> paymentReport, Map<String, Integer> adjustmentReport, Map<String, Integer> itemAdjustmentLossReport, List<String> sessionIds, Map<String,Integer> refundReport, Map<String,Integer> refundPaymentReport) {
        this.restaurantId = restaurantId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.report = report;
        this.paymentReport = paymentReport;
        this.adjustmentReport = adjustmentReport;
        this.itemAdjustmentLossReport = itemAdjustmentLossReport;
        this.sessionIds = new HashSet<>(sessionIds);
        this.refundReport = refundReport;
        this.refundPaymentReport = refundPaymentReport;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public Map<String, Integer> getReport() {
        return report;
    }

    public void setReport(Map<String, Integer> report) {
        this.report = report;
    }

    public Map<String, Integer> getPaymentReport() {
        return paymentReport;
    }

    public void setPaymentReport(Map<String, Integer> paymentReport) {
        this.paymentReport = paymentReport;
    }

    public Map<String, Integer> getAdjustmentReport() {
        return adjustmentReport;
    }

    public void setAdjustmentReport(Map<String, Integer> adjustmentReport) {
        this.adjustmentReport = adjustmentReport;
    }

    public Map<String, Integer> getItemAdjustmentLossReport() {
        return itemAdjustmentLossReport;
    }

    public void setItemAdjustmentLossReport(Map<String, Integer> itemAdjustmentLossReport) {
        this.itemAdjustmentLossReport = itemAdjustmentLossReport;
    }

    public Set<String> getSessionIds() {
        return sessionIds;
    }

    public void setSessionIds(Set<String> sessionIds) {
        this.sessionIds = sessionIds;
    }

    public List<Long> getUnfulfilledCheckIns() {
        return unfulfilledCheckIns;
    }

    public void setUnfulfilledCheckIns(List<Long> unfulfilledCheckIns) {
        this.unfulfilledCheckIns = unfulfilledCheckIns;
    }

    public Map<String, Integer> getRefundReport() {
        return refundReport;
    }

    public void setRefundReport(Map<String, Integer> refundReport) {
        this.refundReport = refundReport;
    }

    public Map<String, Integer> getRefundPaymentReport() {
        return refundPaymentReport;
    }

    public void setRefundPaymentReport(Map<String, Integer> refundPaymentReport) {
        this.refundPaymentReport = refundPaymentReport;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Boolean> flags) {
        this.flags = flags;
    }
}
