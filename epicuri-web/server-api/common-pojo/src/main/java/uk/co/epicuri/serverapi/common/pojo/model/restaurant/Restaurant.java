package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.*;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionIdStrategy;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = TableNames.RESTAURANTS)
public class Restaurant extends Deletable {
    @Transient
    @MgmtIgnoreField
    public transient static final String RECEIPT_IMAGE_ENDPOINT = "Restaurants/receiptImage";

    @MgmtDisplayField
    @Indexed
    private String name;

    @Indexed(unique = true)
    private String staffFacingId;

    @MgmtLong2DateConvert
    @MgmtEditableField(editable = false)
    private long creationTime = System.currentTimeMillis();
    private Address address;

    @MgmtExternalId(externalClass = Cuisine.class, endpoint = "Cuisines", restrictOnParentId = false)
    private String cuisineId;
    private String description;

    @MgmtExternalId(externalClass = Country.class, endpoint = "uk.co.epicuri.serverapi.common.pojo.model.Country", restrictOnParentId = false)
    private String countryId;

    private String phoneNumber1;
    private String phoneNumber2;
    private String publicEmailAddress;
    private String internalEmailAddress;
    private LatLongPair position;
    private boolean enabledForWaiter;
    private boolean enabledForDiner;
    private boolean tablesEnabled = true;
    private String website;
    private String vatNumber;
    private String receiptFooter;

    private boolean hasItemImages = false;

    @MgmtFileOpener(endpointHint = RECEIPT_IMAGE_ENDPOINT)
    private String receiptImageURL;

    @MgmtExternalId(externalClass = Printer.class, endpoint = "Printers", restrictOnParentId = true)
    private String defaultTakeawayPrinterId;

    @MgmtExternalId(externalClass = Printer.class, endpoint = "Printers", restrictOnParentId = true)
    private String defaultBillingPrinterId;

    private TakeawayOfferingType takeawayOffered = TakeawayOfferingType.NOT_OFFERED;

    @MgmtIntegrationsMap
    private Map<ExternalIntegration, KVData> integrations = new HashMap<>();
    private ReceiptType receiptType = ReceiptType.NORMAL;

    @MgmtInternalList(file = "currencies.txt")
    private String ISOCurrency;

    @MgmtInternalList(file = "timezones.txt")
    private String IANATimezone;

    @MgmtExternalId(externalClass = AdjustmentType.class, endpoint = "uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType", restrictOnParentId = false, listView = true)
    private List<String> adjustmentTypes = new ArrayList<>();
    private List<Floor> floors = new ArrayList<>();
    private List<Service> services = new ArrayList<>();

    @MgmtDisplayName(name = "Default Overrides")
    private List<RestaurantDefault> restaurantDefaults = new ArrayList<>();

    @MgmtIgnoreField
    private List<Table> tables = new ArrayList<>();

    @MgmtExternalId(externalClass = Menu.class, endpoint = "Menus", restrictOnParentId = true, listView = false)
    private String takeawayMenu;

    @MgmtDisplayName(name = "Session ID Strategy")
    private SessionIdStrategy sessionIdStrategy = SessionIdStrategy.HASH;

    @MgmtDisplayName(name = "Restaurant Image URLs")
    private List<String> imageURLs = new ArrayList<>();

    @MgmtDisplayName(name = "Logo image (guest app)")
    private String guestLogoURL;

    @MgmtIgnoreField
    private StaffPermissions staffPermissions;

    @Indexed(sparse = true)
    private String headOfficeId;

    @MgmtExternalId(externalClass = Printer.class, endpoint = "Printers", restrictOnParentId = false, listView = true, restrictOnRestaurantId = true)
    private List<String> cashDrawerPrinters = new ArrayList<>();

    @MgmtExternalId(externalClass = Printer.class, endpoint = "Printers", restrictOnParentId = true)
    private String defaultCourseAwayPrinterId;

    @MgmtDisplayName(name = "Tax Code Mappings, our ID[comma]external ID")
    private List<String> taxMappings = new ArrayList<>(); // takes the form key,value for Mews

    private boolean headOffice = false;

    private boolean allowQuickSwitch = true;

    private List<String> onlineOrderingIPAddresses = new ArrayList<>();

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

    public String getCuisineId() {
        return cuisineId;
    }

