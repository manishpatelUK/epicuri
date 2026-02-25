package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.customer.Name;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

public class HostCustomerViewBasic {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Name")
    private Name name;

    private String email;

    public HostCustomerViewBasic(){}
    public HostCustomerViewBasic(Customer customer) {
        this.id = customer.getId();
        if(StringUtils.isNotBlank(customer.getPhoneNumber())) {
            this.phoneNumber = formatPhoneNumber(customer.getInternationalCode(), customer.getPhoneNumber());
        } else {
            this.phoneNumber = "";
        }

        this.name = new Name();
        this.name.setFirstName((StringUtils.isBlank(customer.getFirstName()) ? "" : customer.getFirstName()).trim());
        this.name.setLastName((StringUtils.isBlank(customer.getLastName()) ? "" : customer.getLastName()).trim());
        this.email = customer.getEmail();
    }

    public static String formatPhoneNumber(String internationalCode, String number) {
        return internationalCode != null ? "(" + internationalCode + ")" + "0" + number : "0" + number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
