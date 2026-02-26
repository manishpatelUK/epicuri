package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseConstants;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.WaiterAppFeature;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostRestaurantView {
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Website")
    private String website;

    @JsonProperty("Telephone1")
    private String phoneNumber1;

    @JsonProperty("Telephone2")
    private String phoneNumber2;

    @JsonProperty("VATNumber")
    private String vatNumber;

    @JsonProperty("ReceiptFooter")
    private String recieptFooter;

    @JsonProperty("TakeawayMenuId")
    private String takeawayMenuId;

    @JsonProperty("ReceiptImageURL")
    private String recieptImageURL;

    @JsonProperty("TakeawayPrinterId")
    private String takeawayPrinterId;

    @JsonProperty("BillingPrinterId")
    private String billingPrinterId;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Timezone")
    private String timezone;

    @JsonProperty("MewsIntegration")
    private boolean mewsEnabled;

    @JsonProperty("ReceiptType")
    private int recieptType;

    @JsonProperty("TakeawayOffered")
    private int takeawayOffered;

    @JsonProperty("AdjustmentTypes")
    private List<HostAdjustmentTypeView> adjustmentTypes = new ArrayList<>();

    @JsonProperty("RestaurantDefaults")
    private Map<String,Object> restaurantDefaults = new HashMap<>();

    @JsonProperty("PDQIntegrationPaymentAdjustment")
    private String pdqIntegrationPaymentAdjustment;

    @JsonProperty("PaymentSense")
    private Map<String,String> paymentSense;

    @JsonProperty("permissions")
    private Map<WaiterAppFeature,Boolean> permissions;

    @JsonProperty("cashDrawerConnectedPrinters")
    private List<String> cashDrawerConnectedPrinters = new ArrayList<>();

    @JsonProperty("defaultCourseAwayPrinterId")
    private String defaultCourseAwayPrinterId;

    private String staffFacingId;

    public HostRestaurantView(){}
    public HostRestaurantView(Restaurant restaurant, Map<String,AdjustmentType> adjustmentTypeMap, String apiURL, Map<WaiterAppFeature,Boolean> permissions) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.description = restaurant.getDescription();
        this.address = restaurant.getAddress();
        this.email = restaurant.getPublicEmailAddress();
        this.website = restaurant.getWebsite();
        this.phoneNumber1 = restaurant.getPhoneNumber1();
        this.phoneNumber2 = restaurant.getPhoneNumber2();
        this.vatNumber = restaurant.getVatNumber();
        this.recieptFooter = restaurant.getReceiptFooter();
        this.takeawayMenuId = restaurant.getTakeawayMenu();
        this.recieptImageURL = apiURL + "/Restaurant/BillLogo/" + restaurant.getReceiptImageURL();
        this.takeawayPrinterId = restaurant.getDefaultTakeawayPrinterId();
        this.billingPrinterId = restaurant.getDefaultBillingPrinterId();
        this.currency = restaurant.getISOCurrency();
        this.timezone = restaurant.getIANATimezone();
        this.mewsEnabled = restaurant.getIntegrations().containsKey(ExternalIntegration.MEWS)
                            && adjustmentTypeMap.values().stream().anyMatch(a -> a.getName().equals(MewsConstants.MEWS_ADJUSTMENT_TYPE));
        this.recieptType = restaurant.getReceiptType().getApiExpose();
        this.takeawayOffered = restaurant.getTakeawayOffered().getApiExpose();
        restaurant.getAdjustmentTypes().forEach(a -> {
            AdjustmentType adjustmentType = adjustmentTypeMap.get(a);
            if(adjustmentType != null) {
                adjustmentTypes.add(new HostAdjustmentTypeView(adjustmentType));

                //if there is a PaymentSense integration, add it in
                if (adjustmentType.getName() != null
                        && adjustmentType.getName().equals(PaymentSenseConstants.PS_ADJUSTMENT_TYPE)
                        && restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
                    pdqIntegrationPaymentAdjustment = a;
                }
            }
        });

        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            paymentSense = new HashMap<>();
            final KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE);
            paymentSense.put("host", kvData.getHost());
            paymentSense.put("key", kvData.getKey());
        }

        this.permissions = permissions;
        this.cashDrawerConnectedPrinters = restaurant.getCashDrawerPrinters();
        this.defaultCourseAwayPrinterId = restaurant.getDefaultCourseAwayPrinterId();
        this.staffFacingId = restaurant.getStaffFacingId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email != null && ControllerUtil.EMAIL_REGEX.matcher(email).matches()) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhoneNumber1() {
        return phoneNumber1;
    }

    public void setPhoneNumber1(String phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    public String getPhoneNumber2() {
        return phoneNumber2;
    }

    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getRecieptFooter() {
        return recieptFooter;
    }

    public void setRecieptFooter(String recieptFooter) {
        this.recieptFooter = recieptFooter;
    }

    public String getTakeawayMenuId() {
        return takeawayMenuId;
    }

    public void setTakeawayMenuId(String takeawayMenuId) {
        this.takeawayMenuId = takeawayMenuId;
    }

    public String getRecieptImageURL() {
        return recieptImageURL;
    }

    public void setRecieptImageURL(String recieptImageURL) {
        this.recieptImageURL = recieptImageURL;
    }

    public String getTakeawayPrinterId() {
        return takeawayPrinterId;
    }

    public void setTakeawayPrinterId(String takeawayPrinterId) {
        this.takeawayPrinterId = takeawayPrinterId;
    }

    public String getBillingPrinterId() {
        return billingPrinterId;
    }

    public void setBillingPrinterId(String billingPrinterId) {
        this.billingPrinterId = billingPrinterId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isMewsEnabled() {
        return mewsEnabled;
    }

    public void setMewsEnabled(boolean mewsEnabled) {
        this.mewsEnabled = mewsEnabled;
    }

    public int getRecieptType() {
        return recieptType;
    }

    public void setRecieptType(int recieptType) {
        this.recieptType = recieptType;
    }

    public int getTakeawayOffered() {
        return takeawayOffered;
    }

    public void setTakeawayOffered(int takeawayOffered) {
        this.takeawayOffered = takeawayOffered;
    }

    public Map<String, Object> getRestaurantDefaults() {
        return restaurantDefaults;
    }

    public void setRestaurantDefaults(Map<String, Object> restaurantDefaults) {
        this.restaurantDefaults = restaurantDefaults;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HostAdjustmentTypeView> getAdjustmentTypes() {
        return adjustmentTypes;
    }

    public void setAdjustmentTypes(List<HostAdjustmentTypeView> adjustmentTypes) {
        this.adjustmentTypes = adjustmentTypes;
    }

    public String getPdqIntegrationPaymentAdjustment() {
        return pdqIntegrationPaymentAdjustment;
    }

    public Map<String, String> getPaymentSense() {
        return paymentSense;
    }

    public void setPaymentSense(Map<String, String> paymentSense) {
        this.paymentSense = paymentSense;
    }

    public Map<WaiterAppFeature, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<WaiterAppFeature, Boolean> permissions) {
        this.permissions = permissions;
    }

    public List<String> getCashDrawerConnectedPrinters() {
        return cashDrawerConnectedPrinters;
    }

    public void setCashDrawerConnectedPrinters(List<String> cashDrawerConnectedPrinters) {
        this.cashDrawerConnectedPrinters = cashDrawerConnectedPrinters;
    }

    public String getDefaultCourseAwayPrinterId() {
        return defaultCourseAwayPrinterId;
    }

    public void setDefaultCourseAwayPrinterId(String defaultCourseAwayPrinterId) {
        this.defaultCourseAwayPrinterId = defaultCourseAwayPrinterId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStaffFacingId() {
        return staffFacingId;
    }

    public void setStaffFacingId(String staffFacingId) {
        this.staffFacingId = staffFacingId;
    }
}
