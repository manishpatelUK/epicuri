package uk.co.epicuri.waiter.printing;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import uk.co.epicuri.waiter.interfaces.PrintQueueListener;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.utils.GlobalSettings;

import static uk.co.epicuri.waiter.utils.IdUtil.getIntId;

/**
 * Created by manish on 11/01/2018.
 */

public class BatchPrintedHandler extends Handler {
    private static final String LOG = "BatchPrintedHandler";

    private final WeakReference<PrintQueueService> mService;
    private final PrintQueueListener listener;
    private final EpicuriPrintBatch currentJob;

    public BatchPrintedHandler(PrintQueueService printQueueService, PrintQueueListener listener, EpicuriPrintBatch currentJob) {
        this.mService = new WeakReference<>(printQueueService);
        this.listener = listener;
        this.currentJob = currentJob;
    }

    @Override
    public void handleMessage(Message msg) {
        PrintQueueService service = mService.get();
        if(currentJob == null || service == null) {
            return;
        }

        if(msg.what == 0){
            Log.d(LOG, "Printing succeeding - mark the job as done on server");
            service.markBatchAsPrinted(currentJob);
            service.setCurrentJob(null);

            if(!service.isRunning()) {
                return;
            }

            PrintQueueServiceState.addCompletedJob(currentJob);
            PrintQueueServiceState.incrementNumberOfJobs();
            if(PrintQueueServiceState.getJobAt(getIntId(currentJob.getId())) != null){
                PrintQueueServiceState.removeErroredJob(getIntId(currentJob.getId())); // clear error if there was one for this job
                if(null != listener) listener.itemFailed();
            }
            if(null != listener) listener.itemPrinted(currentJob);
            /*service.startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, service.getNotification());*/
        } else {
            Log.d(LOG, "Print job failed, abandoning " + currentJob.getId());
            PrintQueueServiceState.addErroredJob(currentJob);
            if(null != listener) listener.itemFailed();
            /*service.startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, service.getNotification());*/
        }
    }
}
