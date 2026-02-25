package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class ModifierDetailsReportLine extends AbstractReportLine{
    @CsvBindByName(column = "Modifier Name")
    @CsvSortOrder(order = 0)
    private String modifierName;

    @CsvBindByName(column = "Value")
    @CsvSortOrder(order = 1)
    private String value;

    @CsvBindByName(column = "Tax Name")
    @CsvSortOrder(order = 2)
    private String taxName;

    @CsvBindByName(column = "Tax Rate")
    @CsvSortOrder(order = 3)
    private String taxRate;

    @CsvBindByName(column = "Modifier Group")
    @CsvSortOrder(order = 4)
    private String modifierGroup;

    @CsvBindByName(column = "Menu Item Name")
    @CsvSortOrder(order = 5)
    private String menuItemName;

    @CsvBindByName(column = "Menu Item ID")
    @CsvSortOrder(order = 6)
    private String menuItemId;

    @CsvBindByName(column = "Session DateTime")
    @CsvSortOrder(order = 7)
    private String sessionDateTime;

    @CsvBindByName(column = "Session ID")
    @CsvSortOrder(order = 8)
    private String sessionId;

    @CsvBindByName(column = "Session Type")
    @CsvSortOrder(order = 9)
    private String sessionType;

    @CsvBindByName(column = "Staff Name")
    @CsvSortOrder(order = 10)
    private String staffName;

    @CsvBindByName(column = "StaffId")
    @CsvSortOrder(order = 11)
    private String staffId;

    @CsvBindByName(column = "Origin")
    @CsvSortOrder(order = 12)
    private String origin;

    @CsvBindByName(column = "Guest Name")
    @CsvSortOrder(order = 13)
    private String guestName;

    @CsvBindByName(column = "Order ID")
    @CsvSortOrder(order = 14)
    private String orderId;

    public ModifierDetailsReportLine() {
        super(ModifierDetailsReportLine.class);
    }


    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getModifierGroup() {
        return modifierGroup;
    }

    public void setModifierGroup(String modifierGroup) {
        this.modifierGroup = modifierGroup;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getSessionDateTime() {
        return sessionDateTime;
    }

    public void setSessionDateTime(String sessionDateTime) {
        this.sessionDateTime = sessionDateTime;
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

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
