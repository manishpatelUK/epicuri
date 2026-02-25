package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Manish on 17/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeItemCategory {
    @JsonProperty("Code")
    private String code;

    @JsonProperty("Name")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
