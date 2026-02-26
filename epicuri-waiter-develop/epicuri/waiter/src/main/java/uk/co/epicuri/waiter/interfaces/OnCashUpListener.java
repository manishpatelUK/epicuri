package uk.co.epicuri.waiter.interfaces;

import java.util.Date;

public interface OnCashUpListener {
    void onCashUp(Date fromDate, Date toDate, boolean simulate, boolean canCashUp);
    void showEndOfDay();
}
