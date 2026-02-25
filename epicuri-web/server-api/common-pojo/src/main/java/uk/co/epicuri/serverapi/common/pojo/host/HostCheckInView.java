package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

import java.util.Map;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostCheckInView {

    @JsonProperty("Customer")
    private HostCustomerView customer;

    @JsonProperty("Time")
    private long time; //in seconds

    public HostCheckInView(){}
    public HostCheckInView(CheckIn checkIn, Customer customer, Map<String,Preference> preferences) {
        this.customer = new HostCustomerView(customer, preferences);
        this.time = checkIn.getTime() / 1000;
    }

    public HostCustomerView getCustomer() {
        return customer;
    }

    public void setCustomer(HostCustomerView customer) {
        this.customer = customer;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
