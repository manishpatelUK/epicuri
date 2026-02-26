package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdjustmentRequest {
    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("TypeId")
    private String adjustmentTypeId;

    @JsonProperty("NumericalTypeId")
    private int numericalTypeId;

    @JsonProperty("Value")
    private double value;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("linkedTo")
    private String linkedTo;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAdjustmentTypeId() {
        return adjustmentTypeId;
    }

    public void setAdjustmentTypeId(String adjustmentTypeId) {
        this.adjustmentTypeId = adjustmentTypeId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getNumericalTypeId() {
        return numericalTypeId;
    }

    public void setNumericalTypeId(int numericalTypeId) {
        this.numericalTypeId = numericalTypeId;
    }

    public String getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(String linkedTo) {
        this.linkedTo = linkedTo;
    }
}
