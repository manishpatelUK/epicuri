package uk.co.epicuri.waiter.model;

import java.util.ArrayList;
import java.util.List;

public class PaymentSenseResponse {
    private String notification = "";
    private List<String> notifications = new ArrayList<>();
    private boolean error;

    private String transactionResult;
    private String transactionId = "";
    private Integer gratuity;
    private Integer amountBase;

    public PaymentSenseResponse(){}

    public PaymentSenseResponse(String notification, boolean error) {
        this.notification = notification;
        this.error = error;
    }

    public PaymentSenseResponse(String transactionResult) {
        this.transactionResult = transactionResult;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(String transactionResult) {
        this.transactionResult = transactionResult;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getGratuity() {
        return gratuity;
    }

    public void setGratuity(Integer gratuity) {
        this.gratuity = gratuity;
    }

    public Integer getAmountBase() {
        return amountBase;
    }

    public void setAmountBase(Integer amountBase) {
        this.amountBase = amountBase;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String toString() {
        return "PaymentSenseResponse{" +
                "notification='" + notification + '\'' +
                ", error=" + error +
                ", transactionResult='" + transactionResult + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", gratuity=" + gratuity +
                ", amountBase=" + amountBase +
                '}';
    }
}
