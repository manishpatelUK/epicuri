package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 30/07/2017.
 */
public class TableResponse {
    private String tableName;
    private int amount;
    private int amountCashback;
    private int amountGratuity;
    private int amountPaid;
    private List<Payment> payments = new ArrayList<>();
    private List<ReceiptLine> receipt = new ArrayList<>();
    private boolean locked;
    private String lockedBy;
    private List<String> waiterIds = new ArrayList<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmountCashback() {
        return amountCashback;
    }

    public void setAmountCashback(int amountCashback) {
        this.amountCashback = amountCashback;
    }

    public int getAmountGratuity() {
        return amountGratuity;
    }

    public void setAmountGratuity(int amountGratuity) {
        this.amountGratuity = amountGratuity;
    }

    public int getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<ReceiptLine> getReceipt() {
        return receipt;
    }

    public void setReceipt(List<ReceiptLine> receipt) {
        this.receipt = receipt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public List<String> getWaiterIds() {
        return waiterIds;
    }

    public void setWaiterIds(List<String> waiterIds) {
        this.waiterIds = waiterIds;
    }
}
