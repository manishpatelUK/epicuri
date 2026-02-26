package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public interface OnSessionChangeListener {
	void onSessionChanged(EpicuriSessionDetail session);
}