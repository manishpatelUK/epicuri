package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Address;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerTakeawayOrderRequest {
    @JsonProperty("RestaurantId")
    private String restaurantId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Telephone")
    private String telephone;

    @JsonProperty("RequestedTime")
    private long requestedTime;

    @JsonProperty("timeSlot")
    private String timeSlot;

    @JsonProperty("dateSlot")
    private int dateSlot = 0;

    @JsonProperty("Delivery")
    private boolean delivery;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("InstantiatedFromId")
    private int instantiatedFromId;

    @JsonProperty("Items")
    private List<CustomerOrderItemView> items = new ArrayList<>();

    @JsonProperty("payByCC")
    private boolean payByCC = false;

    @JsonProperty("chargeId")
    private String chargeId;

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public long getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(long requestedTime) {
        this.requestedTime = requestedTime;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getInstantiatedFromId() {
        return instantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        this.instantiatedFromId = instantiatedFromId;
    }

    public List<CustomerOrderItemView> getItems() {
        return items;
    }

    public void setItems(List<CustomerOrderItemView> items) {
        this.items = items;
    }

    public boolean isPayByCC() {
        return payByCC;
    }

    public void setPayByCC(boolean payByCC) {
        this.payByCC = payByCC;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDateSlot() {
        return dateSlot;
    }

    public void setDateSlot(int dateSlot) {
        this.dateSlot = dateSlot;
    }
}
