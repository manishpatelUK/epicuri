package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsCustomerSearchResponse {
    @JsonProperty("Customers")
    private List<MewsCustomer> customers = new ArrayList<>();

    public List<MewsCustomer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<MewsCustomer> customers) {
        this.customers = customers;
    }
}
