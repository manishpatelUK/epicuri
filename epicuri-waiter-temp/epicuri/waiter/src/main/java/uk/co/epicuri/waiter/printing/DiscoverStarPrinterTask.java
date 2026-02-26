package uk.co.epicuri.waiter.printing;

import android.os.AsyncTask;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPortException;

import java.util.ArrayList;
import java.util.List;

public class DiscoverStarPrinterTask extends AsyncTask<Void,Integer,List<PortInfo>> {
    private final IStarPrintersDiscoveredListener listener;

    public DiscoverStarPrinterTask(IStarPrintersDiscoveredListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<PortInfo> doInBackground(Void... voids) {
        try {
            List<PortInfo> list = PrintUtil.searchAllLANPrinters();
            listener.onPrintersDiscovered(list);
            return list;
        } catch (StarIOPortException e) {
            return new ArrayList<>();
        }
    }
}
