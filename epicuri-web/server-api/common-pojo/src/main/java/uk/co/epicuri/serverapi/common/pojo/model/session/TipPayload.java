package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Manish on 18/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipPayload {
    @JsonProperty("Tip")
    private double tip;

    public double getTip() {
        return tip;
    }

    public void setTip(double tip) {
        this.tip = tip;
    }
}
