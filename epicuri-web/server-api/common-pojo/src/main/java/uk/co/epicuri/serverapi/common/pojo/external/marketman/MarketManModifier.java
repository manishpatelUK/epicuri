package uk.co.epicuri.serverapi.common.pojo.external.marketman;

import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

public class MarketManModifier extends MarketManDish {
    private String dishId;

    public MarketManModifier(){}

    public MarketManModifier(Modifier modifier, double netPrice) {
        setId(modifier.getId());
        setName(modifier.getModifierValue());
        setCategory(modifier.getType().getName());
        setNetPrice(netPrice);
        setGrossPrice(MoneyService.toMoneyRoundNearest(modifier.getPrice()));
        setCode(modifier.getPlu());
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }
}
