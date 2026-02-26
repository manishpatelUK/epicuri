package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class RevenueReportLine extends AbstractReportLine {
    //start time, closed time, session id, session type, sub total, tips, adjustments, total, payments, change, overpayment
    @CsvBindByName(column = "End Time")
    @CsvSortOrder(order = 0)
    private String endTime;
    @CsvBindByName(column = "Start Time")
    @CsvSortOrder(order = 1)
    private String startTime;
    @CsvBindByName(column = "Session Id")
    @CsvSortOrder(order = 2)
    private String sessionId;
    @CsvBindByName(column = "Number of Covers")
    @CsvSortOrder(order = 3)
    private String numberOfCovers;
    @CsvBindByName(column = "Session Type")
    @CsvSortOrder(order = 4)
    private String sessionType;
    @CsvBindByName(column = "Table Number(s)")
    @CsvSortOrder(order = 5)
    private String tableNumbers;
    @CsvBindByName(column = "Currency Code")
    @CsvSortOrder(order = 6)
    private String currencyCode;
    @CsvBindByName(column = "Closed By (Username)")
    @CsvSortOrder(order = 7)
    private String closedBy;
    @CsvBindByName(column = "Closed By (ID)")
    @CsvSortOrder(order = 8)
    private String closedById;
    @CsvBindByName(column = "Subtotal")
    @CsvSortOrder(order = 9)
    private String subTotal;
    @CsvBindByName(column = "Total Due")
    @CsvSortOrder(order = 10)
    private String total;
    @CsvBindByName(column = "Total VAT")
    @CsvSortOrder(order = 11)
    private String vatTotal;
    @CsvBindByName(column = "Payments")
    @CsvSortOrder(order = 12)
    private String payments;
    @CsvBindByName(column = "Included Discounts")
    @CsvSortOrder(order = 13)
    private String discounts;
    @CsvBindByName(column = "Included Tips")
    @CsvSortOrder(order = 14)
    private String tips;
    @CsvBindByName(column = "Included Overpayment")
    @CsvSortOrder(order = 15)
    private String overpayment;
    @CsvBindByName(column = "Voided?")
    @CsvSortOrder(order = 16)
    private String voided;
    @CsvBindByName(column = "Void Reason")
    @CsvSortOrder(order = 17)
    private String voidReason;
    @CsvBindByName(column = "Voided Payments")
    @CsvSortOrder(order = 18)
    private String voidedPayments;

    public RevenueReportLine() {
        super(RevenueReportLine.class);
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getDiscounts() {
        return discounts;
    }

    public void setDiscounts(String discounts) {
        this.discounts = discounts;
    }

    public String getPayments() {
        return payments;
    }

    public void setPayments(String payments) {
        this.payments = payments;
    }

    public String getOverpayment() {
        return overpayment;
    }

    public void setOverpayment(String overpayment) {
        this.overpayment = overpayment;
    }

    public String getVoided() {
        return voided;
    }

    public void setVoided(String voided) {
        this.voided = voided;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getNumberOfCovers() {
        return numberOfCovers;
    }

    public void setNumberOfCovers(String numberOfCovers) {
        this.numberOfCovers = numberOfCovers;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public String getClosedById() {
        return closedById;
    }

    public void setClosedById(String closedById) {
        this.closedById = closedById;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getVoidedPayments() {
        return voidedPayments;
    }

    public void setVoidedPayments(String voidedPayments) {
        this.voidedPayments = voidedPayments;
    }

    public String getVatTotal() {
        return vatTotal;
    }

    public void setVatTotal(String vatTotal) {
        this.vatTotal = vatTotal;
    }

    public String getTableNumbers() {
        return tableNumbers;
    }

    public void setTableNumbers(String tableNumbers) {
        this.tableNumbers = tableNumbers;
    }
}
