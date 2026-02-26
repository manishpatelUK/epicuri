package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.ui.TenMinuteTimePicker;

public interface OnTimeSetListener {
    void onTimeSet(TenMinuteTimePicker view, int hourOfDay, int minute);
}
