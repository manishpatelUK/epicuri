package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SetSalesRequest extends MarketManRequest {

    @JsonProperty("UniqueID")
    private String uniqueId;

    @JsonProperty("FromDateUTC")
    private String fromDateUTC;

    @JsonProperty("ToDateUTC")
    private String toDateUTC;

    @JsonProperty("TotalPriceWithVAT")
    private double totalPriceWithVAT;

    @JsonProperty("TotalPriceWithoutVAT")
    private double totalPriceWithoutVAT;

    @JsonProperty("Dishes")
    private List<MarketManDish> dishes;

    @JsonProperty("Modifiers")
    private List<MarketManModifier> modifiers;

    @JsonProperty("Transactions")
    private List<MarketManTransaction> transactions;

    public SetSalesRequest(String token) {
        super(token);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getFromDateUTC() {
        return fromDateUTC;
    }

    public void setFromDateUTC(String fromDateUTC) {
        this.fromDateUTC = fromDateUTC;
    }

    public String getToDateUTC() {
        return toDateUTC;
    }

    public void setToDateUTC(String toDateUTC) {
        this.toDateUTC = toDateUTC;
    }

    public double getTotalPriceWithVAT() {
        return totalPriceWithVAT;
    }

    public void setTotalPriceWithVAT(double totalPriceWithVAT) {
        this.totalPriceWithVAT = totalPriceWithVAT;
    }

    public double getTotalPriceWithoutVAT() {
        return totalPriceWithoutVAT;
    }

    public void setTotalPriceWithoutVAT(double totalPriceWithoutVAT) {
        this.totalPriceWithoutVAT = totalPriceWithoutVAT;
    }

    public List<MarketManDish> getDishes() {
        return dishes;
    }

    public void setDishes(List<MarketManDish> dishes) {
        this.dishes = dishes;
    }

    public List<MarketManModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<MarketManModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public List<MarketManTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<MarketManTransaction> transactions) {
        this.transactions = transactions;
    }
}
