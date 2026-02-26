package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseRefund;

public class HostRefundAdjustmentView {
    private PaymentSenseRefund paymentSense;

    public PaymentSenseRefund getPaymentSense() {
        return paymentSense;
    }

    public void setPaymentSense(PaymentSenseRefund paymentSense) {
        this.paymentSense = paymentSense;
    }
}
