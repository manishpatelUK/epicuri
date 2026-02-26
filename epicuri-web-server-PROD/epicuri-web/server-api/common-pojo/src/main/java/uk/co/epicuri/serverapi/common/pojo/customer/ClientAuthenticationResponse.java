package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientAuthenticationResponse {
    @JsonProperty("Status")
    private String status;

    @JsonProperty("Customer")
    private CustomerCustomerView customer;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CustomerCustomerView getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerCustomerView customer) {
        this.customer = customer;
    }
}
