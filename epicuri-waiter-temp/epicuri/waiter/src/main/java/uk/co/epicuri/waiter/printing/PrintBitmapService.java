package uk.co.epicuri.waiter.printing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.starmicronics.stario.StarIOPortException;

import java.io.File;
import java.io.FileOutputStream;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.ui.CashUpActivity;
import uk.co.epicuri.waiter.ui.SeatedSessionActivity;
import uk.co.epicuri.waiter.ui.TakeawayActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;

/**
 * A service to handle receipt print tasks on a separate thread and report errors via notifications
 */
public class PrintBitmapService extends IntentService {

    private static final String ACTION_PRINT_RECEIPT = "uk.co.epicuri.waiter.view.printing.action.PRINT_RECEIPT";
    private static final String ACTION_PRINT_CASHUP = "uk.co.epicuri.waiter.view.printing.action.PRINT_CASHUP";

    private static final String EXTRA_READABLE_SESSION_ID = "uk.co.epicuri.waiter.view.printing"
            + ".extra.READABLE_SESSION_ID";
    private static final String EXTRA_REAL_SESSION_ID = "uk.co.epicuri.waiter.view.printing.extra"
            + ".SESSION_ID";
    private static final String EXTRA_IMAGE_FILENAME = "uk.co.epicuri.waiter.view.printing.extra.RECEIPT_IMAGE_FILENAME";
    private static final String EXTRA_PRINTER = "uk.co.epicuri.waiter.view.printing.extra.PRINTER";
    private static final String EXTRA_ATTEMPT = "uk.co.epicuri.waiter.view.printing.extra.ATTEMPT";
    private static final String EXTRA_IS_SEATED = "uk.co.epicuri.waiter.view.printing.extra"
            + ".IS_SEATED";

    private static final int MAX_ATTEMPTS = 3;

    private Handler retryHandler;

    /**
     * Starts this service to print a receipt or cashup
     */
    public static void startActionPrintReceipt(Context context, String readableId, String
            realId, Bitmap image, EpicuriMenu.Printer printer, boolean isSeated) {
        String cacheDir = getCacheDir(context);
        File filename = saveBitmap(image, String.format("receipt_%s", System.currentTimeMillis()), cacheDir, false);

        Intent intent = new Intent(context, PrintBitmapService.class);
        intent.setAction(ACTION_PRINT_RECEIPT);
        intent.putExtra(EXTRA_READABLE_SESSION_ID, readableId);
        intent.putExtra(EXTRA_REAL_SESSION_ID, realId);
        try {
            intent.putExtra(EXTRA_IMAGE_FILENAME, filename.toString());
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        intent.putExtra(EXTRA_PRINTER, (Parcelable) printer);
        intent.putExtra(EXTRA_IS_SEATED, isSeated);

        context.startService(intent);

        Toast.makeText(context, "Printing receipt...", Toast.LENGTH_SHORT).show();
    }

    public static File saveBitmap(Bitmap bitmap, String filename, String path, boolean recycle) {
        FileOutputStream out=null;
        try {
            File f = File.createTempFile(filename, ".png", new File(path));
            if(!f.exists()) {
                f.createNewFile();
            }
            Log.d("ReceiptPrint", "Writing file to " + f.getAbsolutePath());
            out = new FileOutputStream(f);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                return f;
            }
            throw new Exception("Could not save bitmap - bitmap.compress is false");
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.e("ReceiptPrint", "Could not save bitmap", e);
        } finally {
            try{
                out.close();
            } catch(Throwable ignore) {}
            if(recycle) {
                bitmap.recycle();
            }
        }
        return null;
    }

    /**
     * Starts this service to print a receipt
     */
    public static void startActionPrintCashup(Context context, Bitmap image, EpicuriMenu.Printer printer) {
        String cacheDir = getCacheDir(context);
        File filename = saveBitmap(image, String.format("cashup_%s", System.currentTimeMillis()), cacheDir, false);

        Intent intent = new Intent(context, PrintBitmapService.class);
        intent.setAction(ACTION_PRINT_CASHUP);
        intent.putExtra(EXTRA_IMAGE_FILENAME, filename.toString());
        intent.putExtra(EXTRA_PRINTER,(Parcelable)  printer);
        context.startService(intent);

        Toast.makeText(context, "Queued for printing. Check notifications area for errors", Toast.LENGTH_SHORT).show();
    }

