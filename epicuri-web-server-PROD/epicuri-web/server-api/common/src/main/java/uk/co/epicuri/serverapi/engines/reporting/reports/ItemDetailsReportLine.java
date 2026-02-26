package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class ItemDetailsReportLine extends AbstractReportLine {
    //date, session id, session type, staff id, staff name, menu item id, menu item name, sales price, void reason, item type, tax % and name
    @CsvBindByName(column = "Session Date/Time")
    @CsvSortOrder(order = 0)
    private String date;
    @CsvBindByName(column = "Order Date/Time")
    @CsvSortOrder(order = 1)
    private String orderDateTime;
    @CsvBindByName(column = "Session ID")
    @CsvSortOrder(order = 2)
    private String sessionId;
    @CsvBindByName(column = "Session Type")
    @CsvSortOrder(order = 3)
    private String sessionType;
    @CsvBindByName(column = "Table Number(s)")
    @CsvSortOrder(order = 4)
    private String tableNumbers;
    @CsvBindByName(column = "Staff ID")
    @CsvSortOrder(order = 5)
    private String staffId;
    @CsvBindByName(column = "Staff Name")
    @CsvSortOrder(order = 6)
    private String staffName;
    @CsvBindByName(column = "Origin")
    @CsvSortOrder(order = 7)
    private String origin;
    @CsvBindByName(column = "Item ID")
    @CsvSortOrder(order = 8)
    private String menuItemId;
    @CsvBindByName(column = "Guest Name")
    @CsvSortOrder(order = 9)
    private String guestName;
    @CsvBindByName(column = "Item Name")
    @CsvSortOrder(order = 10)
    private String menuItemName;
    @CsvBindByName(column = "Sales Price")
    @CsvSortOrder(order = 11)
    private String salesPrice;
    @CsvBindByName(column = "Quantity")
    @CsvSortOrder(order = 12)
    private String quantity;
    @CsvBindByName(column = "Void Reason")
    @CsvSortOrder(order = 13)
    private String voidReason;
    @CsvBindByName(column = "Type")
    @CsvSortOrder(order = 14)
    private String itemType;
    @CsvBindByName(column = "Tax Name")
    @CsvSortOrder(order = 15)
    private String taxName;
    @CsvBindByName(column = "Tax Rate")
    @CsvSortOrder(order = 16)
    private String taxRate;

    public ItemDetailsReportLine() {
        super(ItemDetailsReportLine.class);
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

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(String orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public String getTableNumbers() {
        return tableNumbers;
    }

    public void setTableNumbers(String tableNumbers) {
        this.tableNumbers = tableNumbers;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
}
