package uk.co.epicuri.waiter.printing;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.starmicronics.stario.PortInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.PrintQueueListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.IdUtil;
import uk.co.epicuri.waiter.webservice.CancelBatchWebServiceCall;
import uk.co.epicuri.waiter.webservice.EditPrinterWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrintBatchesWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrinterRedirectsWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.MarkBatchAsPrintedWebServiceCall;
import uk.co.epicuri.waiter.webservice.RequeueBatchWebServiceCall;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.utils.IdUtil.getIntId;

public class PrintQueueService extends Service {
    private static final String LOG = "PrintQueueService";

    private static final int PRINTER_REFRESH_PERIOD = 300000;
    private static final int REFRESH_PERIOD_SECONDS = 30;
    private static final int REFRESH_PERIOD_CHARGING_SECONDS = 10;

    /**
     * Expiry threshold of tasks, they'll be resent if older than this
     */
    private static final int REFRESH_THRESHOLD = 100000;
    private static final String IP_ADDRESS = "ipAddress";
    private static final String BATCH = "batch";
    private static final String PRINTER = "printer";

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private int refreshPeriod = REFRESH_PERIOD_SECONDS;
    private EpicuriPrintBatch currentJob;
    private ScheduledFuture<?> scheduleHandler;
    private SharedPreferences prefs;

    /**
     * local store of printers: ids, names, IP addresses
     */
    private SparseArray<EpicuriMenu.Printer> printers;
    private SparseArray<EpicuriPrintRedirect> printerRedirects;

    /**
     * Keep service here so we can access from all classes
     */
    public static PrintQueueService mPrintQueue = null;
    public static boolean mBound = false;

    private final IBinder binder = new PrintQueueBinder();
    private PrintQueueListener listener;
    private boolean requestError = false;

