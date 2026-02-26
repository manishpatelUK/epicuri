package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorizationResponse extends MarketManResponse {

    @JsonProperty("Token")
    String token;

    @JsonProperty("ExpireDate") //todo yyyy/mm/dd hh:mm:ss -- what timezone??
    String expireDate;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }
}
