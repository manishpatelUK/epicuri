package uk.co.epicuri.waiter.printing;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.starmicronics.stario.PortInfo;

import java.lang.ref.WeakReference;
import java.util.Collections;

import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.webservice.EditPrinterWebServiceCall;
import uk.co.epicuri.waiter.webservice.MarkBatchAsPrintedWebServiceCall;
import uk.co.epicuri.waiter.webservice.RequeueBatchWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by manish on 11/01/2018.
 */

public class BatchDirectPrintingHandler extends Handler {
    private static final String LOG = "BatchPrintedHandler";
    private static final String IP_ADDRESS = "ipAddress";
    public static final int MESSAGE_WITH_IP = 1;
    public static final int MESSAGE_WITHOUT_IP = 0;
    private final WeakReference<PrintDirectlyService> mService;
    private final EpicuriPrintBatch batch;
    private final EpicuriMenu.Printer printer;
    PrintDirectlyService service;

    public BatchDirectPrintingHandler(WeakReference<PrintDirectlyService> service, EpicuriPrintBatch batch, EpicuriMenu.Printer printer) {
        this.mService = service;
        this.batch = batch;
        this.printer = printer;
    }

    private Handler serviceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if(message.what == MESSAGE_WITH_IP) {
                String ipAddress = message.getData().getString(IP_ADDRESS, "");
                service.updatePrinterIp(printer.getId(), ipAddress);
                service.printBatch(batch);
                //update the mac address & ip
                new WebServiceTask(service, new EditPrinterWebServiceCall(printer.getId(), ipAddress, printer.getMacAddress()), false).execute();
            } else if (message.what == MESSAGE_WITHOUT_IP){
                respool(service);
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        service = mService.get();

        if (msg.what != 0 && service != null) {
            // don't start print queue here - it's causing some thread issue
            // service.startQueue();
            Toast.makeText(service, "Printer is offline, or this tablet is on the wrong network!", Toast.LENGTH_LONG).show();

            //try to find the printer by mac address, if there is none, respool
            if(batch != null && (printer.getMacAddress() == null || printer.getMacAddress().length() < 12)) {
                respool(service);
            } else {
                new Thread(new Runnable() {
                    @Override public void run() {
                        PortInfo portInfo = PrintUtil.searchPrinterByMacAddress(printer.getMacAddress());
                        if(portInfo != null) {
                            String ipAddress = portInfo.getPortName();
                            if(ipAddress.startsWith("TCP:")) {
                                ipAddress = ipAddress.substring(4);
                            }

                            Message message = serviceHandler.obtainMessage(MESSAGE_WITH_IP);
                            message.getData().putString(IP_ADDRESS, ipAddress);
                            message.sendToTarget();
                        } else {
                            if(batch != null) {
                                Message message = serviceHandler.obtainMessage(MESSAGE_WITHOUT_IP);
                                message.sendToTarget();
                            }
                        }
                    }
                }).start();

            }
        } else if(service != null){
            MarkBatchAsPrintedWebServiceCall call = new MarkBatchAsPrintedWebServiceCall(Collections.singletonList(batch.getId()));
            WebServiceTask task = new WebServiceTask(service, call);
            task.execute();

            //update the mac address if it isn't on the object already
            if(printer.getMacAddress() == null || printer.getMacAddress().length() < 12) {
                new WebServiceTask(service, new EditPrinterWebServiceCall(printer.getId(), printer.getIpAddress(), printer.getMacAddress()), false).execute();
            }
        }
        service.stopSelf();
    }

    private void respool(final PrintDirectlyService service) {
        RequeueBatchWebServiceCall call = new RequeueBatchWebServiceCall(Collections.singletonList(batch.getId()));
        Log.d(LOG, "Requeue batch ID " + batch.getId());
        WebServiceTask task = new WebServiceTask(service, call);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Log.d(LOG, "Requeue successful");
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Log.d(LOG, "Could not respool job on server");
                Toast.makeText(service, "Error connecting to server - check your prints!", Toast.LENGTH_LONG).show();
            }
        });
        task.execute();
    }
}
