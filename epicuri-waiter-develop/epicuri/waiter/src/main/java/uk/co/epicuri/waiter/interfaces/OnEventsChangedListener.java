package uk.co.epicuri.waiter.interfaces;

import java.util.List;

import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public interface OnEventsChangedListener {
    void onEventsChangedListener(List<EpicuriSessionDetail> sessions);
    void onEventsAddedListener(List<? extends EpicuriEvent.Notification> notifications);

    void finishActionMode();
}
