package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;

/**
 * Created by Manish on 16/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerAuthPayload {
    @JsonProperty("Email")
    private String email;

    @JsonProperty("Password")
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
