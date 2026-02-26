package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

public class MarketManDish {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("PriceWithVAT")
    private double netPrice;

    @JsonProperty("PriceWithoutVAT")
    private double grossPrice;

    @JsonProperty("Code")
    private String code;

    @JsonProperty("Category")
    private String category;

    public MarketManDish(){}
    public MarketManDish(MenuItem menuItem, double netPrice) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.netPrice = netPrice;
        this.grossPrice = MoneyService.toMoneyRoundNearest(menuItem.getPrice());
        this.category = menuItem.getType().getName();
        this.code = menuItem.getPlu();
    }

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

    public double getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(double netPrice) {
        this.netPrice = netPrice;
    }

    public double getGrossPrice() {
        return grossPrice;
    }

    public void setGrossPrice(double grossPrice) {
        this.grossPrice = grossPrice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
