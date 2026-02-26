package uk.co.epicuri.waiter.printing;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.webservice.GetPrinterRedirectsWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;


public class PrintDirectlyService extends Service {
    private static final String LOGGER = "PrintSilentlyService";

    public static String BATCH_EXTRA = "batch_extra";
    private SparseArray<EpicuriMenu.Printer> printers;
    private SparseArray<EpicuriPrintRedirect> printerRedirects;
    private ArrayList<EpicuriPrintBatch> currentJobs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if(extras != null) {
            loadJobsAndPrinters(extras);
        }

        return Service.START_NOT_STICKY;
    }

    private void loadJobsAndPrinters(Bundle extras) {
        ArrayList<EpicuriPrintBatch> newJobs = extras.getParcelableArrayList(BATCH_EXTRA);
        if (currentJobs == null) {
            currentJobs = new ArrayList<>();
        }
        for (EpicuriPrintBatch batch : newJobs) {
            if (!currentJobs.contains(batch)) {
                currentJobs.add(batch);
            }
        }

        if (currentJobs != null) loadPrinters();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void updatePrinterIp(String id, String ipAddress) {
        EpicuriMenu.Printer printer = printers.get(getIntId(id));
        if(printer != null) {
            EpicuriMenu.Printer newPrinter = new EpicuriMenu.Printer(id, printer.getName(), ipAddress, printer.getPrinterType(), printer.getMacAddress());
            printers.put(getIntId(id), newPrinter);
        }
    }

    public void printBatch(EpicuriPrintBatch batch){
        EpicuriMenu.Printer printer = PrintUtil.determinePrinter(batch.getPrinterId(), printers, printerRedirects);
        String printerIp = null;
        if(null != printer && printer.isPhysical()){
            printerIp = printer.getIpAddress();
            batch.setPrinterName(printer.getName());
        }

        if(null != printer && !printer.isPhysical()) {
            return;
        }

        BatchDirectPrintingHandler batchPrintedHandler = new BatchDirectPrintingHandler(new WeakReference<>(this), batch, printer);
        if(null == printerIp){
            // cannot print this item
            batchPrintedHandler.sendMessage(batchPrintedHandler.obtainMessage(1));
        } else {
            PrintUtil.print(this, printerIp, batch, batchPrintedHandler);
        }
    }

    private int getIntId(String id) {
        if(id == null) {
            return 0;
        } else {
            return Math.abs(id.hashCode());
        }
    }

    public void loadPrinters(){
        WebServiceTask task1 = new WebServiceTask(this, new GetPrintersWebServiceCall());
        task1.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if(null == response){
                    Toast.makeText(PrintDirectlyService.this, "Nothing to print", Toast
                            .LENGTH_SHORT).show();

                    return;
                }
                try{
                    JSONArray responseJson = new JSONArray(response);
                    SparseArray<EpicuriMenu.Printer> printers = new SparseArray<EpicuriMenu.Printer>(responseJson.length());
                    for(int i=0; i<responseJson.length(); i++){
                        EpicuriMenu.Printer p = new EpicuriMenu.Printer(responseJson.getJSONObject(i));
                        printers.put(getIntId(p.getId()), p);
                    }

                    PrintDirectlyService.this.printers = printers;
                } catch (JSONException e){
                    Toast.makeText(PrintDirectlyService.this, "Cannot print currently", Toast.LENGTH_SHORT)
                            .show();
                    e.printStackTrace();
                }

                WebServiceTask task2 = new WebServiceTask(PrintDirectlyService.this, new GetPrinterRedirectsWebServiceCall());
                task2.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

                    @Override
                    public void onSuccess(int code, String response) {
                        if(null == response){
                            Toast.makeText(PrintDirectlyService.this, "Cannot print currently", Toast.LENGTH_SHORT)
                                    .show();

                            return;
                        }
                        try{
                            JSONArray responseJson = new JSONArray(response);
                            SparseArray<EpicuriPrintRedirect> printerRedirects = new SparseArray<>(responseJson.length());
                            for(int i=0; i<responseJson.length(); i++){
                                EpicuriPrintRedirect p = new EpicuriPrintRedirect(responseJson.getJSONObject(i));
                                printerRedirects.put(getIntId(p.getSourcePrinter().getId()), p);
                            }
                            PrintDirectlyService.this.printerRedirects = printerRedirects;

                            for (EpicuriPrintBatch batch : currentJobs) {
                                printBatch(batch);
                            }

                        } catch (JSONException e){
                            Toast.makeText(PrintDirectlyService.this, "Cannot print currently", Toast.LENGTH_SHORT)
                                    .show();
                            e.printStackTrace();
                        }
                    }
                });
                task2.execute();
            }
        });

        task1.execute();
    }
}
