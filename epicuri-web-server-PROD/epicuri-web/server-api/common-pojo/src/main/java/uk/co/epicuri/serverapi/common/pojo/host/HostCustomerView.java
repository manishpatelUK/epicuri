package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerBlackMarkUtil;
import uk.co.epicuri.serverapi.common.pojo.customer.Name;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostCustomerView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private Name name;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("FavouriteFood")
    private String favouriteFood;

    @JsonProperty("FavouriteDrink")
    private String favouriteDrink;

    @JsonProperty("HatedFood")
    private String hatedFood;

    @JsonProperty("Allergies")
    private List<PreferenceName> allergies = new ArrayList<>();

    @JsonProperty("DietaryRequirements")
    private List<PreferenceName> dietaryRequirements = new ArrayList<>();

    @JsonProperty("FoodPreferences")
    private List<PreferenceName> foodPreferences = new ArrayList<>();

    @JsonProperty("IsBlackMarked")
    private boolean blackMarked;

    public HostCustomerView(){}
    public HostCustomerView(Customer customer, Map<String,Preference> allPreferences) {
        this.id = customer.getId();
        this.name = new Name();
        this.name.setFirstName((StringUtils.isBlank(customer.getFirstName()) ? "" : customer.getFirstName()).trim());
        this.name.setLastName((StringUtils.isBlank(customer.getLastName()) ? "" : customer.getLastName()).trim());
        this.phoneNumber = customer.getInternationalCode() != null ? "(" + customer.getInternationalCode() + ")" + "0" + customer.getPhoneNumber()
                                                                    : "0" + customer.getPhoneNumber();
        this.address = customer.getAddress();
        this.favouriteFood = customer.getFavouriteFood();
        this.favouriteDrink = customer.getFavouriteDrink();
        this.hatedFood = customer.getHatedFood();

        this.allergies.addAll(customer.getAllergies().stream().filter(allPreferences::containsKey).map(x -> new PreferenceName(allPreferences.get(x).getName())).collect(Collectors.toList()));
        this.dietaryRequirements.addAll(customer.getDietaryRequirements().stream().filter(allPreferences::containsKey).map(x -> new PreferenceName(allPreferences.get(x).getName())).collect(Collectors.toList()));
        this.foodPreferences.addAll(customer.getFoodPreferences().stream().filter(allPreferences::containsKey).map(x -> new PreferenceName(allPreferences.get(x).getName())).collect(Collectors.toList()));

        this.blackMarked = CustomerBlackMarkUtil.exceedsBlackMarks(customer);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getFavouriteFood() {
        return favouriteFood;
    }

    public void setFavouriteFood(String favouriteFood) {
        this.favouriteFood = favouriteFood;
    }

    public String getFavouriteDrink() {
        return favouriteDrink;
    }

    public void setFavouriteDrink(String favouriteDrink) {
        this.favouriteDrink = favouriteDrink;
    }

    public String getHatedFood() {
        return hatedFood;
    }

    public void setHatedFood(String hatedFood) {
        this.hatedFood = hatedFood;
    }

    public List<PreferenceName> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<PreferenceName> allergies) {
        this.allergies = allergies;
    }

    public List<PreferenceName> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(List<PreferenceName> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public List<PreferenceName> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(List<PreferenceName> foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public boolean isBlackMarked() {
        return blackMarked;
    }

    public void setBlackMarked(boolean blackMarked) {
        this.blackMarked = blackMarked;
    }

    public static class PreferenceName {
        @JsonProperty("Name")
        private String name;

        public PreferenceName(){}

        public PreferenceName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
