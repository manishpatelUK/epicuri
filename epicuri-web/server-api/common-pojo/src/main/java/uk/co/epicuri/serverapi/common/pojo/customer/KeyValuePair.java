package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for preferences
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValuePair {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("Value")
    private String value;

    public KeyValuePair(){}

    public KeyValuePair(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
