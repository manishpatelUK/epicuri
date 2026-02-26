package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriReservation;

/**
 * Created by Home on 7/19/16.
 */
public interface OnReservationSelectedListener {
    void onReservationSelected(EpicuriReservation reservation);
    void onNothingSelected();
}
