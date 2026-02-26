package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

/**
 * Created by manish on 30/07/2017.
 */
public class AmountUpdateRequest extends SequentialTableRequest {
    private int newAmount;

    public int getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(int newAmount) {
        this.newAmount = newAmount;
    }
}
