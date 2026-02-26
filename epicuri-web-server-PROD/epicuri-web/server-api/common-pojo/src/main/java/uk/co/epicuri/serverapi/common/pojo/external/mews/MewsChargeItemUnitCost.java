package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeItemUnitCost {
    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Tax")
    private double tax;

    public MewsChargeItemUnitCost(){}
    public MewsChargeItemUnitCost(double amount, String currency, double tax) {
        this.amount = amount;
        this.currency = currency;
        this.tax = tax;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return "MewsChargeItemUnitCost{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", tax=" + tax +
                '}';
    }
}