    public PrintBitmapService() {
        super("PrintReceiptService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        retryHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PRINT_RECEIPT.equals(action)) {
	            //final int sessionId = intent.getIntExtra(EXTRA_SESSION_ID, 0);
                final String readableId = intent.getStringExtra(EXTRA_READABLE_SESSION_ID);
                final String realId = intent.getStringExtra(EXTRA_REAL_SESSION_ID);
	            final String receiptImageFilename = intent.getStringExtra(EXTRA_IMAGE_FILENAME);
	            final EpicuriMenu.Printer printer = intent.getParcelableExtra(EXTRA_PRINTER);
	            final Boolean isSeated = intent.getBooleanExtra(EXTRA_IS_SEATED, true);
                if(null == printer) {
                    Log.e("Print","Printer is null");
                } else if (printer.isPhysical()) {
		            handleActionPrintReceipt(readableId, realId, isSeated, receiptImageFilename,
                            printer, intent.getIntExtra(EXTRA_ATTEMPT, 1));
	            } else {
		            Log.e("Print","Trying to print to logical printer: " + printer.getId());
	            }
            }
            else if (ACTION_PRINT_CASHUP.equals(action)) {
                final String imageFilename = intent.getStringExtra(EXTRA_IMAGE_FILENAME);
                final EpicuriMenu.Printer printer = intent.getParcelableExtra(EXTRA_PRINTER);
                if(null == printer) {
                    Log.e("Print", "Printer is null");
                } else if(printer.isPhysical()) {
		            handleActionPrintCashup(imageFilename, printer, intent.getIntExtra(EXTRA_ATTEMPT, 1));
	            } else {
		            Log.e("Print","Trying to print to logical printer: " + printer.getId());
	            }
            }
        }
    }

    private void handleActionPrintReceipt(String readableId, String realId, boolean isSeated, String
            receiptImageFilename, EpicuriMenu.Printer printer, int attempt) {
        Log.e("PrintBitmap", "Trying to printing receipt, attempt " + attempt);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap receiptImage = null;
        try {
            receiptImage = BitmapFactory.decodeFile(receiptImageFilename, options);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        startForeground(GlobalSettings.NOTIFICATION_RECEIPT_PRINT, generateReceiptNotification(readableId));
        BitmapPrint bp = new BitmapPrint(String.format("TCP:%s", printer.getIpAddress()), "", printer.getId(), printer.getMacAddress());
        boolean success = false;
        Exception caughtException = null;
        String error;
        try {
            success = bp.print(receiptImage, this);
            error = bp.getError();
        } catch (StarIOPortException e) {
            caughtException = e;
            error = e.getMessage();
        }
        if(!success) {
            if(attempt < MAX_ATTEMPTS) {
                Log.e("PrintBitmap", "Printing receipt failed, trying again in 5 seconds");
                final Intent intent = new Intent(PrintBitmapService.this, PrintBitmapService.class);
                intent.setAction(ACTION_PRINT_RECEIPT);
                intent.putExtra(EXTRA_READABLE_SESSION_ID, readableId);
                intent.putExtra(EXTRA_REAL_SESSION_ID, realId);
                intent.putExtra(EXTRA_IMAGE_FILENAME, receiptImageFilename);
                intent.putExtra(EXTRA_PRINTER, (Parcelable) printer);
                intent.putExtra(EXTRA_ATTEMPT, attempt + 1);
                intent.putExtra(EXTRA_IS_SEATED, isSeated);

                // queue up a retry
                retryHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService(intent);
                    }
                }, 5000); // 5 seconds delay


            } else {
                stopForeground(true);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(GlobalSettings.NOTIFICATION_RECEIPT_PRINT_FAILED,
                        generateReceiptFailureNotification(readableId, realId, isSeated, bp.getError()));
                File f = null;
                try {
                    f = new File(receiptImageFilename);
                    if (f.exists()) f.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        } else {
            stopForeground(true);
            File f = null;
            try {
                f = new File(receiptImageFilename);
                if (f.exists()) f.delete();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    private void handleActionPrintCashup(String imageFilename, EpicuriMenu.Printer printer, int attempt) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap image = null;
        try {
            image = BitmapFactory.decodeFile(imageFilename, options);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        startForeground(GlobalSettings.NOTIFICATION_CASHUP_PRINT, generateCashupNotification());
        BitmapPrint bp = new BitmapPrint(String.format("TCP:%s", printer.getIpAddress()), "", printer.getId(), printer.getMacAddress());
        boolean success = false;
        String error;
        Exception caughtException = null;
        try {
            success = bp.print(image, this);
            error = bp.getError();
        } catch (StarIOPortException e) {
            caughtException = e;
            error = e.getMessage();
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        if(!success) {
            if(attempt < MAX_ATTEMPTS) {
                Log.e("PrintBitmap", "Printing cashup failed, trying again in 5 seconds");

                final Intent intent = new Intent(PrintBitmapService.this, PrintBitmapService.class);
                intent.setAction(ACTION_PRINT_CASHUP);
                intent.putExtra(EXTRA_IMAGE_FILENAME, imageFilename);
                intent.putExtra(EXTRA_PRINTER, (Parcelable) printer);
                intent.putExtra(EXTRA_ATTEMPT, attempt + 1);
                // queue up a retry
                retryHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService(intent);
                    }
                }, 5000); // 5 seconds delay

            } else {
                stopForeground(true);
//                Crashlytics.log("Print Job failed, max retries exhausted: " + error);
//                if(null != caughtException) Crashlytics.logException(caughtException);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(GlobalSettings.NOTIFICATION_CASHUP_PRINT_FAILED, generateCashupFailureNotification(bp.getError()));
                File f = new File(imageFilename);
                if(f.exists()) f.delete();
            }
        } else {
            stopForeground(true);
            File f = new File(imageFilename);
            if(f.exists()) f.delete();
        }
    }

    public Notification generateReceiptNotification(String readableId){
        return new NotificationCompat.Builder(this, GlobalSettings.CHANNEL_PRINT)
                .setSmallIcon(R.drawable.notify_print)
                .setContentTitle("Printing Receipt")
                .setContentText(String.format("Printing receipt for session %s", readableId))
                .build();
    }

    public Notification generateCashupNotification(){
        return new NotificationCompat.Builder(this, GlobalSettings.CHANNEL_PRINT)
                .setSmallIcon(R.drawable.notify_print)
                .setContentTitle("Printing Cashup")
                .setContentText("Please wait, printing cashup")
                .build();
    }

    public Notification generateReceiptFailureNotification(String readableId, String realId,
            boolean isSeated, String error){

        Intent resultIntent = isSeated ? new Intent(this, SeatedSessionActivity.class) : new
                Intent(this, TakeawayActivity.class);

        resultIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, realId);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, GlobalSettings.CHANNEL_PRINT)
                .setSmallIcon(R.drawable.notify_print_error)
                .setContentTitle("Receipt print failed")
                .setContentText(String.format("Receipt print failed for session %s, %s.  Click to"
                        + " reopen session.", readableId, error))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build();
    }
    public Notification generateCashupFailureNotification(String error){

        Intent resultIntent = new Intent(this, CashUpActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, GlobalSettings.CHANNEL_PRINT)
                .setSmallIcon(R.drawable.notify_print_error)
                .setContentTitle("Cashup print failed")
                .setContentText(String.format("Receipt cashup failed.\n%s.\nClick to reopen.", error))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .build();
    }


    public static String getCacheDir(Context context) {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||!Environment.isExternalStorageRemovable() ?
                context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
    }


}
