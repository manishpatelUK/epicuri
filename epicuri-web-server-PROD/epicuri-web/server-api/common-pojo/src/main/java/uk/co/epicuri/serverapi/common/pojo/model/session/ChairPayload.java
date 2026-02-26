package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChairPayload {
    @JsonProperty("ChairData")
    private String chairData;

    public String getChairData() {
        return chairData;
    }

    public void setChairData(String chairData) {
        this.chairData = chairData;
    }
}
