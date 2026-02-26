package uk.co.epicuri.serverapi.common.pojo.customer;

import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;

import java.util.ArrayList;
import java.util.List;

public class SelfServiceRequest {
    private List<CustomerOrderItemView> orders = new ArrayList<>();
    private CreditCardData ccData;
    private String deliveryLocation;
    private int tipAmount = 0;

    public List<CustomerOrderItemView> getOrders() {
        return orders;
    }

    public void setOrders(List<CustomerOrderItemView> orders) {
        this.orders = orders;
    }

    public CreditCardData getCcData() {
        return ccData;
    }

    public void setCcData(CreditCardData ccData) {
        this.ccData = ccData;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        if(deliveryLocation != null && deliveryLocation.length() > 10) {
            this.deliveryLocation = deliveryLocation.substring(0, 9);
        } else this.deliveryLocation = deliveryLocation;
    }

    public int getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(int tipAmount) {
        this.tipAmount = tipAmount;
    }
}
