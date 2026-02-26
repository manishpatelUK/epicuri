package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import com.google.common.collect.Lists;

public class ReceiptBuilder {

    private Receipt receipt;

    private ReceiptBuilder() {
        receipt = new Receipt();
    }

    public static ReceiptBuilder newInstance() {
        return new ReceiptBuilder();
    }

    public ReceiptBuilder add(String value, ReceiptLineFormat... format) {
        return add(value, ReceiptLineType.TEXT, format);
    }

    public ReceiptBuilder add(String value, ReceiptLineType lineType, ReceiptLineFormat... format) {
        ReceiptLine line = new ReceiptLine();
        line.setValue(value);
        line.setType(lineType);
        line.setFormat(Lists.newArrayList(format));
        receipt.getNewReceipt().add(line);
        return this;
    }

    public ReceiptBuilder setLastKnownAmount(int lastKnownAmount) {
        receipt.setLastKnownAmount(lastKnownAmount);
        return this;
    }


    public ReceiptBuilder setLastKnownPaidAmount(int lastKnownPaidAmount) {
        receipt.setLastKnownPaidAmount(lastKnownPaidAmount);
        return this;
    }

    public Receipt build() {
        return receipt;
    }

}
