package uk.co.epicuri.waiter.interfaces;

import java.util.Calendar;

import uk.co.epicuri.waiter.model.EpicuriReservation;


public interface AddReservationListener {
    void addReservation(Calendar day);
    void editReservation(EpicuriReservation reservation);
}
