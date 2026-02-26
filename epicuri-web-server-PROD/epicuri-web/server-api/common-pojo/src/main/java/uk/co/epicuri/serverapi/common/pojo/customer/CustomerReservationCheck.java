package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerReservationCheck {
    public static final String REASON_DELIVERY = "Delivery";

    @JsonProperty("Cost")
    private double cost;

    private double extraCosts;
    private String extraCostsReason;

    @JsonProperty("Warning")
    private List<String> warning;

    public List<String> getWarning() {
        return warning;
    }

    public void setWarning(List<String> warning) {
        this.warning = warning;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getExtraCosts() {
        return extraCosts;
    }

    public void setExtraCosts(double extraCosts) {
        this.extraCosts = extraCosts;
    }

    public String getExtraCostsReason() {
        return extraCostsReason;
    }

    public void setExtraCostsReason(String extraCostsReason) {
        this.extraCostsReason = extraCostsReason;
    }
}