    public class PrintQueueBinder extends Binder {
        public PrintQueueService getService() {
            return PrintQueueService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG, "onStartCommand called");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(powerConnectionReceiver, filter);
        prefs = getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
        PrintQueueServiceState.resetNumberOfJobs();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "onDestroy called");
        unregisterReceiver(powerConnectionReceiver);
        super.onDestroy();
    }

    public void startProcessingPrintQueue() {
        if (isRunning()) {
            Log.v(LOG, "startProcessingPrintQueue called, already running");
            return;
        }

        Log.d(LOG, "self-starting the print queue");
        startService(new Intent(this, PrintQueueService.class));

        if (null == printers || null == printerRedirects) {
            loadPrinters();
            Log.d(LOG, "Printers not loaded yet");
            if (null != listener) {
                listener.statusChanged();
            }
        }

        Log.d(LOG, "clearing current jobs");
        clearErrorJobs();

        Log.d(LOG, "rescheduling refresh");
        rescheduleRefreshPeriod(true);
        Toast.makeText(this, "Print queue service started", Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(GlobalSettings.PREF_KEY_PRINT_QUEUE, true);
        editor.commit();
    }

    private void clearErrorJobs() {
        PrintQueueServiceState.clearErroredJobs();
    }

    public void stopProcessingPrintQueue(boolean saveState) {
        Log.d(LOG, "stopping print queue... mark printed batches as printed");
        markBatchesAsPrinted(PrintQueueServiceState.getCompletedJobs());

        Log.d(LOG, "clear jobs and stop foreground");
        clearErrorJobs();
        stopForeground(true);

        if (scheduleHandler != null) {
            Log.d(LOG, "cancel scheduler");
            Toast.makeText(this, "Stopping print queue", Toast.LENGTH_SHORT).show();
            scheduleHandler.cancel(true);
        }
        scheduleHandler = null;
        if (null != listener) {
            listener.statusChanged();
        }

        stopSelf();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(GlobalSettings.PREF_KEY_PRINT_QUEUE, saveState);
        editor.commit();
    }


    final Runnable getAndProcessQueue = new Runnable() {
        @Override
        public void run() {
            // make sure we're still logged in
            if (!TokenManager.checkToken(PrintQueueService.this)) {
                stopProcessingPrintQueue(false);
                return;
            }

            /* actually start processing the queue */
            getQueueFromServer();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * register as a listener for events
     *
     * @param l
     */
    public void setListener(PrintQueueListener l) {
        listener = l;
    }

    /**
     * generate the notification based on the current status
     */
    /*Notification getNotification() throws NullPointerException {
        // when clicked, show the epicuri print queue activity
        Intent notificationIntent = new Intent(this, PrintQueueActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(this, GlobalSettings.CHANNEL_PRINT)
                .setContentTitle("Epicuri Print Queue")
                .setContentText(getStatus())
                .setContentIntent(pendingIntent)
                .setSmallIcon(requestError || PrintQueueServiceState.getNumberOfErroredJobs() > 0 ? R.drawable.notify_print_error : R.drawable.notify_print).build();
    }*/

    /**
     * load new print queue
     */
    private void getQueueFromServer() {
        Log.d(LOG, "get queue from server");
        WebServiceTask task = new WebServiceTask(this, new GetPrintBatchesWebServiceCall());
        task.setOnErrorListener(mErrorListener);
        task.setOnCompleteListener(mSuccessListener);
        task.execute();
    }

    final WebServiceTask.OnErrorListener mErrorListener = new WebServiceTask.OnErrorListener() {
        @Override
        public void onError(int code, String response) {
            Toast.makeText(PrintQueueService.this, "Error loading print queue", Toast.LENGTH_SHORT).show();
            PrintQueueServiceState.REFRESH_TIME = System.currentTimeMillis();
            setErrorState(true);
        }
    };

    final WebServiceTask.OnSuccessListener mSuccessListener = new WebServiceTask.OnSuccessListener() {
        @Override
        public void onSuccess(int code, String response) {
            if (null == response) {
                Toast.makeText(PrintQueueService.this, "No response for print queue", Toast.LENGTH_SHORT).show();
                setErrorState(true);
            } else {
                try {
                    JSONArray responseJson = new JSONArray(response);
                    Date refreshDate = new Date();
                    for (int i = 0; i < responseJson.length(); i++) {
                        EpicuriPrintBatch batch = new EpicuriPrintBatch(responseJson.getJSONObject(i), refreshDate);
                        boolean added = PrintQueueServiceState.addPending(batch);
                        if (added) {
                            Log.d(LOG, "added item to pending list: " + batch.getId());
                        }
                    }
                    PrintQueueServiceState.REFRESH_TIME = System.currentTimeMillis();
                    setErrorState(false);
                } catch (JSONException e) {
                    Toast.makeText(PrintQueueService.this, "Print batches did not load", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    setErrorState(true);
                }
            }
            // kick off a process
            processQueue();
        }
    };

    private void setErrorState(boolean error) {
        if (error != requestError) {
            requestError = error;
            /*try {
                startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, getNotification());
            } catch (NullPointerException e) {
                //
            }*/
            if (null != listener) {
                listener.statusChanged();
            }
        }
    }

    public void loadPrinters() {
        Log.d(LOG, "Load printers");
        WebServiceTask task = new WebServiceTask(this, new GetPrintersWebServiceCall());
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if (null == response) {
                    return;
                }
                try {
                    JSONArray responseJson = new JSONArray(response);
                    SparseArray<EpicuriMenu.Printer> printers = new SparseArray<EpicuriMenu.Printer>(responseJson.length());
                    for (int i = 0; i < responseJson.length(); i++) {
                        EpicuriMenu.Printer p = new EpicuriMenu.Printer(responseJson.getJSONObject(i));
                        printers.put(getIntId(p.getId()), p);
                    }
                    PrintQueueService.this.printers = printers;
//					if(!isRunning()){
//						startProcessingPrintQueue();
//					}
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute();

        task = new WebServiceTask(this, new GetPrinterRedirectsWebServiceCall());
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if (null == response) {
                    return;
                }
                try {
                    JSONArray responseJson = new JSONArray(response);
                    SparseArray<EpicuriPrintRedirect> printerRedirects = new SparseArray<>(responseJson.length());
                    for (int i = 0; i < responseJson.length(); i++) {
                        EpicuriPrintRedirect p = new EpicuriPrintRedirect(responseJson.getJSONObject(i));
                        printerRedirects.put(getIntId(p.getSourcePrinter().getId()), p);
                    }
                    PrintQueueService.this.printerRedirects = printerRedirects;
//					if(!isRunning()){
//						startPrintService(PrintQueueService.this);
//					}
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute();
        PrintQueueServiceState.PRINTER_REFRESH_TIME = System.currentTimeMillis();
    }

    /**
     * cancel all error jobs by telling server they're printed
     */
    public void cancelAll() {
        Log.d(LOG, "cancel all jobs");
        if (EpicuriApplication.getInstance(this).getApiVersion() >= GlobalSettings.API_VERSION_6) {
            cancelApiCall(null);
        } else {
            markBatchesAsPrinted(PrintQueueServiceState.getErroredJobs());
            requeueAll();
        }
        requestError = false;
    }

    private void cancelApiCall(final List<EpicuriPrintBatch> errorJobs) {
        Log.d(LOG, "cancel batch jobs on server: " + (errorJobs == null ? "ALL" : errorJobs.size()) + " jobs");

        List<String> batchIds = buildBatchIds(errorJobs);

        CancelBatchWebServiceCall call;
        if (errorJobs == null) {
            call = new CancelBatchWebServiceCall();
        } else {
            call = new CancelBatchWebServiceCall(batchIds, false);
        }
        executeCancel(errorJobs, call);
    }

    private void executeCancel(final List<EpicuriPrintBatch> errorJobs, CancelBatchWebServiceCall call) {
        WebServiceTask task = new WebServiceTask(this, call);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                if (errorJobs == null) {
                    return;
                }

                PrintQueueServiceState.removeErroredJob(errorJobs);
                if (listener != null) {
                    listener.itemCancelOrRequeue();
                }
            }
        });
        task.execute();
    }

    private List<String> buildBatchIds(List<EpicuriPrintBatch> errorJobs) {
        if (errorJobs == null) {
            return null;
        }

        ArrayList<String> batchIds = new ArrayList<>();
        for (EpicuriPrintBatch batch : errorJobs) {
            batchIds.add(batch.getId());
        }
        return batchIds;
    }

    /**
     * requeue all errored print jobs
     */
    public void requeueAll() {
        Log.d(LOG, "requeueAll called");
        clearErrorJobs();
        if (null != listener) listener.itemFailed();
        /*if (isRunning()) {
            Log.d(LOG, "isRunning is true - call startForeground");
            startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, getNotification());
        }*/

        requestError = false;
    }

    public void requeueApiCall() {
        Log.d(LOG, "mark jobs for requeue on server");
        List<EpicuriPrintBatch> errorJobs = PrintQueueServiceState.getErroredJobs();

        if (errorJobs.isEmpty()) return;

        ArrayList<String> batchIds = new ArrayList<>(errorJobs.size());
        for (EpicuriPrintBatch batch : errorJobs) {
            batchIds.add(batch.getId());
        }

        RequeueBatchWebServiceCall call = new RequeueBatchWebServiceCall(batchIds);
        WebServiceTask task = new WebServiceTask(this, call);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                requeueAll();
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override public void onError(int code, String response) {
                requeueAll();
            }
        });
        task.execute();
    }

    public void requeueSingleCall(final EpicuriPrintBatch batch) {
        Log.d(LOG, "mark 1 job for requeue on server");
        ArrayList<String> batchIds = new ArrayList<>(1);
        batchIds.add(batch.getId());

        RequeueBatchWebServiceCall call = new RequeueBatchWebServiceCall(batchIds);
        WebServiceTask task = new WebServiceTask(this, call);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                requeueJob(batch);
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override public void onError(int code, String response) {
                requeueJob(batch);
            }
        });
        task.execute();
    }

    /**
     * cancel specific job by telling server that it's printed
     */
    public void cancelJob(EpicuriPrintBatch batch) {
        if (EpicuriApplication.getInstance(this).getApiVersion() >= GlobalSettings.API_VERSION_6) {
            ArrayList<EpicuriPrintBatch> batches = new ArrayList<>(1);
            batches.add(batch);
            cancelApiCall(batches);
        } else {
            markBatchAsPrinted(batch);
        }
    }

    /**
     * forget about a job so that it's processed again next time round
     */
    public void requeueJob(EpicuriPrintBatch batch) {
        Log.d(LOG, "requeue single job");
        PrintQueueServiceState.removeErroredJob(getIntId(batch.getId()));

        if (null != listener) {
            listener.itemFailed();
            listener.statusChanged();
        }
        /*if (isRunning()) {
            Log.d(LOG, "isRunning is true - call startForeground");
            startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, getNotification());
        }*/
    }


    /**
     * do a pass of the queue
     */
    void processQueue() {
        Log.d(LOG, "process queue");
        long now = System.currentTimeMillis();
        expireErrors();

        if (now - PrintQueueServiceState.PRINTER_REFRESH_TIME > PRINTER_REFRESH_PERIOD) {
            // refresh printers every X minutes
            loadPrinters();
            PrintQueueServiceState.PRINTER_REFRESH_TIME = now;
        }
        if (null == printers || null == printerRedirects) {
            Log.d(LOG, "Printers not loaded");
            return;
        }

        // getting close to the next refresh, so finish up and exit
        List<EpicuriPrintBatch> completedJobs = PrintQueueServiceState.getCompletedJobs();
        if (now - PrintQueueServiceState.REFRESH_TIME > REFRESH_THRESHOLD) {
            markBatchesAsPrinted(completedJobs);
            return;
        } else {
            if (now - PrintQueueServiceState.REPLY_TIME > REFRESH_PERIOD_SECONDS * 1000
                    && markBatchesAsPrinted(completedJobs)) {
                return;
            }
            if (!PrintQueueServiceState.anyPendingJobs()) {
                if (markBatchesAsPrinted(completedJobs)) {
                    return;
                }
            } else {
                while (PrintQueueServiceState.anyPendingJobs()) {
                    currentJob = PrintQueueServiceState.popPending();
                    printBatch(currentJob);
                }
                return;
            }
        }
    }

    void setCurrentJob(EpicuriPrintBatch currentJob) {
        this.currentJob = currentJob;
    }

    /**
     * Whether print queue is running
     *
     * @return true if print queue is running, false otherwise
     */
    public boolean isRunning() {
        return scheduleHandler != null;
    }

    /**
     * get text status of printer
     *
     * @return string representing print queue processing state
     */
    public CharSequence getStatus() {
        if (!isRunning()) return "Stopped";
        if (requestError) return "Error getting queue from server";

        StringBuilder sb = new StringBuilder("Running: ").append(PrintQueueServiceState.getNumberOfJobs()).append(" jobs printed.");
        if (PrintQueueServiceState.anyErroredJobs()) {
            sb.append(" ").append(PrintQueueServiceState.getNumberOfErroredJobs()).append(" errors.");
        }
        sb.append(" " + refreshPeriod + "s refresh");
        return sb;
    }

    private Handler printJobHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            EpicuriMenu.Printer printer = message.getData().getParcelable(PRINTER);

            Iterator<EpicuriPrintBatch> iterator = jobsMap.get(printer).iterator();
            jobsMap.remove(printer);
            while (iterator.hasNext()){
                String newIpAddress = message.getData().getString(IP_ADDRESS);
                initiatePrintJob(iterator.next(), newIpAddress);
            }
        }
    };

    volatile ConcurrentHashMap<EpicuriMenu.Printer, ArrayList<EpicuriPrintBatch>> jobsMap = new ConcurrentHashMap<>();

    /**
     * send a print job to the printer
     *
     * @param batch
     */
    private void printBatch(final EpicuriPrintBatch batch) {
        final EpicuriMenu.Printer printer = PrintUtil.determinePrinter(batch.getPrinterId(), printers, printerRedirects);
        String printerIp = null;
        if (null != printer && printer.isPhysical()) {
            printerIp = printer.getIpAddress();
            batch.setPrinterName(printer.getName());
        }

        if (null != printer && !printer.isPhysical()) {
            return;
        }

        //search for the printer
        if (printer != null && printer.getMacAddress() != null) {
            if (jobsMap.containsKey(printer)) {
                jobsMap.get(printer).add(batch);
            } else {
                jobsMap.put(printer, new ArrayList<EpicuriPrintBatch>());
                jobsMap.get(printer).add(batch);
                new Thread(new Runnable() {
                    @Override public void run() {
                        PortInfo portInfo = PrintUtil.searchPrinterByMacAddress(printer.getMacAddress());
                        if (portInfo != null) {
                            String newIpAddress = updateIPAddressIfRequired(printer, portInfo);
                            Message message = printJobHandler.obtainMessage(1);
                            message.getData().putString(IP_ADDRESS, newIpAddress);
                            message.getData().putParcelable(PRINTER, printer);
                            message.sendToTarget();
                        }else {
                            //else -- let it get respooled automatically
                            jobsMap.remove(printer);
                        }
                    }
                }).start();
            }
        } else {
            initiatePrintJob(batch, printerIp);
        }
    }

    private void initiatePrintJob(EpicuriPrintBatch batch, String printerIp) {
        BatchPrintedHandler batchPrintedHandler = new BatchPrintedHandler(this, listener, currentJob);
        if (null == printerIp) {
            // cannot print this item
            batchPrintedHandler.sendMessage(batchPrintedHandler.obtainMessage(1));
        } else {
            PrintUtil.print(this, printerIp, batch, batchPrintedHandler);
        }
    }

    private String updateIPAddressIfRequired(EpicuriMenu.Printer printer, PortInfo portInfo) {
        String ipAddress = portInfo.getPortName();
        if (ipAddress.startsWith("TCP:")) {
            ipAddress = ipAddress.substring(4);
        }
        if (!ipAddress.equals(printer.getIpAddress())) {
            new WebServiceTask(this, new EditPrinterWebServiceCall(printer.getId(), ipAddress, printer.getMacAddress()), false).execute();
        }

        return ipAddress;
    }

    /**
     * mark single batch as printed, sending to server
     *
     * @param batch
     */
    public void markBatchAsPrinted(EpicuriPrintBatch batch) {
        Log.d(LOG, "Mark " + batch.getId() + " as printed");
        ArrayList<String> batchIds = new ArrayList<>(1);
        batchIds.add(batch.getId());
        MarkBatchAsPrintedWebServiceCall call = new MarkBatchAsPrintedWebServiceCall(batchIds);
        WebServiceTask task = new WebServiceTask(this, call);
        task.execute();
    }

    /**
     * tell the server that the batches have been printed
     *
     * @param jobsToComplete jobs to mark as printed
     * @return true if jobs were printed, false if no jobs were specified
     */
    private boolean markBatchesAsPrinted(final List<EpicuriPrintBatch> jobsToComplete) {
        if (jobsToComplete.isEmpty()) return false;

        ArrayList<String> batchIds = new ArrayList<>(jobsToComplete.size());
        for (EpicuriPrintBatch batch : jobsToComplete) {
            batchIds.add(batch.getId());
        }
        final MarkBatchAsPrintedWebServiceCall call = new MarkBatchAsPrintedWebServiceCall(batchIds);
        WebServiceTask task = new WebServiceTask(this, call);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                PrintQueueServiceState.clearCompletedJobs(jobsToComplete);
                PrintQueueServiceState.REPLY_TIME = System.currentTimeMillis();
                processQueue();
            }
        });
        task.execute();
        return true;
    }

    /**
     * any errored jobs created earlier than {@link #REFRESH_THRESHOLD} will have expired
     * so will be removed from the local queue
     */
    private void expireErrors() {

        Date expiredJobTime = new Date(new Date().getTime() - REFRESH_THRESHOLD);

        boolean aChangeHasOccurred = false;
        for (EpicuriPrintBatch batch : PrintQueueServiceState.getErroredJobs()) {
            if (null != batch
                    && batch.getQueuedTime().before(expiredJobTime)) {
                PrintQueueServiceState.removeErroredJob(IdUtil.getIntId(batch.getId()));
                aChangeHasOccurred = true;
            }
        }

        if (aChangeHasOccurred) {
            /*startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, getNotification());*/
            if (null != listener) {
                listener.statusChanged();
                listener.itemFailed();
            }

        }
    }

    final BroadcastReceiver powerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            refreshPeriod = isCharging ? REFRESH_PERIOD_CHARGING_SECONDS : REFRESH_PERIOD_SECONDS;

            rescheduleRefreshPeriod(false);
        }
    };

    /**
     * schedule the print refresh
     *
     * @param startIfNotRunning set to true to start up the print queue; false will only adjust the period
     */
    private void rescheduleRefreshPeriod(boolean startIfNotRunning) {
        if (startIfNotRunning || null != scheduleHandler) {
            if (null != scheduleHandler) {
                scheduleHandler.cancel(false);
            }

            scheduleHandler = scheduler.scheduleAtFixedRate(getAndProcessQueue, refreshPeriod, refreshPeriod, TimeUnit.SECONDS);
            if (null != listener) {
                listener.statusChanged();
            }
            /*try {
                startForeground(GlobalSettings.NOTIFICATION_PRINT_QUEUE, getNotification());
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            if (null != listener) listener.statusChanged();
        }
    }
}