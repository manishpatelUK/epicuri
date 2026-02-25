package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

public class PaymentSenseRefund {
    private String transactionId;
    private String location;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
