package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRemovalAdjustmentRequest {
    @JsonProperty("AdjustmentType")
    private String adjustmentTypeId;

    public String getAdjustmentTypeId() {
        return adjustmentTypeId;
    }

    public void setAdjustmentTypeId(String adjustmentTypeId) {
        this.adjustmentTypeId = adjustmentTypeId;
    }
}
