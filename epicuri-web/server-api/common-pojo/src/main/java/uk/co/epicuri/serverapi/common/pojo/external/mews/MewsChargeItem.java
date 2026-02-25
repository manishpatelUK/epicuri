package uk.co.epicuri.serverapi.common.pojo.external.mews;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.Collection;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsChargeItem {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("UnitCount")
    private int unitCount;

    @JsonProperty("UnitAmount")
    private MewsChargeItemUnitCost unitAmount;

    @JsonProperty("AccountingCategoryId")
    private String category;

    public MewsChargeItem(){}
    public MewsChargeItem(Order order, int grossValueIncMods, int netValueIncMods, String taxCode, String currency, String accountingCategory){
        name = order.getMenuItem().getName();
        unitCount = order.getQuantity();
        unitAmount = new MewsChargeItemUnitCost(
                MoneyService.toMoneyRoundNearest(grossValueIncMods),
                MoneyService.toMoneyRoundNearest(netValueIncMods),
                currency,
                taxCode
                );
        this.category = accountingCategory;
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

    public MewsChargeItemUnitCost getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(MewsChargeItemUnitCost unitAmount) {
        this.unitAmount = unitAmount;
    }

    /*public MewsChargeItemCategory getCategory() {
        return category;
    }

    public void setCategory(MewsChargeItemCategory category) {
        this.category = category;
    }*/

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "MewsChargeItem{" +
                "name='" + name + '\'' +
                ", unitCount=" + unitCount +
                ", unitCost=" + unitAmount +
                ", category=" + category +
                '}';
    }
}
