package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 17/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeRequest extends MewsAPIRequest{

    @JsonProperty("CustomerId")
    private String customerId;

    @JsonProperty("Items")
    private List<MewsChargeItem> items = new ArrayList<>();

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("ServiceId")
    private String serviceId;

//    @JsonProperty("ProductOrders")
//    private List<MewsProductOrders> productOrders = new ArrayList<>();

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<MewsChargeItem> getItems() {
        return items;
    }

    public void setItems(List<MewsChargeItem> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /*public List<MewsProductOrders> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<MewsProductOrders> productOrders) {
        this.productOrders = productOrders;
    }*/

    @Override
    public String toString() {
        return "MewsChargeRequest{" +
                "customerId='" + customerId + '\'' +
                ", items=" + items +
                ", notes='" + notes + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
