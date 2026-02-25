package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.*;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

@Document(collection = TableNames.STAFF)
public class Staff extends Deletable {
    @MgmtDisplayField
    private String name;
    private String pin;
    private String userName;

    @MgmtDisplayName(name = "Password")
    @MgmtPassword
    private String mash;
    private StaffRole role = StaffRole.WAIT_STAFF;

    @MgmtIgnoreField
    @Transient
    private transient Restaurant restaurant; //for convenience

    @MgmtEditableField(editable = false)
    @Indexed
    private String restaurantId;

    @MgmtDisplayName(name = "PaymentSense ID")
    private String paymentSenseId;

    public Staff(){}

    public Staff(StaffView staffView, String restaurantId) {
        this.name = staffView.getName();
        this.pin = staffView.getPin();
        this.userName = staffView.getUsername();
        if(staffView.getRole() != null) {
            this.role = StaffRole.valueOf(staffView.getRole());
        }
        else {
            this.role = staffView.isManager() ? StaffRole.MANAGER : StaffRole.WAIT_STAFF;
        }
        this.restaurantId = restaurantId;
        this.paymentSenseId = staffView.getPaymentSenseId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getMash() {
        return mash;
    }

    public void setMash(String mash) {
        this.mash = mash;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getPaymentSenseId() {
        return paymentSenseId;
    }

    public void setPaymentSenseId(String paymentSenseId) {
        this.paymentSenseId = paymentSenseId;
    }
}
