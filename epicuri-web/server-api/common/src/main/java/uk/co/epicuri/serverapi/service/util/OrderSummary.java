package uk.co.epicuri.serverapi.service.util;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;

import java.util.HashMap;
import java.util.Map;

import static uk.co.epicuri.serverapi.service.util.MapUtil.update;

/**
 * Created by manish
 */
public class OrderSummary {
    private Map<ItemType, Integer> itemTypeVatTotal = new HashMap<>();
    private Map<ItemType, Integer> itemTypeTotal = new HashMap<>();
    private Map<ItemType, Integer> itemTypeCount = new HashMap<>();
    private Map<String, Integer> adjustments = new HashMap<>();
    private Map<String, Integer> menuItemCount = new HashMap<>();
    private Map<String, Integer> menuItemValue = new HashMap<>();
    private Map<String, Integer> vatTypeTotal = new HashMap<>();

    public OrderSummary() {
        for (ItemType type : ItemType.values()) {
            itemTypeVatTotal.put(type, 0);
            itemTypeTotal.put(type, 0);
            itemTypeCount.put(type, 0);
        }
    }

    public Map<ItemType, Integer> getItemTypeVatTotal() {
        return itemTypeVatTotal;
    }

    public int getSumVAT() {
        return itemTypeVatTotal.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int sumTotal() {
        return itemTypeTotal.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<ItemType, Integer> getItemTypeTotal() {
        return itemTypeTotal;
    }

    public Map<String, Integer> getAdjustments() {
        return adjustments;
    }

    public Map<ItemType, Integer> getItemTypeCount() {
        return itemTypeCount;
    }

    public Map<String, Integer> getVatTypeTotal() {
        return vatTypeTotal;
    }

    public void updateItemTypeVAT(ItemType type, int amount) {
        update(itemTypeVatTotal, type, amount);
    }

    public void updateAdjustment(String adjustmentName, int amount) {
        update(adjustments, adjustmentName, amount);
    }

    public void updateItemTypeCount(ItemType type, int amount) {
        update(itemTypeCount, type, amount);
    }

    public void updateItemTypeTotal(ItemType type, int amount) {
        update(itemTypeTotal, type, amount);
    }

    public void updateMenuItemCount(MenuItem item, int amount) {
        update(menuItemCount, item.getId(), amount);
    }

    public void updateMenuItemTotal(MenuItem item, int amount) {
        update(menuItemValue, item.getId(), amount);
    }

    public void updateVatTypeTotal(TaxRate rate, int amount) {
        update(vatTypeTotal, rate == null || StringUtils.isBlank(rate.getName()) ? "Unknown" : rate.getName(), amount);
    }

    public Map<String, Integer> getMenuItemCount() {
        return menuItemCount;
    }

    public Map<String, Integer> getMenuItemValue() {
        return menuItemValue;
    }
}
