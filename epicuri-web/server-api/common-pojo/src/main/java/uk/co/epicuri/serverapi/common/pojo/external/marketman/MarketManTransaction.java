package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketManTransaction {
    @JsonProperty("DishID")
    private String id;

    @JsonProperty("DishName")
    private String name;

    @JsonProperty("DishCode")
    private String code;

    @JsonProperty("PriceTotalWithVAT")
    private double grossTotal;

    @JsonProperty("PriceTotalWithoutVAT")
    private double netTotal;

    @JsonProperty("DateUTC")
    private String dateUTC;

    @JsonProperty("Quantity")
    private double quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(double grossTotal) {
        this.grossTotal = grossTotal;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public String getDateUTC() {
        return dateUTC;
    }

    public void setDateUTC(String dateUTC) {
        this.dateUTC = dateUTC;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
