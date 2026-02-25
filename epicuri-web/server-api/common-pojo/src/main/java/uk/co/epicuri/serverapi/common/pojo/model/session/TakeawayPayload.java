package uk.co.epicuri.serverapi.common.pojo.model.session;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Address;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TakeawayPayload {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("RestaurantId")
    private String restaurantId;

    @JsonProperty("Delivery")
    private boolean delivery;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("ExpectedTime")
    private long expectedTime; //was double, in seconds

    @JsonProperty("RequestedTime")
    private long requestedTime; //was double, in seconds

    @JsonProperty("Accepted")
    private boolean accepted;

    @JsonProperty("Total")
    private double total;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("Telephone")
    private String telephone;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("LeadCustomerId")
    private String leadCustomerId;

    @JsonProperty("Paid")
    private boolean paid;

    @JsonProperty("RequestedBill")
    private boolean requestedBill;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        this.expectedTime = expectedTime;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public long getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(long requestedTime) {
        this.requestedTime = requestedTime;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeadCustomerId() {
        return leadCustomerId;
    }

    public void setLeadCustomerId(String leadCustomerId) {
        this.leadCustomerId = leadCustomerId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isRequestedBill() {
        return requestedBill;
    }

    public void setRequestedBill(boolean requestedBill) {
        this.requestedBill = requestedBill;
    }
}
