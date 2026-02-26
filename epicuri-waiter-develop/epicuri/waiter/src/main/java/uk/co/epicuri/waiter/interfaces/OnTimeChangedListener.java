package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.ui.TenMinuteTimePicker;

public interface OnTimeChangedListener {
    void onTimeChanged(TenMinuteTimePicker view, int hourOfDay, int minute);
}

