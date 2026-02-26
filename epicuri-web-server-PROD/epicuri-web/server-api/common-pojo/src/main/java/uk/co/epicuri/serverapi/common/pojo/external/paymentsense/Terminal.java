package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by manish.
 */
public class Terminal {
    @JsonProperty("tpi")
    private String id;

    @JsonProperty("status")
    private TerminalStatus status;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("capabilities")
    private List<String> capabilities;

    @JsonProperty("location")
    private String location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TerminalStatus getStatus() {
        return status;
    }

    public void setStatus(TerminalStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
