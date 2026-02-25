package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.authentication.StaffAuthentication;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffView {
    @JsonProperty("AuthKey")
    private String authKey;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Username")
    private String username;

    @JsonProperty("Manager")
    private boolean manager; //soon to be legacy

    @JsonProperty("Role")
    private String role; //new

    @JsonProperty("Pin")
    private String pin;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("PaymentSenseId")
    private String paymentSenseId = "1";

    public StaffView(){}
    public StaffView(Staff staff, String authKey) {
        this.id = staff.getId();
        this.name = staff.getName();
        this.username = staff.getUserName();
        this.manager = staff.getRole().isHigherOrEqualSecurityLevelThan(StaffRole.MANAGER);
        this.role = staff.getRole().toString();
        this.authKey = authKey;
        this.pin = staff.getPin();
        this.paymentSenseId = staff.getPaymentSenseId();
    }

    public StaffView(Staff staff) {
        this.id = staff.getId();
        this.name = staff.getName();
        this.username = staff.getUserName();
        this.manager = staff.getRole().isHigherOrEqualSecurityLevelThan(StaffRole.MANAGER);
        this.role = staff.getRole().toString();
        this.pin = staff.getPin();
        this.paymentSenseId = staff.getPaymentSenseId();
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPaymentSenseId() {
        return paymentSenseId;
    }

    public void setPaymentSenseId(String paymentSenseId) {
        this.paymentSenseId = paymentSenseId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
