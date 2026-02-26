package uk.co.epicuri.waiter.interfaces;

import java.util.List;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public interface OnSessionChangedListener {
    void addSessions(List<EpicuriSessionDetail> sessionsWithoutTabs);

    void setItemChecked(int currentItem, boolean b);

    int getCheckedItemPosition();

    void showSession(EpicuriSessionDetail session);

    void finishActionMode();
}
