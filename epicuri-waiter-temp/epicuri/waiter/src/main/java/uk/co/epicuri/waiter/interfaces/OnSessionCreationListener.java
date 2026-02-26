package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

/**
 * Created by manish on 28/02/2018.
 */

public interface OnSessionCreationListener {
    void onSessionCreated(EpicuriSessionDetail epicuriSessionDetail);
}
