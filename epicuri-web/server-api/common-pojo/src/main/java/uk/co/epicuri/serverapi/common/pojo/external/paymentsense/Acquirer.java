package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Acquirer {
    private String currency;
    private Map<String,TotalsBreakdown> currentSessionIssuerTotals = new HashMap<>();
    private Map<String,TotalsBreakdown> previousSessionIssuerTotals = new HashMap<>();
    private TotalsBreakdown currentSessionTotals;
    private TotalsBreakdown previousSessionTotals;
    private List<String> currentSessionTransactionNumbers = new ArrayList<>();
    private List<String> previousSessionTransactionNumbers = new ArrayList<>();

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, TotalsBreakdown> getCurrentSessionIssuerTotals() {
        return currentSessionIssuerTotals;
    }

    public void setCurrentSessionIssuerTotals(Map<String, TotalsBreakdown> currentSessionIssuerTotals) {
        this.currentSessionIssuerTotals = currentSessionIssuerTotals;
    }

    public Map<String, TotalsBreakdown> getPreviousSessionIssuerTotals() {
        return previousSessionIssuerTotals;
    }

    public void setPreviousSessionIssuerTotals(Map<String, TotalsBreakdown> previousSessionIssuerTotals) {
        this.previousSessionIssuerTotals = previousSessionIssuerTotals;
    }

    public TotalsBreakdown getCurrentSessionTotals() {
        return currentSessionTotals;
    }

    public void setCurrentSessionTotals(TotalsBreakdown currentSessionTotals) {
        this.currentSessionTotals = currentSessionTotals;
    }

    public TotalsBreakdown getPreviousSessionTotals() {
        return previousSessionTotals;
    }

    public void setPreviousSessionTotals(TotalsBreakdown previousSessionTotals) {
        this.previousSessionTotals = previousSessionTotals;
    }

    public List<String> getCurrentSessionTransactionNumbers() {
        return currentSessionTransactionNumbers;
    }

    public void setCurrentSessionTransactionNumbers(List<String> currentSessionTransactionNumbers) {
        this.currentSessionTransactionNumbers = currentSessionTransactionNumbers;
    }

    public List<String> getPreviousSessionTransactionNumbers() {
        return previousSessionTransactionNumbers;
    }

    public void setPreviousSessionTransactionNumbers(List<String> previousSessionTransactionNumbers) {
        this.previousSessionTransactionNumbers = previousSessionTransactionNumbers;
    }
}
