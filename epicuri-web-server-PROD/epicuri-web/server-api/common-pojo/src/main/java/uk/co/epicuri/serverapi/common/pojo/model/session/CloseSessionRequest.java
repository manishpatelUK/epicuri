package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Manish on 18/07/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseSessionRequest {
    @JsonProperty("GiveBlackMark")
    private boolean giveBlackMark;

    public boolean isGiveBlackMark() {
        return giveBlackMark;
    }

    public void setGiveBlackMark(boolean giveBlackMark) {
        this.giveBlackMark = giveBlackMark;
    }
}
