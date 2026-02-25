package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class CashUpReportLine extends AbstractReportLine {
    @CsvBindByName(column = "Start")
    @CsvSortOrder(order = 0)
    private String startDate;
    @CsvBindByName(column = "End")
    @CsvSortOrder(order = 1)
    private String endDate;
    @CsvBindByName(column = "On Premise Count")
    @CsvSortOrder(order = 2)
    private String onPremiseCount;
    @CsvBindByName(column = "On Premise Value")
    @CsvSortOrder(order = 3)
    private String onPremiseValue;
    @CsvBindByName(column = "Takeaway Count")
    @CsvSortOrder(order = 4)
    private String takeawaysCount;
    @CsvBindByName(column = "Takeaway Value")
    @CsvSortOrder(order = 5)
    private String takeawaysValue;
    @CsvBindByName(column = "Unpaid On Premise Count")
    @CsvSortOrder(order = 6)
    private String unpaidOnPremiseCount;
    @CsvBindByName(column = "Unpaid On Premise Value")
    @CsvSortOrder(order = 7)
    private String unpaidOnPremiseValue;
    @CsvBindByName(column = "Unpaid Takeaway Count")
    @CsvSortOrder(order = 8)
    private String unpaidTakeawayCount;
    @CsvBindByName(column = "Unpaid Takeaway Value")
    @CsvSortOrder(order = 9)
    private String unpaidTakeawayValue;
    @CsvBindByName(column = "Total Voids")
    @CsvSortOrder(order = 10)
    private String totalUnpaid;
    @CsvBindByName(column = "Food Items Count")
    @CsvSortOrder(order = 11)
    private String foodCount;
    @CsvBindByName(column = "Food Items Value")
    @CsvSortOrder(order = 12)
    private String grossFoodAmount;
    @CsvBindByName(column = "Drink Items Count")
    @CsvSortOrder(order = 13)
    private String drinkCount;
    @CsvBindByName(column = "Drink Items Value")
    @CsvSortOrder(order = 14)
    private String grossDrinkAmount;
    @CsvBindByName(column = "Other Items Count")
    @CsvSortOrder(order = 15)
    private String otherCount;
    @CsvBindByName(column = "Other Items Value")
    @CsvSortOrder(order = 16)
    private String grossOtherAmount;
    @CsvBindByName(column = "Delivery Charges")
    @CsvSortOrder(order = 17)
    private String deliveryCharges;
    @CsvBindByName(column = "Total Sales (before adjustments)")
    @CsvSortOrder(order = 18)
    private String totalSales;
    @CsvBindByName(column = "Total Bill Adjustments")
    @CsvSortOrder(order = 19)
    private String totalAdjustments;

    @CsvBindByName(column = "DiscountTypes")
    @CsvSortOrder(order = 20)
    @DynamicColumn
    private String adjustmentTypes;

    @CsvBindByName(column = "Total Sales (after adjustments)")
    @CsvSortOrder(order = 21)
    private String totalSalesAfterAdjustments;
    @CsvBindByName(column = "Total VAT Charged")
    @CsvSortOrder(order = 22)
    private String totalVATCharged;
    @CsvBindByName(column = "Net Sales")
    @CsvSortOrder(order = 23)
    private String netSales;
    @CsvBindByName(column = "Tips (inc on bill)")
    @CsvSortOrder(order = 24)
    private String tips;
    @CsvBindByName(column = "Over-Payments")
    @CsvSortOrder(order = 25)
    private String overpayments;
    @CsvBindByName(column = "Total Payments (inc over-payments/tips)")
    @CsvSortOrder(order = 26)
    private String totalPayments;

    @CsvBindByName(column = "PaymentTypes")
    @CsvSortOrder(order = 27)
    @DynamicColumn
    private String paymentTypes;

    @CsvBindByName(column = "Refunds")
    @CsvSortOrder(order = 28)
    @DynamicColumn
    private String refunds;

    @CsvBindByName(column = "Unique ID")
    @CsvSortOrder(order = 29)
    private String id;

    public CashUpReportLine() {
        super(CashUpReportLine.class);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getOnPremiseCount() {
        return onPremiseCount;
    }

    public void setOnPremiseCount(String onPremiseCount) {
        this.onPremiseCount = onPremiseCount;
    }

    public String getOnPremiseValue() {
        return onPremiseValue;
    }

    public void setOnPremiseValue(String onPremiseValue) {
        this.onPremiseValue = onPremiseValue;
    }

    public String getTakeawaysCount() {
        return takeawaysCount;
    }

    public void setTakeawaysCount(String takeawaysCount) {
        this.takeawaysCount = takeawaysCount;
    }

    public String getTakeawaysValue() {
        return takeawaysValue;
    }

    public void setTakeawaysValue(String takeawaysValue) {
        this.takeawaysValue = takeawaysValue;
    }

    public String getUnpaidOnPremiseCount() {
        return unpaidOnPremiseCount;
    }

    public void setUnpaidOnPremiseCount(String unpaidOnPremiseCount) {
        this.unpaidOnPremiseCount = unpaidOnPremiseCount;
    }

    public String getUnpaidOnPremiseValue() {
        return unpaidOnPremiseValue;
    }

    public void setUnpaidOnPremiseValue(String unpaidOnPremiseValue) {
        this.unpaidOnPremiseValue = unpaidOnPremiseValue;
    }

    public String getUnpaidTakeawayCount() {
        return unpaidTakeawayCount;
    }

    public void setUnpaidTakeawayCount(String unpaidTakeawayCount) {
        this.unpaidTakeawayCount = unpaidTakeawayCount;
    }

    public String getUnpaidTakeawayValue() {
        return unpaidTakeawayValue;
    }

    public void setUnpaidTakeawayValue(String unpaidTakeawayValue) {
        this.unpaidTakeawayValue = unpaidTakeawayValue;
    }

    public String getTotalUnpaid() {
        return totalUnpaid;
    }

    public void setTotalUnpaid(String totalUnpaid) {
        this.totalUnpaid = totalUnpaid;
    }

    public String getFoodCount() {
        return foodCount;
    }

    public void setFoodCount(String foodCount) {
        this.foodCount = foodCount;
    }

    public String getGrossFoodAmount() {
        return grossFoodAmount;
    }

    public void setGrossFoodAmount(String grossFoodAmount) {
        this.grossFoodAmount = grossFoodAmount;
    }

    public String getDrinkCount() {
        return drinkCount;
    }

    public void setDrinkCount(String drinkCount) {
        this.drinkCount = drinkCount;
    }

    public String getGrossDrinkAmount() {
        return grossDrinkAmount;
    }

    public void setGrossDrinkAmount(String grossDrinkAmount) {
        this.grossDrinkAmount = grossDrinkAmount;
    }

    public String getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(String otherCount) {
        this.otherCount = otherCount;
    }

    public String getGrossOtherAmount() {
        return grossOtherAmount;
    }

    public void setGrossOtherAmount(String grossOtherAmount) {
        this.grossOtherAmount = grossOtherAmount;
    }

    public String getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(String deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public String getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(String totalSales) {
        this.totalSales = totalSales;
    }

    public String getTotalAdjustments() {
        return totalAdjustments;
    }

    public void setTotalAdjustments(String totalAdjustments) {
        this.totalAdjustments = totalAdjustments;
    }

    public String getTotalSalesAfterAdjustments() {
        return totalSalesAfterAdjustments;
    }

    public void setTotalSalesAfterAdjustments(String totalSalesAfterAdjustments) {
        this.totalSalesAfterAdjustments = totalSalesAfterAdjustments;
    }

    public String getTotalVATCharged() {
        return totalVATCharged;
    }

    public void setTotalVATCharged(String totalVATCharged) {
        this.totalVATCharged = totalVATCharged;
    }

    public String getNetSales() {
        return netSales;
    }

    public void setNetSales(String netSales) {
        this.netSales = netSales;
    }

    public String getOverpayments() {
        return overpayments;
    }

    public void setOverpayments(String overpayments) {
        this.overpayments = overpayments;
    }

    public String getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(String totalPayments) {
        this.totalPayments = totalPayments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(String paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public String getAdjustmentTypes() {
        return adjustmentTypes;
    }

    public void setAdjustmentTypes(String adjustmentTypes) {
        this.adjustmentTypes = adjustmentTypes;
    }

    public String getRefunds() {
        return refunds;
    }

    public void setRefunds(String refunds) {
        this.refunds = refunds;
    }
}
