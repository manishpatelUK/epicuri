package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

public class TotalsBreakdown {
    private String currency;
    private int totalAmount;
    private int totalRefundAmount;
    private int totalRefundsCount;
    private int totalSalesAmount;
    private int totalSalesCount;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(int totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
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
}
