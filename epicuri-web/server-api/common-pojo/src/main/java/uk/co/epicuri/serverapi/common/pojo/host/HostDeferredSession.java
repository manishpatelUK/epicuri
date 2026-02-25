package uk.co.epicuri.serverapi.common.pojo.host;

import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.session.CalculationKey;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HostDeferredSession {
    private String sessionId;
    private String staffId;
    private boolean paid;
    private long creationTime;
    private HostCustomerView customer;
    private double remainingTotal;

    public HostDeferredSession(){}
    public HostDeferredSession(CustomerInteractionDeferredSession deferredSession, Customer customer, Map<CalculationKey,Number> calculations) {
        this.sessionId = deferredSession.getSessionId();
        this.staffId = deferredSession.getStaffId();
        this.paid = deferredSession.isPaid();
        this.creationTime = deferredSession.getCreationTime();
        this.customer = new HostCustomerView(customer, new HashMap<>());
        this.remainingTotal = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.REMAINING_TOTAL).intValue());
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public HostCustomerView getCustomer() {
        return customer;
    }

    public void setCustomer(HostCustomerView customer) {
        this.customer = customer;
    }

    public double getRemainingTotal() {
        return remainingTotal;
    }

    public void setRemainingTotal(double remainingTotal) {
        this.remainingTotal = remainingTotal;
    }
}
