package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeItemUnitCost {
    /*@JsonProperty("NetValue")
    private double netValue;*/

    @JsonProperty("GrossValue")
    private double grossValue;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("TaxCodes")
    private List<String> taxCodes = new ArrayList<>();

    public MewsChargeItemUnitCost(double grossValue, double netValue, String currency, String taxCode) {
        this.grossValue = grossValue;
        //this.netValue = netValue;
        this.currency = currency;
        if(taxCode != null) {
            this.taxCodes.add(taxCode);
        }
    }

    /* public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }*/

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getTaxCodes() {
        return taxCodes;
    }

    public void setTaxCodes(List<String> taxCodes) {
        this.taxCodes = taxCodes;
    }

    public double getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(double grossValue) {
        this.grossValue = grossValue;
    }

    @Override
    public String toString() {
        return "MewsChargeItemUnitCost{" +
                ", grossValue=" + grossValue +
                ", currency='" + currency + '\'' +
                ", taxCodes=" + taxCodes +
                '}';
    }
}
