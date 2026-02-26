package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.ui.TakeawayActivity;

public interface SetPandingTakeawayListener {
    void editItems();
    void setPendingTakeaway(TakeawayActivity.PendingTakeaway pendingTakeaway);
    TakeawayActivity.PendingTakeaway getPendingTakeaway();
    void closeTakeawayEdit();
}
