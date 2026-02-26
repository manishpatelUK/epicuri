package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriCustomer;

public interface OnDeferCompleteListener {
    void deferComplete(EpicuriCustomer customer);
}
