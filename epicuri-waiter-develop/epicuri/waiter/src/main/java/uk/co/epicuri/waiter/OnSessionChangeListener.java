package uk.co.epicuri.waiter;

import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

/**
 * Created by pharris on 25/09/14.
 */
public interface OnSessionChangeListener {
	void onSessionChanged(EpicuriSessionDetail session);

	interface SessionContainer {

		void registerSessionListener(OnSessionChangeListener listener);

		void deRegisterSessionListener(OnSessionChangeListener listener);

	}
}