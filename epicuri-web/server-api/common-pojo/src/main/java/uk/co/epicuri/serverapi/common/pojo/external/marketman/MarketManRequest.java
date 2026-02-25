package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketManRequest {
    @JsonProperty("Token")
    private String token;

    public MarketManRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
