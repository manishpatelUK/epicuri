package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.common.pojo.model.session.NumericalAdjustmentType;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostAdjustmentView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("NumericalTypeId")
    private int numericalTypeId;

    @JsonProperty("Value")
    private double value;

    @JsonProperty("TypeId")
    private String typeId;

    private HostAdjustmentTypeView type;

    @JsonProperty("Created")
    private long created;

    @JsonProperty("linkedTo")
    private String linkedTo;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty
    private Map<String,Object> extras = new HashMap<>();

    public HostAdjustmentView(){}
    public HostAdjustmentView(Adjustment adjustment) {
        id = adjustment.getId();
        numericalTypeId = NumericalAdjustmentType.toClientId(adjustment.getNumericalType());
        if(adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
            value = MoneyService.toMoneyRoundNearest(adjustment.getValue());
        } else {
            value = MoneyService.intToPercentageDiscount(adjustment.getValue());
        }
        typeId = adjustment.getAdjustmentType().getId();
        type = new HostAdjustmentTypeView(adjustment.getAdjustmentType());
        created = adjustment.getCreated() / 1000;
        this.linkedTo = adjustment.getLinkedTo();
        if(adjustment.getSpecialAdjustmentData().containsKey(Adjustment.REFERENCE)) {
            this.reference = adjustment.getSpecialAdjustmentData().get(Adjustment.REFERENCE).toString();
        }
        if(adjustment.getSpecialAdjustmentData().containsKey(StripeConstants.PAYMENT_KEY)) {
            extras.put(StripeConstants.PAYMENT_KEY, adjustment.getSpecialAdjustmentData()); //todo this looks a bit wrong - check against waiter app code & change it
        }
        if(adjustment.getSpecialAdjustmentData().containsKey(RestaurantConstants.ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE)) {
            extras.put(RestaurantConstants.ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE, adjustment.getSpecialAdjustmentData().get(RestaurantConstants.ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE));
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumericalTypeId() {
        return numericalTypeId;
    }

    public void setNumericalTypeId(int numericalTypeId) {
        this.numericalTypeId = numericalTypeId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(String linkedTo) {
        this.linkedTo = linkedTo;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public HostAdjustmentTypeView getType() {
        return type;
    }

    public void setType(HostAdjustmentTypeView type) {
        this.type = type;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }
}
