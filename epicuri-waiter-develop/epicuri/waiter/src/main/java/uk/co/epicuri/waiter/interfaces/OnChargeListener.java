package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriCustomer;

public interface OnChargeListener {
    void charge(String sessionId, EpicuriCustomer customer);
}
