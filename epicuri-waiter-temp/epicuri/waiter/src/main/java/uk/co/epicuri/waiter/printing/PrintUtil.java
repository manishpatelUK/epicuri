package uk.co.epicuri.waiter.printing;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseArray;

import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import uk.co.epicuri.waiter.interfaces.Printable;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;

import static uk.co.epicuri.waiter.utils.IdUtil.getIntId;

/**
 * Created by manish on 10/01/2018.
 */

public class PrintUtil {
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final String TEST_OUTPUT = "\u001BW0\u001Bh0\nTEST PRINT";

    private static final Executor executor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));


    public static EpicuriMenu.Printer determinePrinter(String printerId,
                                        SparseArray<EpicuriMenu.Printer> printers,
                                        SparseArray<EpicuriPrintRedirect> printerRedirects) {
        EpicuriMenu.Printer printer;
        EpicuriPrintRedirect r = printerRedirects.get(getIntId(printerId));
        if(null != r){
            printer = r.getDestinationPrinter();
        } else {
            printer = printers.get(getIntId(printerId));
        }

        return printer;
    }

    public static void printFromJsonResponse(JSONObject responseJson, Context context, ContextWrapper contextWrapper) throws JSONException {
        ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);
        JSONArray array = responseJson.getJSONArray("batches");
        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
                objBatches.add(batch);
            }
        }

        Intent intent = new Intent(context, PrintDirectlyService.class);
        intent.putExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
        contextWrapper.startService(intent);
    }

    public static void print(Context context, String printerIp, Printable printable, Handler handler) {
        byte[] printOutput = null;
        EpicuriRestaurant epicuriRestaurant = LocalSettings.getStaticCachedRestaurant();
        if(epicuriRestaurant == null) {
            printOutput = printable.getPrintOutput();
        } else {
            printOutput = printable.getPrintOutput(epicuriRestaurant.isDoubleHeight(), epicuriRestaurant.isDoubleWidth());
        }
        try {
            new KitchenPrintTask(context, "TCP:" + printerIp, "")
                    .setHandler(handler)
                    .executeOnExecutor(executor, printOutput);
        }catch (Exception e){
            Log.d("PRINT_EXCEPTION", "RejectedExecutionException");
        }
    }

    public static void kickDrawer(Context context, EpicuriMenu.Printer printer) {
        if(context == null) {
            Log.d("PRINT_EXCEPTION", "Cannot kick drawer: context is null");
            return;
        }
        if(printer == null) {
            Log.d("PRINT_EXCEPTION", "Cannot kick drawer: context is null");
            return;
        }
        KickDrawerTask task = new KickDrawerTask(context, "TCP:" + printer.getIpAddress(), "");
        task.execute();
    }

    public static PortInfo searchPrinterByMacAddress(String mac) {
        if(mac == null || mac.trim().length() < 12) {
            return null;
        }

        mac = mac.trim();

        try {
            List<PortInfo> printers = searchAllLANPrinters();
            for(PortInfo portInfo : printers) {
                if(portInfo.getMacAddress() != null && portInfo.getMacAddress().trim().equalsIgnoreCase(mac)) {
                    return portInfo;
                }
            }
        } catch (StarIOPortException e) {

            //todo

            return null;
        }

        return null;
    }

    public static StarPrinterStatus getPrinterStatus(String address, Context context) {
        StarIOPort port = null;
        try {
            port = StarIOPort.getPort("TCP:"+address, "", 3000, context);
            return port != null ? port.retreiveStatus() : null;
        } catch (StarIOPortException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {}
            }
        }
    }

    public static List<PortInfo> searchAllLANPrinters() throws StarIOPortException {
        if(Build.VERSION.SDK_INT > 25){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        return StarIOPort.searchPrinter("TCP:");
    }

    public static void testPrint(Context context, String ipAddress) {
        byte[] toPrint = ("------\n\n" + TEST_OUTPUT + ": " + ipAddress + "\n\n------").getBytes();
        try {
            new KitchenPrintTask(context, ipAddress.startsWith("TCP:") ? ipAddress : "TCP:" + ipAddress, "")
                    .setHandler(new PostPrintToastHandler(new WeakReference<>(context)))
                    .executeOnExecutor(executor, toPrint);
        }catch (Exception e){
            Log.d("PRINT_EXCEPTION", "RejectedExecutionException");
        }
    }
}
