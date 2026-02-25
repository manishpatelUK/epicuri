package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private int amountBase;
    private int amountCashback;
    private int amountGratuity;
    private int amountTotal;
    private String applicationId;
    private String applicationLabel;
    private String authCode;
    private String cardSchemeName;
    private CardHolderVerificationMethod cardHolderVerificationMethod;
    private String currency;
    private String dateOfExpiry;
    private String dateOfStart;
    private String location;
    private List<PSNotification> notifications = new ArrayList<>();
    private PaymentMethod paymentMethod;
    private String primaryAccountNumber;
    private String primaryAccountNumberSequence;
    private List<ReceiptLine> receiptLines = new ArrayList<>();
    private String requestId;
    private String transactionId;
    private String transactionNumber;
    private String transactionResult;
    private String transactionTime;
    private String transactionType;

    public int getAmountBase() {
        return amountBase;
    }

    public void setAmountBase(int amountBase) {
        this.amountBase = amountBase;
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

    public int getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(int amountTotal) {
        this.amountTotal = amountTotal;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getCardSchemeName() {
        return cardSchemeName;
    }

    public void setCardSchemeName(String cardSchemeName) {
        this.cardSchemeName = cardSchemeName;
    }

    public CardHolderVerificationMethod getCardHolderVerificationMethod() {
        return cardHolderVerificationMethod;
    }

    public void setCardHolderVerificationMethod(CardHolderVerificationMethod cardHolderVerificationMethod) {
        this.cardHolderVerificationMethod = cardHolderVerificationMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    public String getDateOfStart() {
        return dateOfStart;
    }

    public void setDateOfStart(String dateOfStart) {
        this.dateOfStart = dateOfStart;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<PSNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<PSNotification> notifications) {
        this.notifications = notifications;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPrimaryAccountNumber() {
        return primaryAccountNumber;
    }

    public void setPrimaryAccountNumber(String primaryAccountNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
    }

    public String getPrimaryAccountNumberSequence() {
        return primaryAccountNumberSequence;
    }

    public void setPrimaryAccountNumberSequence(String primaryAccountNumberSequence) {
        this.primaryAccountNumberSequence = primaryAccountNumberSequence;
    }

    public List<ReceiptLine> getReceiptLines() {
        return receiptLines;
    }

    public void setReceiptLines(List<ReceiptLine> receiptLines) {
        this.receiptLines = receiptLines;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(String transactionResult) {
        this.transactionResult = transactionResult;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
