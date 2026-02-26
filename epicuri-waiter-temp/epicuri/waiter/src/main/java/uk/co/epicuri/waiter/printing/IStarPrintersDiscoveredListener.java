package uk.co.epicuri.waiter.printing;

import com.starmicronics.stario.PortInfo;

import java.util.List;

public interface IStarPrintersDiscoveredListener {
    void onPrintersDiscovered(List<PortInfo> printers);
}
