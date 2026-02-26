package uk.co.epicuri.waiter.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.model.Discount;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.ui.SessionActivity;

/**
 * Created by manish on 19/12/2017.
 */

public class CalculationService implements Serializable {
    private Map<String,List<Integer>> items = new HashMap<>();
    private List<Integer> payments = new ArrayList<>();
    private List<Integer> finalDiscounts = new ArrayList<>();
    private List<Discount> discounts = new ArrayList<>();

    private SessionCalculator calculator = new SessionCalculator(items, payments, finalDiscounts, discounts);

    public void resetItems() {
        items.clear();
    }

    public void resetAdjustments() {
        payments.clear();
        finalDiscounts.clear();
        discounts.clear();
    }

    public void addItem(int price, EpicuriMenu.Item.ItemType itemType) {
        String key = itemType == null ? "ALL" : itemType.toString();
        if(!items.containsKey(key)) {
            items.put(key, new ArrayList<Integer>());
        }
        items.get(key).add(price);
    }

    public void addPayment(int payment) {
        payments.add(payment);
    }

    public void addDiscount(double discount, boolean isPercentage, EpicuriMenu.Item.ItemType itemType) {
        String value = itemType == null ? "ALL" : itemType.toString();
        if(isPercentage) {
            discounts.add(Discount.newDiscountByPercentage(discount, value));
        } else {
            discounts.add(Discount.newDiscountByAmount(MoneyService.toPenniesRoundNearest(discount), value));
        }
    }

    public void setTipPercentage(double percentage) { //e.g. 0.1 for 10%, 0.125 for 12.5%
        calculator.setTipPercentage(percentage);
    }

    public void recalculate() {
        calculator.recalculate();
    }

    public SessionCalculator getCalculator() {
        return calculator;
    }
}
