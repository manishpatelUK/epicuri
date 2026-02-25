package uk.co.epicuri.serverapi.common.pojo.external.stripe;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CredentialsGrant {
    @JsonProperty("client_secret")
    private String secret;

    @JsonProperty("code")
    private String code;

    @JsonProperty("grant_type")
    private String grantType = "authorization_code";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
}
