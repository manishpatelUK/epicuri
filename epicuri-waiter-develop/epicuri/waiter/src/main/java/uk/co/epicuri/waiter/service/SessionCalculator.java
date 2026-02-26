package uk.co.epicuri.waiter.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.model.Discount;
import uk.co.epicuri.waiter.ui.SessionActivity;

/**
 * Created by manish on 19/12/2017.
 */

public class SessionCalculator implements Serializable {
    private final Map<String,List<Integer>> items;
    private final List<Integer> payments;
    private final List<Integer> finalDiscounts;
    private final List<Discount> discounts;

    private int total;
    private int subTotal;
    private Map<String,Integer> subTotalByType = new HashMap<>();
    private int tipTotal;
    private int discountTotal;
    private int paymentsTotal;
    private double tipPercentage;


    public SessionCalculator(Map<String,List<Integer>> items, List<Integer> payments, List<Integer> finalDiscounts, List<Discount> discounts) {
        this.items = items;
        this.payments = payments;
        this.finalDiscounts = finalDiscounts;
        this.discounts = discounts;

        clearValues();
    }

    public void setTipPercentage(double tipPercentage) {
        this.tipPercentage = tipPercentage;
    }

    private void clearValues() {
        total = 0;
        subTotal = 0;
        tipTotal = 0;
        discountTotal = 0;
        paymentsTotal = 0;
        subTotalByType.clear();
    }

    public void recalculate() {
        clearValues();
        calculateSubTotal();
        calculateTip();
        calculateDiscounts();
        calculatePayments();

        total = (subTotal + tipTotal) - discountTotal;
        if(total < 0) {
            total = 0;
        }
    }

    private void calculateSubTotal() {
        for(Map.Entry<String,List<Integer>> entry : items.entrySet()) {
            int itemTypeTotal = calculateSubTotal(entry.getValue());
            subTotalByType.put(entry.getKey(), itemTypeTotal);
            subTotal += itemTypeTotal;
        }
    }

    private int calculateSubTotal(List<Integer> items) {
        int tot = 0;
        for(int value : items) {
            tot += value;
        }
        return tot;
    }

    private void calculateDiscounts() {
        finalDiscounts.clear();
        calculateFinalDiscounts();
        for(int value : finalDiscounts) {
            discountTotal += value;
        }
    }

    private void calculateFinalDiscounts() {
        Map<String,Integer> subTotalByTypeCopy = new HashMap<>(subTotalByType);

        for(Discount discount : discounts) {
            if(discount.getAmount() != null) {
                finalDiscounts.add(discount.getAmount());
            } else if(discount.getPercentage() != null && ((discount.getType().equals(SessionActivity.TYPE_ALL)) || subTotalByTypeCopy.containsKey(discount.getType()))) {
                int operatingValue = 0;
                if(discount.getType().equals(SessionActivity.TYPE_ALL)) {
                    operatingValue = getSum(subTotalByTypeCopy);
                } else {
                    operatingValue = subTotalByTypeCopy.get(discount.getType());
                }
                int value = MoneyService.discountValue(discount.getPercentage(), operatingValue);
                finalDiscounts.add(value);
            }
        }
    }

    private int getSum(Map<String, Integer> subTotalByTypeCopy) {
        int total = 0;
        for(int value : subTotalByTypeCopy.values()) {
            total += value;
        }
        return total;
    }

    private void calculateTip() {
        BigDecimal calculatedTipValue = BigDecimal.valueOf(tipPercentage).scaleByPowerOfTen(-2).multiply(BigDecimal.valueOf(subTotal - discountTotal));
        tipTotal = MoneyService.toPenniesRoundNearest(calculatedTipValue);
    }

    private void calculatePayments() {
        for(int value : payments) {
            paymentsTotal += value;
        }
    }

    public int getTotal() {
        return total;
    }

    public int getTipTotal() {
        return tipTotal;
    }

    public int getDiscountTotal() {
        return discountTotal;
    }

    public int getPaymentsTotal() {
        return paymentsTotal;
    }

    public int getSubTotal() {
        return subTotal;
    }
}
