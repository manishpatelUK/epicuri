package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.host.HostOpeningHoursView;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRestaurantView implements Comparable<CustomerRestaurantView> {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Website")
    private String website;

    @JsonProperty("EnabledForDiner")
    private boolean enabledForDiner;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Position")
    private LatLongPair position;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("TakeawayOffered")
    private Integer takeawayOffered;

    @JsonProperty("TakeawayMenuId")
    private String takeawayMenuId;

    @JsonProperty("CategoryId")
    private String categoryId;

    @JsonProperty("RestaurantDefaults")
    private Map<String,String> restaurantDefaults;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Timezone")
    private String timezone;

    @JsonProperty("HasItemImages")
    private boolean hasItemImages = false;

    @JsonProperty("imageURLs")
    private List<String> imageURLs = new ArrayList<>();

    @JsonProperty("openingHours")
    private HostOpeningHoursView openingHours;

    @JsonProperty("distanceKm")
    private Double distance;

    @JsonProperty("showImagesForItems")
    private boolean showImagesForItems = false;

    @JsonProperty("guestLogoURL")
    private String guestLogoURL;

    private boolean paymentsEnabled;

    public CustomerRestaurantView(){}

    public CustomerRestaurantView(Restaurant restaurant) {
        setAddress(restaurant.getAddress());
        setCategoryId(restaurant.getCuisineId());
        setCurrency(restaurant.getISOCurrency());
        setDescription(restaurant.getDescription());
        setEmail(restaurant.getPublicEmailAddress());
        setEnabledForDiner(restaurant.isEnabledForDiner());
        setId(restaurant.getId());
        setName(restaurant.getName());
        setPhoneNumber(restaurant.getPhoneNumber1());
        setPosition(restaurant.getPosition());
        setRestaurantDefaults(restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, RestaurantDefault::convertValueToString)));
        setTakeawayMenuId(restaurant.getTakeawayMenu());
        setTakeawayOffered(restaurant.getTakeawayOffered().getApiExpose());
        setTimezone(restaurant.getIANATimezone());
        setWebsite(restaurant.getWebsite());
        setHasItemImages(restaurant.isHasItemImages());
        this.imageURLs.addAll(restaurant.getImageURLs());
        this.showImagesForItems = restaurant.isHasItemImages();
        this.guestLogoURL = restaurant.getGuestLogoURL();

        if(!restaurant.getIntegrations().containsKey(ExternalIntegration.STRIPE)
                || restaurant.getIntegrations().get(ExternalIntegration.STRIPE).getToken() == null
                || restaurant.getISOCurrency() == null) {
            paymentsEnabled = false;
        } else {
            paymentsEnabled = true;
        }
    }

    public CustomerRestaurantView(Restaurant restaurant, OpeningHours openingHours) {
        this(restaurant);
        if(openingHours != null) {
            this.openingHours = new HostOpeningHoursView(openingHours);
        }
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isEnabledForDiner() {
        return enabledForDiner;
    }

    public void setEnabledForDiner(boolean enabledForDiner) {
        this.enabledForDiner = enabledForDiner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LatLongPair getPosition() {
        return position;
    }

    public void setPosition(LatLongPair position) {
        this.position = position;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getTakeawayOffered() {
        return takeawayOffered;
    }

    public void setTakeawayOffered(Integer takeawayOffered) {
        this.takeawayOffered = takeawayOffered;
    }

    public String getTakeawayMenuId() {
        return takeawayMenuId;
    }

    public void setTakeawayMenuId(String takeawayMenuId) {
        this.takeawayMenuId = takeawayMenuId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Map<String, String> getRestaurantDefaults() {
        return restaurantDefaults;
    }

    public void setRestaurantDefaults(Map<String, String> restaurantDefaults) {
        this.restaurantDefaults = restaurantDefaults;
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

    public HostOpeningHoursView getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(HostOpeningHoursView openingHours) {
        this.openingHours = openingHours;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public boolean isShowImagesForItems() {
        return showImagesForItems;
    }

    public void setShowImagesForItems(boolean showImagesForItems) {
        this.showImagesForItems = showImagesForItems;
    }

    public String getGuestLogoURL() {
        return guestLogoURL;
    }

    public void setGuestLogoURL(String guestLogoURL) {
        this.guestLogoURL = guestLogoURL;
    }

    public boolean isPaymentsEnabled() {
        return paymentsEnabled;
    }

    public void setPaymentsEnabled(boolean paymentsEnabled) {
        this.paymentsEnabled = paymentsEnabled;
    }

    @Override
    public int compareTo(CustomerRestaurantView other) {
        if(this.name == null || other == null || other.name == null) {
            return 0;
        }
        return this.name.compareTo(other.name);
    }
}
