package uk.co.epicuri.serverapi.management.model;

import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Floor;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionIdStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RestaurantFacade {
    //controller 1
    private String name;
    private boolean autosetId;
    private Integer manuallySetStaffId;
    private SessionIdStrategy sessionIdStrategy = SessionIdStrategy.HASH;
    private Integer startSessionIdFrom;

    //controller 2
    private Address address = new Address();
    private Double longitude;
    private Double latitude;
    private String description, telephone1, telephone2, publicEmail, internalEmail, website;
    private Cuisine cuisine;
    private Country country;

    //controller3
    private String vatNumber, guestImageURL, receiptFooterField;
    private ReceiptType receiptType;
    private File receiptImageFile;
    private String currency;
    private String timezone;
    private TakeawayOfferingType takeawayOfferingType = TakeawayOfferingType.NOT_OFFERED;

    //controller4
    private List<Printer> printers = new ArrayList<>();
    private Printer defaultBillingPrinter;
    private Printer takeawayPrinter;
    private List<FloorFacade> floors = new ArrayList<>();


    public RestaurantFacade() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutosetId() {
        return autosetId;
    }

    public void setAutosetId(boolean autosetId) {
        this.autosetId = autosetId;
    }

    public Integer getManuallySetStaffId() {
        return manuallySetStaffId;
    }

    public void setManuallySetStaffId(Integer manuallySetStaffId) {
        this.manuallySetStaffId = manuallySetStaffId;
    }

    public SessionIdStrategy getSessionIdStrategy() {
        return sessionIdStrategy;
    }

    public void setSessionIdStrategy(SessionIdStrategy sessionIdStrategy) {
        this.sessionIdStrategy = sessionIdStrategy;
    }

    public Integer getStartSessionIdFrom() {
        return startSessionIdFrom;
    }

    public void setStartSessionIdFrom(Integer startSessionIdFrom) {
        this.startSessionIdFrom = startSessionIdFrom;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTelephone1() {
        return telephone1;
    }

    public void setTelephone1(String telephone1) {
        this.telephone1 = telephone1;
    }

    public String getTelephone2() {
        return telephone2;
    }

    public void setTelephone2(String telephone2) {
        this.telephone2 = telephone2;
    }

    public String getPublicEmail() {
        return publicEmail;
    }

    public void setPublicEmail(String publicEmail) {
        this.publicEmail = publicEmail;
    }

    public String getInternalEmail() {
        return internalEmail;
    }

    public void setInternalEmail(String internalEmail) {
        this.internalEmail = internalEmail;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Cuisine getCuisine() {
        return cuisine;
    }

    public void setCuisine(Cuisine cuisine) {
        this.cuisine = cuisine;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getGuestImageURL() {
        return guestImageURL;
    }

    public void setGuestImageURL(String guestImageURL) {
        this.guestImageURL = guestImageURL;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public File getReceiptImageFile() {
        return receiptImageFile;
    }

    public void setReceiptImageFile(File receiptImageFile) {
        this.receiptImageFile = receiptImageFile;
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

    public TakeawayOfferingType getTakeawayOfferingType() {
        return takeawayOfferingType;
    }

    public void setTakeawayOfferingType(TakeawayOfferingType takeawayOfferingType) {
        this.takeawayOfferingType = takeawayOfferingType;
    }

    public String getReceiptFooterField() {
        return receiptFooterField;
    }

    public void setReceiptFooterField(String receiptFooterField) {
        this.receiptFooterField = receiptFooterField;
    }

    public List<Printer> getPrinters() {
        return printers;
    }

    public void setPrinters(List<Printer> printers) {
        this.printers = printers;
    }

    public Printer getDefaultBillingPrinter() {
        return defaultBillingPrinter;
    }

    public void setDefaultBillingPrinter(Printer defaultBillingPrinter) {
        this.defaultBillingPrinter = defaultBillingPrinter;
    }

    public Printer getTakeawayPrinter() {
        return takeawayPrinter;
    }

    public void setTakeawayPrinter(Printer takeawayPrinter) {
        this.takeawayPrinter = takeawayPrinter;
    }

    public List<FloorFacade> getFloors() {
        return floors;
    }

    public void setFloors(List<FloorFacade> floors) {
        this.floors = floors;
    }
}