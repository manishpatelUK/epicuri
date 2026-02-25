package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SMSRegistrationView {
    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("internationalCode")
    private String internationalCode;

    private String regionCode;

    @JsonProperty("name")
    private Name name;

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

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getInternationalCode() {
        return internationalCode;
    }

    public void setInternationalCode(String internationalCode) {
        this.internationalCode = internationalCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
}
