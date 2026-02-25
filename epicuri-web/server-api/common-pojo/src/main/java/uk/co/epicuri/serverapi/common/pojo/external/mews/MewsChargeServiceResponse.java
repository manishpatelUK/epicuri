package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeServiceResponse {
    @JsonProperty("Services")
    private List<MewsChargeService> services = new ArrayList<>();

    public List<MewsChargeService> getServices() {
        return services;
    }

    public void setServices(List<MewsChargeService> services) {
        this.services = services;
    }
}
