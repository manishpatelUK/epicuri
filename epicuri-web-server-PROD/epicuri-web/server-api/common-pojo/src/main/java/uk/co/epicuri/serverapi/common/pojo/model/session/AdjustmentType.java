package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

@Document(collection = TableNames.ADJUSTMENT_TYPES)
public class AdjustmentType extends Deletable {
    @MgmtDisplayField
    @Indexed
    private String name;

    private String shortCode;

    private AdjustmentTypeType type;
    private boolean supportsChange;
    private boolean visible = true;
    private boolean showOnReceipt = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AdjustmentTypeType getType() {
        return type;
    }

    public void setType(AdjustmentTypeType type) {
        this.type = type;
    }

    public boolean isSupportsChange() {
        return supportsChange;
    }

    public void setSupportsChange(boolean supportsChange) {
        this.supportsChange = supportsChange;
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
