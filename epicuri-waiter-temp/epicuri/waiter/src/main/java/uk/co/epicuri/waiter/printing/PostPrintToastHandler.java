package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class PostPrintToastHandler extends Handler {
    private final WeakReference<Context> contextWeakReference;

    public PostPrintToastHandler(WeakReference<Context> contextWeakReference) {
        this.contextWeakReference = contextWeakReference;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what != 0) {
            Toast.makeText(contextWeakReference.get(), "Test print failed (printer or tablet is offline)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(contextWeakReference.get(), "Sent to printer", Toast.LENGTH_SHORT).show();
        }
    }
}
