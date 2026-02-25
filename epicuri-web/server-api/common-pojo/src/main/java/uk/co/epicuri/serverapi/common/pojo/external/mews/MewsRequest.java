package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Manish on 17/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsRequest {
    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("Customer")
    private MewsCustomer customer;

    @JsonProperty("PaymentAmount")
    private double paymentAmount;

    @JsonProperty("Reference")
    private String reference;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public MewsCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(MewsCustomer customer) {
        this.customer = customer;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
