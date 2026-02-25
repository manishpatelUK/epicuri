package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class AdjustmentReportLine extends AbstractReportLine {
    //date, session id, session type, staff id, staff name, adjustment type, adjustment name, adjustment value, currency code, mews name, room number, charge id
    @CsvBindByName(column = "Date")
    @CsvSortOrder(order = 0)
    private String date;
    @CsvBindByName(column = "Session Id")
    @CsvSortOrder(order = 1)
    private String sessionId;
    @CsvBindByName(column = "Staff Id")
    @CsvSortOrder(order = 2)
    private String staffId;
    @CsvBindByName(column = "Staff Name")
    @CsvSortOrder(order = 3)
    private String staffName;
    @CsvBindByName(column = "Session Type")
    @CsvSortOrder(order = 4)
    private String sessionType;
    @CsvBindByName(column = "Table Number(s)")
    @CsvSortOrder(order = 5)
    private String tableNumber;
    @CsvBindByName(column = "Session Voided?")
    @CsvSortOrder(order = 6)
    private String sessionVoided;
    @CsvBindByName(column = "Adjustment Voided?")
    @CsvSortOrder(order = 7)
    private String adjustmentVoided;
    @CsvBindByName(column = "Voided By (Username)?")
    @CsvSortOrder(order = 8)
    private String adjustmentVoidedByUser;
    @CsvBindByName(column = "Voided By (User ID)?")
    @CsvSortOrder(order = 9)
    private String adjustmentVoidedByID;
    @CsvBindByName(column = "Payment/Discount")
    @CsvSortOrder(order = 10)
    private String adjustmentType;
    @CsvBindByName(column = "Adjustment Name")
    @CsvSortOrder(order = 11)
    private String adjustmentName;
    @CsvBindByName(column = "Currency Code")
    @CsvSortOrder(order = 12)
    private String currency;
    @CsvBindByName(column = "Adjustment Value")
    @CsvSortOrder(order = 13)
    private String adjustmentValue;
    @CsvBindByName(column = "Mews Charge ID")
    @CsvSortOrder(order = 14)
    private String mewsChargeId;
    @CsvBindByName(column = "Mews Name")
    @CsvSortOrder(order = 15)
    private String mewsName;
    @CsvBindByName(column = "Mews Room Number")
    @CsvSortOrder(order = 16)
    private String mewsRoomNumber;

    public AdjustmentReportLine() {
        super(AdjustmentReportLine.class);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getAdjustmentName() {
        return adjustmentName;
    }

    public void setAdjustmentName(String adjustmentName) {
        this.adjustmentName = adjustmentName;
    }

    public String getAdjustmentValue() {
        return adjustmentValue;
    }

    public void setAdjustmentValue(String adjustmentValue) {
        this.adjustmentValue = adjustmentValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMewsName() {
        return mewsName;
    }

    public void setMewsName(String mewsName) {
        this.mewsName = mewsName;
    }

    public String getMewsRoomNumber() {
        return mewsRoomNumber;
    }

    public void setMewsRoomNumber(String mewsRoomNumber) {
        this.mewsRoomNumber = mewsRoomNumber;
    }

    public String getMewsChargeId() {
        return mewsChargeId;
    }

    public void setMewsChargeId(String mewsChargeId) {
        this.mewsChargeId = mewsChargeId;
    }

    public String getSessionVoided() {
        return sessionVoided;
    }

    public void setSessionVoided(String sessionVoided) {
        this.sessionVoided = sessionVoided;
    }

    public String getAdjustmentVoided() {
        return adjustmentVoided;
    }

    public void setAdjustmentVoided(String adjustmentVoided) {
        this.adjustmentVoided = adjustmentVoided;
    }

    public String getAdjustmentVoidedByUser() {
        return adjustmentVoidedByUser;
    }

    public void setAdjustmentVoidedByUser(String adjustmentVoidedByUser) {
        this.adjustmentVoidedByUser = adjustmentVoidedByUser;
    }

    public String getAdjustmentVoidedByID() {
        return adjustmentVoidedByID;
    }

    public void setAdjustmentVoidedByID(String adjustmentVoidedByID) {
        this.adjustmentVoidedByID = adjustmentVoidedByID;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }
}
