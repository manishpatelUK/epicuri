package uk.co.epicuri.serverapi.common.pojo.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.customer.SMSRegistrationView;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.ArrayList;
import java.util.List;

@Document(collection = TableNames.CUSTOMERS)
public class Customer extends Deletable {
    @Indexed
    private String firstName = "";
    @Indexed
    private String lastName = "";

    @Indexed
    private String email;

    @Indexed(sparse = true)
    private String phoneNumber;
    private String internationalCode;
    private String regionCode;

    private Address address;

    @Indexed
    private String authKey;

    private Long birthday; //was double
    private String favouriteFood;
    private String favouriteDrink;
    private String hatedFood;
    private List<String> allergies = new ArrayList<>(); //these are ids
    private List<String> dietaryRequirements = new ArrayList<>(); //these are ids
    private List<String> foodPreferences = new ArrayList<>(); //these are ids
    private String passwordMash;
    private List<BlackMark> blackMarks = new ArrayList<>();
    private String confirmationCode;
    private CreditCardData ccData;
    private Long legalEmailSent;
    boolean optedIntoMarketing = false;
    boolean registeredViaApp = false;

    private long registered = System.currentTimeMillis();

    public Customer(){}
    public Customer(CustomerCustomerView customerView, Customer original) {
        setId(original.getId());
        if(customerView.getName() != null) {
            this.firstName = customerView.getName().getFirstName();
            this.lastName = customerView.getName().getLastName();
        } else {
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
        }
        this.email = customerView.getEmail() != null ? customerView.getEmail() : original.getEmail();
        this.phoneNumber = original.getPhoneNumber(); //not allowed to change
        this.internationalCode = original.getInternationalCode();
        this.regionCode = original.getRegionCode();
        this.address = customerView.getAddress() != null ? customerView.getAddress() : original.getAddress();
        this.authKey = original.getAuthKey();
        this.authKey = original.getAuthKey();
        if(customerView.getBirthday() != null) {
            this.birthday = customerView.getBirthday() * 1000;
        } else {
            this.birthday = original.getBirthday();
        }
        this.favouriteFood = customerView.getFavouriteFood() != null ? customerView.getFavouriteFood() : original.getFavouriteFood();
        this.favouriteDrink = customerView.getFavouriteDrink() != null ? customerView.getFavouriteDrink() : original.getFavouriteDrink();
        this.hatedFood = customerView.getHatedFood() != null ? customerView.getHatedFood() : original.getHatedFood();
        this.allergies = customerView.getAllergies() != null && customerView.getAllergies().size() != 0 ? customerView.getAllergies() : original.getAllergies();
        this.dietaryRequirements = customerView.getDietaryRequirements() != null && customerView.getDietaryRequirements().size() != 0 ? customerView.getDietaryRequirements() : original.getDietaryRequirements();
        this.foodPreferences = customerView.getFoodPreferences() != null && customerView.getFoodPreferences().size() != 0 ? customerView.getFoodPreferences() : original.getFoodPreferences();
        this.blackMarks = original.getBlackMarks();
        this.confirmationCode = original.getConfirmationCode();
        this.ccData = original.getCcData();
        this.legalEmailSent = original.getLegalEmailSent();
        this.optedIntoMarketing = original.isOptedIntoMarketing();
        this.registeredViaApp = original.isRegisteredViaApp();
        this.registered = original.getRegistered();
    }

    public Customer(SMSRegistrationView smsRegistrationView, String confirmationCode) {
        if(smsRegistrationView.getName() != null) {
            this.firstName = smsRegistrationView.getName().getFirstName();
            this.lastName = smsRegistrationView.getName().getLastName();
        }
        this.email = smsRegistrationView.getEmail();
        this.phoneNumber = smsRegistrationView.getPhoneNumber();
        this.confirmationCode = confirmationCode;
        this.internationalCode = smsRegistrationView.getInternationalCode();
        this.regionCode = smsRegistrationView.getRegionCode();
    }

    public static String determineName(Customer customer) {
        return determineName(customer.getFirstName(), customer.getLastName(), "Anonymous");
    }

    public static String determineName(String firstName, String lastName, String defaultName) {
        String name = "";
        if(StringUtils.isNotBlank(firstName)) {
            name += firstName;
        }
        if(StringUtils.isNotBlank(name)) {
            name += " ";
        }
        if(StringUtils.isNotBlank(lastName)) {
            name += lastName;
        }
        name = name.trim();
        if(StringUtils.isBlank(name)) {
            name = defaultName;
        }
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getBirthday() {
        return birthday;
    }

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
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

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(List<String> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public List<String> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(List<String> foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public String getPasswordMash() {
        return passwordMash;
    }

    public void setPasswordMash(String passwordMash) {
        this.passwordMash = passwordMash;
    }

    public List<BlackMark> getBlackMarks() {
        return blackMarks;
    }

    public void setBlackMarks(List<BlackMark> blackMarks) {
        this.blackMarks = blackMarks;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getInternationalCode() {
        return internationalCode;
    }

    public void setInternationalCode(String internationalCode) {
        this.internationalCode = internationalCode;
    }

    public CreditCardData getCcData() {
        return ccData;
    }

    public void setCcData(CreditCardData ccData) {
        this.ccData = ccData;
    }

    public Long getLegalEmailSent() {
        return legalEmailSent;
    }

    public void setLegalEmailSent(Long legalEmailSent) {
        this.legalEmailSent = legalEmailSent;
    }

    public boolean isOptedIntoMarketing() {
        return optedIntoMarketing;
    }

    public void setOptedIntoMarketing(boolean optedIntoMarketing) {
        this.optedIntoMarketing = optedIntoMarketing;
    }

    public boolean isRegisteredViaApp() {
        return registeredViaApp;
    }

    public void setRegisteredViaApp(boolean registeredViaApp) {
        this.registeredViaApp = registeredViaApp;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
