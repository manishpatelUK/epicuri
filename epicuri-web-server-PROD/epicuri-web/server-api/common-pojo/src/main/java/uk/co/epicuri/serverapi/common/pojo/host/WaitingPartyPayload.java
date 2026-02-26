package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaitingPartyPayload {
    @JsonProperty("Name")
    private String name = ""; //null causing problems on waiter app

    @JsonProperty("NumberOfPeople")
    private int numberOfPeople;

    @JsonProperty("LeadCustomer")
    private HostCustomerView customer;

    @JsonProperty("Tables")
    private List<String> tables = new ArrayList<>();

    @JsonProperty("CreateSession")
    private Boolean createSession;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("IsAdHoc")
    private Boolean isAdHoc;

    @JsonProperty("refund")
    private boolean refund = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public HostCustomerView getCustomer() {
        return customer;
    }

    public void setCustomer(HostCustomerView customer) {
        this.customer = customer;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public Boolean getCreateSession() {
        return createSession;
    }

    public void setCreateSession(Boolean createSession) {
        this.createSession = createSession;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Boolean getAdHoc() {
        return isAdHoc;
    }

    public void setAdHoc(Boolean adHoc) {
        isAdHoc = adHoc;
    }

    public boolean isRefund() {
        return refund;
    }

    public void setRefund(boolean refund) {
        this.refund = refund;
    }
}
