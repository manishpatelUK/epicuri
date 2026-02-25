package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MewsTaxations {
    @JsonProperty("Taxations")
    private List<MewsTaxation> taxations = new ArrayList<>();

    @JsonProperty("TaxRates")
    private List<MewsTaxRate> taxRates = new ArrayList<>();

    public List<MewsTaxation> getTaxations() {
        return taxations;
    }

    public void setTaxations(List<MewsTaxation> taxations) {
        this.taxations = taxations;
    }

    public List<MewsTaxRate> getTaxRates() {
        return taxRates;
    }

    public void setTaxRates(List<MewsTaxRate> taxRates) {
        this.taxRates = taxRates;
    }
}
