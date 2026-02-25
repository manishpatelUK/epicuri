package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationRequest {
    @JsonProperty("APIKey")
    private String apiKey;

    @JsonProperty("APIPassword")
    private String apiPassword;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }
}
