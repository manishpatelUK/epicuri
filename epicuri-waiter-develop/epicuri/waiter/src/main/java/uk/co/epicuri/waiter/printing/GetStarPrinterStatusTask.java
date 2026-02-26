package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.os.AsyncTask;

import com.starmicronics.stario.StarPrinterStatus;

import java.lang.ref.WeakReference;

public class GetStarPrinterStatusTask extends AsyncTask<String,Integer,StarPrinterStatus> {
    private final WeakReference<Context> contextWeakReference;
    private final IStarPrinterStatusListener listener;

    public GetStarPrinterStatusTask(Context context, IStarPrinterStatusListener listener) {
        this.contextWeakReference = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected StarPrinterStatus doInBackground(String... strings) {
        StarPrinterStatus printerStatus = PrintUtil.getPrinterStatus(strings[0], contextWeakReference.get());
        listener.onStatusRetrieved(printerStatus);
        return printerStatus;
    }
}
