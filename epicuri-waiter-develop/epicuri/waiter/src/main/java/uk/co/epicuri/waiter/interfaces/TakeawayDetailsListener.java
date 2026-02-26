package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public interface TakeawayDetailsListener {
    void editTakeawayDetails(EpicuriSessionDetail session);
    void closeTakeaway();
}
