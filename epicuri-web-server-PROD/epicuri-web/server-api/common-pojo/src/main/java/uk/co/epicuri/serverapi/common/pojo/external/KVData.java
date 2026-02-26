package uk.co.epicuri.serverapi.common.pojo.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by manish
 */
@MgmtPojoModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class KVData implements Serializable{
    private String password;
    private String key;
    private String token;
    private String secret;
    private String host;

    @MgmtIgnoreField
    private long tokenExpiration = -1;

    @MgmtIgnoreField
    private Map<String,String> data = new TreeMap<>();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(long tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KVData data1 = (KVData) o;

        if (tokenExpiration != data1.tokenExpiration) return false;
        if (password != null ? !password.equals(data1.password) : data1.password != null) return false;
        if (key != null ? !key.equals(data1.key) : data1.key != null) return false;
        if (token != null ? !token.equals(data1.token) : data1.token != null) return false;
        if (secret != null ? !secret.equals(data1.secret) : data1.secret != null) return false;
        if (host != null ? !host.equals(data1.host) : data1.host != null) return false;
        return data != null ? data.equals(data1.data) : data1.data == null;
    }

    @Override
    public int hashCode() {
        int result = password != null ? password.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (secret != null ? secret.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (int) (tokenExpiration ^ (tokenExpiration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