    public void setCuisineId(String cuisineId) {
        this.cuisineId = cuisineId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
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

    public String getPublicEmailAddress() {
        return publicEmailAddress;
    }

    public void setPublicEmailAddress(String publicEmailAddress) {
        this.publicEmailAddress = publicEmailAddress;
    }

    public String getInternalEmailAddress() {
        return internalEmailAddress;
    }

    public void setInternalEmailAddress(String internalEmailAddress) {
        this.internalEmailAddress = internalEmailAddress;
    }

    public LatLongPair getPosition() {
        return position;
    }

    public void setPosition(LatLongPair position) {
        this.position = position;
    }

    public boolean isEnabledForWaiter() {
        return enabledForWaiter;
    }

    public void setEnabledForWaiter(boolean enabledForWaiter) {
        this.enabledForWaiter = enabledForWaiter;
    }

    public boolean isEnabledForDiner() {
        return enabledForDiner;
    }

    public void setEnabledForDiner(boolean enabledForDiner) {
        this.enabledForDiner = enabledForDiner;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getReceiptFooter() {
        return receiptFooter;
    }

    public void setReceiptFooter(String receiptFooter) {
        this.receiptFooter = receiptFooter;
    }

    public String getReceiptImageURL() {
        return receiptImageURL;
    }

    public void setReceiptImageURL(String receiptImageURL) {
        this.receiptImageURL = receiptImageURL;
    }

    public String getDefaultTakeawayPrinterId() {
        return defaultTakeawayPrinterId;
    }

    public void setDefaultTakeawayPrinterId(String defaultTakeawayPrinterId) {
        this.defaultTakeawayPrinterId = defaultTakeawayPrinterId;
    }

    public String getDefaultBillingPrinterId() {
        return defaultBillingPrinterId;
    }

    public void setDefaultBillingPrinterId(String defaultBillingPrinterId) {
        this.defaultBillingPrinterId = defaultBillingPrinterId;
    }

    public TakeawayOfferingType getTakeawayOffered() {
        return takeawayOffered;
    }

    public void setTakeawayOffered(TakeawayOfferingType takeawayOffered) {
        this.takeawayOffered = takeawayOffered;
    }

    public Map<ExternalIntegration, KVData> getIntegrations() {
        return integrations;
    }

    public void setIntegrations(Map<ExternalIntegration, KVData> integrations) {
        this.integrations = integrations;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public String getISOCurrency() {
        return ISOCurrency;
    }

    public void setISOCurrency(String ISOCurrency) {
        this.ISOCurrency = ISOCurrency;
    }

    public String getIANATimezone() {
        return IANATimezone;
    }

    public void setIANATimezone(String IANATimezone) {
        this.IANATimezone = IANATimezone;
    }

    public String getTakeawayMenu() {
        return takeawayMenu;
    }

    public void setTakeawayMenu(String takeawayMenu) {
        this.takeawayMenu = takeawayMenu;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public List<String> getAdjustmentTypes() {
        return adjustmentTypes;
    }

    public void setAdjustmentTypes(List<String> adjustmentTypes) {
        this.adjustmentTypes = adjustmentTypes;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public String getStaffFacingId() {
        return staffFacingId;
    }

    public void setStaffFacingId(String staffFacingId) {
        this.staffFacingId = staffFacingId;
    }

    public List<RestaurantDefault> getRestaurantDefaults() {
        return restaurantDefaults;
    }

    public void setRestaurantDefaults(List<RestaurantDefault> restaurantDefaults) {
        this.restaurantDefaults = restaurantDefaults;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public SessionIdStrategy getSessionIdStrategy() {
        return sessionIdStrategy;
    }

    public void setSessionIdStrategy(SessionIdStrategy sessionIdStrategy) {
        this.sessionIdStrategy = sessionIdStrategy;
    }

    public boolean isHasItemImages() {
        return hasItemImages;
    }

    public void setHasItemImages(boolean hasItemImages) {
        this.hasItemImages = hasItemImages;
    }

    public List<String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public String getGuestLogoURL() {
        return guestLogoURL;
    }

    public void setGuestLogoURL(String guestLogoURL) {
        this.guestLogoURL = guestLogoURL;
    }

    public boolean isTablesEnabled() {
        return tablesEnabled;
    }

    public void setTablesEnabled(boolean tablesEnabled) {
        this.tablesEnabled = tablesEnabled;
    }

    public StaffPermissions getStaffPermissions() {
        return staffPermissions;
    }

    public void setStaffPermissions(StaffPermissions staffPermissions) {
        this.staffPermissions = staffPermissions;
    }

    public List<String> getOnlineOrderingIPAddresses() {
        return onlineOrderingIPAddresses;
    }

    public void setOnlineOrderingIPAddresses(List<String> onlineOrderingIPAddresses) {
        this.onlineOrderingIPAddresses = onlineOrderingIPAddresses;
    }

    public List<String> getTaxMappings() {
        return taxMappings;
    }

    public void setTaxMappings(List<String> taxMappings) {
        this.taxMappings = taxMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass() && EqualsBuilder.reflectionEquals(this, o);

    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean isAllowQuickSwitch() {
        return allowQuickSwitch;
    }

    public void setAllowQuickSwitch(boolean allowQuickSwitch) {
        this.allowQuickSwitch = allowQuickSwitch;
    }

    public String getHeadOfficeId() {
        return headOfficeId;
    }

    public void setHeadOfficeId(String headOfficeId) {
        this.headOfficeId = headOfficeId;
    }

    public boolean isHeadOffice() {
        return headOffice;
    }

    public void setHeadOffice(boolean headOffice) {
        this.headOffice = headOffice;
    }

    public List<String> getCashDrawerPrinters() {
        return cashDrawerPrinters;
    }

    public void setCashDrawerPrinters(List<String> cashDrawerPrinters) {
        this.cashDrawerPrinters = cashDrawerPrinters;
    }

    public String getDefaultCourseAwayPrinterId() {
        return defaultCourseAwayPrinterId;
    }

    public void setDefaultCourseAwayPrinterId(String defaultCourseAwayPrinterId) {
        this.defaultCourseAwayPrinterId = defaultCourseAwayPrinterId;
    }
}
