package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 27/07/2017.
 */
public class TransactionsList {
    @JsonProperty("transactions")
    private List<Transaction> transactions = new ArrayList<>();

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
