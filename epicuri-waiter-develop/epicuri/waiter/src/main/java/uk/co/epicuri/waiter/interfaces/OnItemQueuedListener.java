package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public interface OnItemQueuedListener {
    void queueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner);
    void unQueueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner);
}