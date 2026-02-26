package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by manish on 30/07/2017.
 */
public class Payment {
    private int amountCashback;
    private int amountGratuity;
    private int amountPaid;
    private String authCode;
    private String currency;
    private String cardSchemeName;
    private PaymentMethod paymentMethod;
    private String time;
    private String paymentId;

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

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCardSchemeName() {
        return cardSchemeName;
    }

    public void setCardSchemeName(String cardSchemeName) {
        this.cardSchemeName = cardSchemeName;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Payment payment = (Payment) o;

        return new EqualsBuilder()
                .append(amountCashback, payment.amountCashback)
                .append(amountGratuity, payment.amountGratuity)
                .append(amountPaid, payment.amountPaid)
                .append(authCode, payment.authCode)
                .append(currency, payment.currency)
                .append(cardSchemeName, payment.cardSchemeName)
                .append(paymentMethod, payment.paymentMethod)
                .append(time, payment.time)
                .append(paymentId, payment.paymentId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(amountCashback)
                .append(amountGratuity)
                .append(amountPaid)
                .append(authCode)
                .append(currency)
                .append(cardSchemeName)
                .append(paymentMethod)
                .append(time)
                .append(paymentId)
                .toHashCode();
    }
}
