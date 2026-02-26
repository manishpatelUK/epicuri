package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 28/07/2017.
 */
public class PostTableRequest {
    private String tableName;
    private int amount;
    private String currency;
    private List<ReceiptLine> receipt = new ArrayList<>();
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<ReceiptLine> getReceipt() {
        return receipt;
    }

    public void setReceipt(List<ReceiptLine> receipt) {
        this.receipt = receipt;
    }

    public List<String> getWaiterIds() {
        return waiterIds;
    }

    public void setWaiterIds(List<String> waiterIds) {
        this.waiterIds = waiterIds;
    }
}
