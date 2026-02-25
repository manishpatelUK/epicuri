package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Transient;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;

import java.util.HashMap;
import java.util.Map;

public class Adjustment extends IDAble{
    @Transient
    public static final String REFERENCE = "reference";

    private AdjustmentType adjustmentType;

    private NumericalAdjustmentType numericalType;
    private int value; //percentage or pennies
    private long created; //was datetime in cs
    private String staffId;
    private Map<String,Object> specialAdjustmentData = new HashMap<>();
    private String linkedTo;

    private boolean voided;
    private String voidedByStaffId;

    private ItemType applicableToItems = ItemType.ALL;

    public Adjustment(){
    }

    public Adjustment(String parentSessionOrOrderId){
        setId(generateId(parentSessionOrOrderId));
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(AdjustmentType adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public NumericalAdjustmentType getNumericalType() {
        return numericalType;
    }

    public void setNumericalType(NumericalAdjustmentType numericalType) {
        this.numericalType = numericalType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Map<String,Object> getSpecialAdjustmentData() {
        return specialAdjustmentData;
    }

    public void setSpecialAdjustmentData(Map<String,Object> specialAdjustmentData) {
        this.specialAdjustmentData = specialAdjustmentData;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public String getVoidedByStaffId() {
        return voidedByStaffId;
    }

    public void setVoidedByStaffId(String voidedByStaffId) {
        this.voidedByStaffId = voidedByStaffId;
    }

    public String getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(String linkedTo) {
        this.linkedTo = linkedTo;
    }

    public ItemType getApplicableToItems() {
        if(applicableToItems == null) {
            return ItemType.ALL;
        }
        return applicableToItems;
    }

    public void setApplicableToItems(ItemType applicableToItems) {
        this.applicableToItems = applicableToItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Adjustment that = (Adjustment) o;

        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
