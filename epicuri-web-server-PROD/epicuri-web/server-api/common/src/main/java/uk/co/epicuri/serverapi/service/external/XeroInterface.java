package uk.co.epicuri.serverapi.service.external;

import com.xero.api.*;
import com.xero.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;

import java.io.IOException;
import java.util.List;

public class XeroInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(XeroInterface.class);

    private Config originalConfig = new JsonConfig("xero.json");

    public OAuthRequestToken executeAuthRequest(String restaurantId) throws IOException {
        Config config = new JsonConfig("xero.json");
        config.setAuthCallBackUrl(config.getRedirectUri() + "/" + restaurantId);
        OAuthRequestToken requestToken = new OAuthRequestToken(config);
        requestToken.execute();
        return requestToken;
    }

    public OAuthAuthorizeToken createAuthorizeToken(OAuthRequestToken requestToken, String restaurantId) {
        Config config = new JsonConfig("xero.json");
        config.setAuthCallBackUrl(config.getRedirectUri() + "/" + restaurantId);
        return new OAuthAuthorizeToken(config, requestToken.getTempToken());
    }

    public OAuthAccessToken verify(String verifier, String token, String secret) throws IOException {
        OAuthAccessToken accessToken = new OAuthAccessToken(originalConfig);
        accessToken.build(verifier, token, secret).execute();
        return accessToken;
    }

    public List<Account> getAccounts(String token, String tokenSecret) throws IOException {
        Config config = new JsonConfig("xero.json");
        XeroClient client = new XeroClient(config);
        client.setOAuthToken(token, tokenSecret);
        return client.getAccounts();
    }

    public boolean isTokenStale(String timestamp) {
        return isTokenStale(originalConfig, timestamp);
    }

    public boolean isTokenStale(Config config, String timestamp) {
        OAuthAccessToken accessToken = new OAuthAccessToken(config);
        if(config.getAppType().equals("PARTNER")) {
            return accessToken.isStale(timestamp);
        } else if(config.getAppType().equals("PUBLIC")) {
            LOGGER.warn("Epicuri Xero app is PUBLIC");
            long ts = Long.valueOf(timestamp);
            long diff = (System.currentTimeMillis() / 1000) - ts;
            LOGGER.debug("Xero timeout ts = {}, diff = {}", ts, diff);
            return diff > (60*30);
        }

        return true;
    }

    public boolean refreshToken(KVData kvData) throws IOException {
        OAuthAccessToken accessToken = new OAuthAccessToken(originalConfig);
        accessToken.setToken(kvData.getToken());
        accessToken.setTokenSecret(kvData.getSecret());
        accessToken.build().execute();
        boolean success = accessToken.isSuccess();
        if(success) {
            kvData.setToken(accessToken.getToken());
            kvData.setSecret(accessToken.getTokenSecret());
            kvData.setTokenExpiration(Long.valueOf(accessToken.getTokenTimestamp()));
            kvData.setKey(accessToken.getSessionHandle());
        }

        return success;
    }

    public boolean isPublicApp() {
        return originalConfig.getAppType().equals("PUBLIC");
    }
}
