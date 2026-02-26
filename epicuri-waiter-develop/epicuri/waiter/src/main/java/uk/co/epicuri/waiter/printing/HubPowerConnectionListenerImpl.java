package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.content.Intent;

/**
 * Created by manish on 12/02/2018.
 */

public class HubPowerConnectionListenerImpl implements IPowerConnectionListener {

    @Override
    public void onConnectionStatus(Context context, Intent intent, boolean isCharging) {
        PrintQueueService mPrintQueue = PrintQueueService.mPrintQueue;
        boolean mBound = PrintQueueService.mBound;

        if(mPrintQueue == null || !mBound) {
            return;
        }

        boolean running = mPrintQueue.isRunning();

        if (isCharging && !running) {
            mPrintQueue.startProcessingPrintQueue();
        } else if(!isCharging && running && !PrintQueueServiceState.SWITCHED_ON_MANUALLY){
            mPrintQueue.stopProcessingPrintQueue(false);
        } else if(!isCharging) {
            PrintQueueServiceState.SWITCHED_ON_MANUALLY = false; //reset
        }
    }
}
