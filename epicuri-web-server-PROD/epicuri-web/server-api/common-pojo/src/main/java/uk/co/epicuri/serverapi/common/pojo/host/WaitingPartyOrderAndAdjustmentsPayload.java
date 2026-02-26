package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 28/12/2017.
 */
public class WaitingPartyOrderAndAdjustmentsPayload extends WaitingPartyOrderPayload {
    @JsonProperty("adjustments")
    private List<AdjustmentRequest> adjustments = new ArrayList<>();

    public List<AdjustmentRequest> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<AdjustmentRequest> adjustments) {
        this.adjustments = adjustments;
    }
}
