package uk.co.epicuri.waiter.printing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

/**
 * Created by manish on 08/02/2018.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {
    private final IPowerConnectionListener powerConnectionListener;

    public PowerConnectionReceiver(IPowerConnectionListener listener) {
        powerConnectionListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        PrintQueueServiceState.LAST_KNOWN_CHARGE_STATE = isCharging ? ChargeState.CHARGING : ChargeState.NOT_CHARGING;

        if(powerConnectionListener != null) {
            powerConnectionListener.onConnectionStatus(context, intent, isCharging);
        }
    }
}
