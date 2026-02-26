package uk.co.epicuri.serverapi.common.pojo.external.mews;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeItem {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("UnitCount")
    private int unitCount;

    @JsonProperty("UnitCost")
    private MewsChargeItemUnitCost unitCost;

    @JsonProperty("Category")
    private MewsChargeItemCategory category;

    public MewsChargeItem(){}
    public MewsChargeItem(Order order, int orderItemPlusModsCost, Collection<TaxRate> allRates, String currency){
        name = order.getMenuItem().getName();
        unitCount = order.getQuantity();
        unitCost = new MewsChargeItemUnitCost(
                MoneyService.toMoneyRoundNearest(orderItemPlusModsCost),
                currency,
                allRates.stream().filter(t -> order.getMenuItem().getTaxTypeId().equals(t.getId())).findFirst().orElse(TaxRate.ZERO).getRateAsDouble());
        MewsChargeItemCategory category = new MewsChargeItemCategory();
        if(order.getMenuItem().getType() == ItemType.FOOD) {
            category.setCode("FOOD");
        } else if(order.getMenuItem().getType() == ItemType.DRINK) {
            category.setCode("DRINK");
        } else {
            category.setCode("OTHER");
        }
    }

    public MewsChargeItem(Adjustment adjustment, String currency) { //just for tips for now
        name = "TIP";
        unitCount = 1;
        unitCost = new MewsChargeItemUnitCost();
        unitCost.setAmount(MoneyService.toMoneyRoundNearest(adjustment.getValue()));
        unitCost.setCurrency(currency);
        unitCost.setTax(0);
        category = new MewsChargeItemCategory();
        category.setCode("TIP");
        category.setName("TIP");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
    }

    public MewsChargeItemUnitCost getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(MewsChargeItemUnitCost unitCost) {
        this.unitCost = unitCost;
    }

    public MewsChargeItemCategory getCategory() {
        return category;
    }

    public void setCategory(MewsChargeItemCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "MewsChargeItem{" +
                "name='" + name + '\'' +
                ", unitCount=" + unitCount +
                ", unitCost=" + unitCost +
                ", category=" + category +
                '}';
    }
}
