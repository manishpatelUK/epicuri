package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifierValueView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ModifierValue")
    private String name;

    @JsonProperty("TaxTypeId")
    private String taxTypeId;

    @JsonProperty("Price")
    private double price;

    @JsonProperty("priceInt")
    private int priceInt;

    public ModifierValueView() {}

    public ModifierValueView(Modifier modifier) {
        this.id = modifier.getId();
        this.name = modifier.getModifierValue();
        this.taxTypeId = modifier.getTaxTypeId();
        this.price = MoneyService.toMoneyRoundNearest(modifier.getPriceOverride());
        this.priceInt = modifier.getPriceOverride();
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

    public String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPriceInt() {
        return priceInt;
    }

    public void setPriceInt(int priceInt) {
        this.priceInt = priceInt;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
