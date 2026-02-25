package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Receipt extends SequentialTableRequest {
    private List<ReceiptLine> newReceipt = new ArrayList<>();

    public List<ReceiptLine> getNewReceipt() {
        return newReceipt;
    }

    public void setNewReceipt(List<ReceiptLine> newReceipt) {
        this.newReceipt = newReceipt;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
