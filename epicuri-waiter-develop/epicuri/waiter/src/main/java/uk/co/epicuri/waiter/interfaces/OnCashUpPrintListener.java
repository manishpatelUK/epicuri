package uk.co.epicuri.waiter.interfaces;

import java.util.Date;

public interface OnCashUpPrintListener {
    void onCashUp(final Date fromDate, final Date toDate, final boolean simulate, final boolean canCashUp);
}
