package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostAdjustmentTypeView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    private String shortCode;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("ChangeGiven")
    private boolean changeGiven;

    @JsonProperty("visible")
    private boolean visible;

    private boolean showOnReceipt;

    public HostAdjustmentTypeView(){}

    public HostAdjustmentTypeView(AdjustmentType adjustmentType) {
        this.id = adjustmentType.getId();
        this.name = adjustmentType.getName();
        if(adjustmentType.getType() == AdjustmentTypeType.PAYMENT && !adjustmentType.getName().equals(MewsConstants.MEWS_ADJUSTMENT_TYPE)) {
            this.type = 0;
        } else if(adjustmentType.getType() == AdjustmentTypeType.PAYMENT && adjustmentType.getName().equals(MewsConstants.MEWS_ADJUSTMENT_TYPE)) {
            this.type = 0;
        } else if(adjustmentType.getType() == AdjustmentTypeType.DISCOUNT) {
            this.type = 1;
        } else if(adjustmentType.getType() == AdjustmentTypeType.GRATUITY) {
            this.type = 0;
        }

        changeGiven = adjustmentType.isSupportsChange();
        shortCode = adjustmentType.getShortCode();
        visible = adjustmentType.isVisible();
        showOnReceipt = adjustmentType.isShowOnReceipt();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isChangeGiven() {
        return changeGiven;
    }

    public void setChangeGiven(boolean changeGiven) {
        this.changeGiven = changeGiven;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isShowOnReceipt() {
        return showOnReceipt;
    }

    public void setShowOnReceipt(boolean showOnReceipt) {
        this.showOnReceipt = showOnReceipt;
    }
}
