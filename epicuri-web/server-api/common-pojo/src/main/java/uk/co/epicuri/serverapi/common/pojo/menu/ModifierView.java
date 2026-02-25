package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifierView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ModifierValue")
    private String modifierValue;

    @JsonProperty("Price")
    private double price;

    @JsonProperty("ModifierGroupId")
    private String modifierGroupId;

    @JsonProperty("TaxTypeId")
    private String taxTypeId;

    @JsonProperty("plu")
    private String plu;

    @JsonProperty("MenuItemTypeId")
    private int type = -1;

    public ModifierView(){}

    public ModifierView(Modifier modifier, ModifierGroup modifierGroup) {
        this(modifier);
        this.modifierGroupId = modifierGroup.getId();
    }

    public ModifierView(Modifier modifier) {
        this.id = modifier.getId();
        this.modifierValue = modifier.getModifierValue();
        this.price = MoneyService.toMoneyRoundNearest(modifier.getPriceOverride());
        this.taxTypeId = modifier.getTaxTypeId();
        this.plu = modifier.getPlu();
        this.type = modifier.getType().getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModifierValue() {
        return modifierValue;
    }

    public void setModifierValue(String modifierValue) {
        this.modifierValue = modifierValue;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getModifierGroupId() {
        return modifierGroupId;
    }

    public void setModifierGroupId(String modifierGroupId) {
        this.modifierGroupId = modifierGroupId;
    }

    public String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
