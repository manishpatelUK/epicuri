package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriPrintBatch;

/** bound clients register this to receive updates */
public interface PrintQueueListener {
    void statusChanged();
    void itemPrinted(EpicuriPrintBatch batch);
    void itemFailed();
    void itemCancelOrRequeue();
}
