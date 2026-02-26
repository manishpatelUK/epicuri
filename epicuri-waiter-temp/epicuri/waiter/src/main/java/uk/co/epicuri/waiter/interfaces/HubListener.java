package uk.co.epicuri.waiter.interfaces;

import android.support.v7.view.ActionMode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.ui.HubEventsFragment;

public interface HubListener {
    void acknowledge(EpicuriEvent.Notification eventSelected);

    void highlightTablesForSession(String sessionId);

    void postpone(@Nullable EpicuriEvent.Notification eventSelected);

    void launchSession(@Nullable String sessionId);

    void launchSession(@Nullable String sessionId, boolean addItems);

    void highlightNoTables();

    void restartEventLoader();

    void showReseatUi(@Nullable EpicuriSessionDetail sessionSelected);

    void showNewPartyDialog(String[] tableIds);

    void editReservation(@Nullable String id);

    boolean partyDetails();

    void floorSwitch();

    void refresh();
}
