package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MewsTaxRate {
    @JsonProperty("Code")
    private String code;
    @JsonProperty("TaxationCode")
    private String taxationCode;
    @JsonProperty("Strategy")
    private MewsTaxStrategy strategy;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTaxationCode() {
        return taxationCode;
    }

    public void setTaxationCode(String taxationCode) {
        this.taxationCode = taxationCode;
    }

    public MewsTaxStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(MewsTaxStrategy strategy) {
        this.strategy = strategy;
    }
}
