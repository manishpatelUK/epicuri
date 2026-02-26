package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

@Document(collection = TableNames.MODIFIERS)
public class Modifier extends Deletable {

    private String modifierValue;
    private int price;
    private int priceOverride;
    private String taxTypeId;
    private TaxRate taxRate; //only stored when stored inside an Order
    private String plu;
    private ItemType type = ItemType.OTHER;

    public Modifier(){}
    public Modifier(ModifierView modifierView) {
        modifierValue = modifierView.getModifierValue();
        price = MoneyService.toPenniesRoundNearest(modifierView.getPrice());
        priceOverride = price;
        taxTypeId = modifierView.getTaxTypeId();
        if(modifierView.getPlu() != null && modifierView.getPlu().trim().length() > 0) {
            plu = modifierView.getPlu();
        }
        type = ItemType.valueOf(modifierView.getType());
    }

    public Modifier(Modifier modifier, TaxRate taxRate) {
        setId(modifier.getId());
        setDeleted(modifier.getDeleted());
        this.modifierValue = modifier.getModifierValue();
        this.price = modifier.getPrice();
        this.priceOverride = modifier.getPriceOverride();
        this.taxTypeId = modifier.getTaxTypeId();
        this.taxRate = taxRate;
        this.plu = modifier.getPlu();
        this.type = modifier.getType();
    }

    public String getModifierValue() {
        return modifierValue;
    }

    public void setModifierValue(String modifierValue) {
        this.modifierValue = modifierValue;
    }

    public String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(int priceOverride) {
        this.priceOverride = priceOverride;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public int hashCode(String... exclude) {
        return HashCodeBuilder.reflectionHashCode(this, exclude);
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}
