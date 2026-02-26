package uk.co.epicuri.waiter.model;

import java.io.Serializable;

/**
 * Created by manish on 28/12/2017.
 */

public class NewAdjustmentRequest implements Serializable {
    private String sessionId;
    private EpicuriAdjustmentType adjustmentType;
    private NumericalAdjustmentType type;
    private double value;
    private String reference;
    private String itemType;

    public NewAdjustmentRequest(String sessionId, EpicuriAdjustmentType adjustmentType, NumericalAdjustmentType type, double value, String reference, String itemType) {
        this.sessionId = sessionId;
        this.adjustmentType = adjustmentType;
        this.type = type;
        this.value = value;
        this.reference = reference;
        this.itemType = itemType;
    }

    public EpicuriAdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(EpicuriAdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public NumericalAdjustmentType getType() {
        return type;
    }

    public void setType(NumericalAdjustmentType type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
