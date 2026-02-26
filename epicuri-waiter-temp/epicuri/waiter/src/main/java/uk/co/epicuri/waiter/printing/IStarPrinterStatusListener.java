package uk.co.epicuri.waiter.printing;

import com.starmicronics.stario.StarPrinterStatus;

public interface IStarPrinterStatusListener {
    void onStatusRetrieved(StarPrinterStatus starPrinterStatus);
}
