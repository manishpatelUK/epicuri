package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

/**
 * Created by manish on 29/07/2017.
 */
public class SequentialTableRequest {
    private int lastKnownAmount;
    private int lastKnownPaidAmount;

    public int getLastKnownAmount() {
        return lastKnownAmount;
    }

    public void setLastKnownAmount(int lastKnownAmount) {
        this.lastKnownAmount = lastKnownAmount;
    }

    public int getLastKnownPaidAmount() {
        return lastKnownPaidAmount;
    }

    public void setLastKnownPaidAmount(int lastKnownPaidAmount) {
        this.lastKnownPaidAmount = lastKnownPaidAmount;
    }
}
