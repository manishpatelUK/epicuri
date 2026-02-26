package uk.co.epicuri.waiter.model;

import java.io.Serializable;

/**
 * Created by manish on 26/12/2017.
 */

public class Discount implements Serializable {
    private Integer amount;
    private Double percentage;
    private String type;

    public static Discount newDiscountByPercentage(double percentage, String type) {
        Discount discount = new Discount();
        discount.setPercentage(percentage);
        discount.setType(type);
        return discount;
    }

    public static Discount newDiscountByAmount(int amount, String type) {
        Discount discount = new Discount();
        discount.setAmount(amount);
        discount.setType(type);
        return discount;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
