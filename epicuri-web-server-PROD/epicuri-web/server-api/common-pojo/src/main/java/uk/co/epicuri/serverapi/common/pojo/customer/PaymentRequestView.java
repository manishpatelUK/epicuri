package uk.co.epicuri.serverapi.common.pojo.customer;

import java.util.ArrayList;
import java.util.List;

public class PaymentRequestView {
    private String ccToken;
    private int amount;
    private int tipAmount;
    private List<String> orderIds = new ArrayList<>();

    public String getCcToken() {
        return ccToken;
    }

    public void setCcToken(String ccToken) {
        this.ccToken = ccToken;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(int tipAmount) {
        this.tipAmount = tipAmount;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }
}
