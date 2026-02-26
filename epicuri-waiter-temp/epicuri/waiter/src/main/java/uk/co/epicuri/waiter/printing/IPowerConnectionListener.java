package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.content.Intent;

/**
 * Created by manish on 12/02/2018.
 */

public interface IPowerConnectionListener {
    void onConnectionStatus(Context context, Intent intent, boolean isCharging);
}
