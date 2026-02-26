package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.HashMap;
import java.util.Map;

public class ReportBalance {
    private String currency;
    private Map<String,TotalsBreakdown> issuerTotals = new HashMap<>();
    private int totalAmount;
    private int totalCashbackAmount;
    private int totalCashbackCount;
    private int totalGratuityAmount;
    private int totalGratuityCount;
    private int totalRefundsAmount;
    private int totalRefundsCount;
    private int totalSalesAmount;
    private int totalSalesCount;
    private String totalsSince;
    private Map<String,TotalsBreakdown> waiterTotals = new HashMap<>();

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, TotalsBreakdown> getIssuerTotals() {
        return issuerTotals;
    }

    public void setIssuerTotals(Map<String, TotalsBreakdown> issuerTotals) {
        this.issuerTotals = issuerTotals;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalCashbackAmount() {
        return totalCashbackAmount;
    }

    public void setTotalCashbackAmount(int totalCashbackAmount) {
        this.totalCashbackAmount = totalCashbackAmount;
    }

    public int getTotalCashbackCount() {
        return totalCashbackCount;
    }

    public void setTotalCashbackCount(int totalCashbackCount) {
        this.totalCashbackCount = totalCashbackCount;
    }

    public int getTotalGratuityAmount() {
        return totalGratuityAmount;
    }

    public void setTotalGratuityAmount(int totalGratuityAmount) {
        this.totalGratuityAmount = totalGratuityAmount;
    }

    public int getTotalGratuityCount() {
        return totalGratuityCount;
    }

    public void setTotalGratuityCount(int totalGratuityCount) {
        this.totalGratuityCount = totalGratuityCount;
    }

    public int getTotalRefundsAmount() {
        return totalRefundsAmount;
    }

    public void setTotalRefundsAmount(int totalRefundsAmount) {
        this.totalRefundsAmount = totalRefundsAmount;
    }

    public int getTotalRefundsCount() {
        return totalRefundsCount;
    }

    public void setTotalRefundsCount(int totalRefundsCount) {
        this.totalRefundsCount = totalRefundsCount;
    }

    public int getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(int totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }

    public int getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(int totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public String getTotalsSince() {
        return totalsSince;
    }

    public void setTotalsSince(String totalsSince) {
        this.totalsSince = totalsSince;
    }

    public Map<String, TotalsBreakdown> getWaiterTotals() {
        return waiterTotals;
    }

    public void setWaiterTotals(Map<String, TotalsBreakdown> waiterTotals) {
        this.waiterTotals = waiterTotals;
    }
}
