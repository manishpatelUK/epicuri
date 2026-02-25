package uk.co.epicuri.serverapi.common.pojo.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.Address;

public class OnlineOrderingAuthResponse {
    private String restaurantName;
    private String restaurantPhoneNumber;
    private String restaurantImage;
    private String token;
    private String currencySymbol;
    private String isoCurrency;
    private String stripePublicKey;
    private int takeawayMinimumTime;
    private double maxOrderValue;
    private double minOrderValue;
    private double maxWithoutCC;
    private Address address;

    //special
    private String caymanAPIKey;
    private String caymanAuth;

    public OnlineOrderingAuthResponse(){}
    public OnlineOrderingAuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getStripePublicKey() {
        return stripePublicKey;
    }

    public void setStripePublicKey(String stripePublicKey) {
        this.stripePublicKey = stripePublicKey;
    }

    public double getMaxOrderValue() {
        return maxOrderValue;
    }

    public void setMaxOrderValue(double maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public int getTakeawayMinimumTime() {
        return takeawayMinimumTime;
    }

    public void setTakeawayMinimumTime(int takeawayMinimumTime) {
        this.takeawayMinimumTime = takeawayMinimumTime;
    }

    public String getIsoCurrency() {
        return isoCurrency;
    }

    public void setIsoCurrency(String isoCurrency) {
        this.isoCurrency = isoCurrency;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getRestaurantPhoneNumber() {
        return restaurantPhoneNumber;
    }

    public void setRestaurantPhoneNumber(String restaurantPhoneNumber) {
        this.restaurantPhoneNumber = restaurantPhoneNumber;
    }

    public double getMaxWithoutCC() {
        return maxWithoutCC;
    }

    public void setMaxWithoutCC(double maxWithoutCC) {
        this.maxWithoutCC = maxWithoutCC;
    }

    public String getCaymanAPIKey() {
        return caymanAPIKey;
    }

    public void setCaymanAPIKey(String caymanAPIKey) {
        this.caymanAPIKey = caymanAPIKey;
    }

    public String getCaymanAuth() {
        return caymanAuth;
    }

    public void setCaymanAuth(String caymanAuth) {
        this.caymanAuth = caymanAuth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("restaurantName", restaurantName)
                .append("restaurantPhoneNumber", restaurantPhoneNumber)
                .append("restaurantImage", restaurantImage)
                .append("token", token)
                .append("currencySymbol", currencySymbol)
                .append("isoCurrency", isoCurrency)
                .append("stripePublicKey", stripePublicKey)
                .append("takeawayMinimumTime", takeawayMinimumTime)
                .append("maxOrderValue", maxOrderValue)
                .append("minOrderValue", minOrderValue)
                .append("maxWithoutCC", maxWithoutCC)
                .append("address", address)
                .toString();
    }
}
