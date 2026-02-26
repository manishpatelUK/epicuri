package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCustomerView { //evil name
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private Name name;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("AuthKey")
    private String authKey;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("Birthday")
    private Long birthday;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("FavouriteFood")
    private String favouriteFood;

    @JsonProperty("FavouriteDrink")
    private String favouriteDrink;

    @JsonProperty("HatedFood")
    private String hatedFood;

    @JsonProperty("DietaryRequirements")
    private List<String> dietaryRequirements = new ArrayList<>(); //ids

    @JsonProperty("Allergies")
    private List<String> allergies = new ArrayList<>(); //ids

    @JsonProperty("FoodPreferences")
    private List<String> foodPreferences = new ArrayList<>(); //ids

    @JsonProperty("Password")
    private String password;

    @JsonProperty("ccData")
    private CreditCardData ccData;

    public CustomerCustomerView(){}

    public CustomerCustomerView(Customer customer){
        this.id = customer.getId();
        this.name = new Name(customer.getFirstName(), customer.getLastName());
        this.email = customer.getEmail();
        this.authKey = customer.getAuthKey();
        this.address = customer.getAddress();
        if(customer.getBirthday() != null) {
            this.birthday = customer.getBirthday() / 1000;
        }
        this.phoneNumber = "0" + customer.getPhoneNumber();
        this.favouriteFood = customer.getFavouriteFood();
        this.favouriteDrink = customer.getFavouriteDrink();
        this.hatedFood = customer.getHatedFood();
        this.dietaryRequirements = customer.getDietaryRequirements();
        this.allergies = customer.getAllergies();
        this.foodPreferences = customer.getFoodPreferences();
        this.ccData = customer.getCcData();
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

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Long getBirthday() {
        return birthday;
    }

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public List<String> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(List<String> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(List<String> foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CreditCardData getCcData() {
        return ccData;
    }

    public void setCcData(CreditCardData ccData) {
        this.ccData = ccData;
    }
}
