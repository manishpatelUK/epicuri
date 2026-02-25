package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class AggregatedItemsReportLine extends AbstractReportLine {
    @CsvBindByName(column = "Item ID/SKU")
    @CsvSortOrder(order = 0)
    private String itemId;
    @CsvBindByName(column = "Item Name")
    @CsvSortOrder(order = 1)
    private String itemName;
    @CsvBindByName(column = "Price")
    @CsvSortOrder(order = 2)
    private String price;
    @CsvBindByName(column = "Last Sold At")
    @CsvSortOrder(order = 3)
    private String lastSold;
    @CsvBindByName(column = "Quantity")
    @CsvSortOrder(order = 4)
    private String quantity;
    @CsvBindByName(column = "Value(inc. mods)")
    @CsvSortOrder(order = 5)
    private String value;
    @CsvBindByName(column = "Value(exc. mods)")
    @CsvSortOrder(order = 6)
    private String valueExcludingMods;
    @CsvBindByName(column = "Average Sales Price")
    @CsvSortOrder(order = 7)
    private String averageSalesPrice;
    @CsvBindByName(column = "Type")
    @CsvSortOrder(order = 8)
    private String type;
    @CsvBindByName(column = "Tax Name")
    @CsvSortOrder(order = 9)
    private String taxName;
    @CsvBindByName(column = "Tax Rate")
    @CsvSortOrder(order = 10)
    private String taxRate;

    public AggregatedItemsReportLine() {
        super(AggregatedItemsReportLine.class);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLastSold() {
        return lastSold;
    }

    public void setLastSold(String lastSold) {
        this.lastSold = lastSold;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAverageSalesPrice() {
        return averageSalesPrice;
    }

    public void setAverageSalesPrice(String averageSalesPrice) {
        this.averageSalesPrice = averageSalesPrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getValueExcludingMods() {
        return valueExcludingMods;
    }

    public void setValueExcludingMods(String valueExcludingMods) {
        this.valueExcludingMods = valueExcludingMods;
    }
}
