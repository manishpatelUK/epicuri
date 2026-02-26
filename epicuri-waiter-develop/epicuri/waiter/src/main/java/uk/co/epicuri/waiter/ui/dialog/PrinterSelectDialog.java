package uk.co.epicuri.waiter.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.starmicronics.stario.PortInfo;

import java.util.List;

import uk.co.epicuri.waiter.model.EpicuriMenu;

public class PrinterSelectDialog {
    private Context context;
    private OnPrinterSelectedListener listener;
    private List<EpicuriMenu.Printer> printers;
    private PortInfo currentInfo;

    public interface OnPrinterSelectedListener {
        void onPrinterSelected(EpicuriMenu.Printer printer, PortInfo currentInfo);
    }

    public static PrinterSelectDialog newInstance() {
        return new PrinterSelectDialog();
    }

    public void show(Context context, OnPrinterSelectedListener listener, List<EpicuriMenu.Printer> validPrinters, PortInfo currentInfo) {
        this.context = context;
        this.listener = listener;
        this.printers = validPrinters;
        this.currentInfo = currentInfo;
        showDialog();
    }

    private void showDialog() {
        final CharSequence[] printersArray = new CharSequence[printers.size()];
        for (int i = 0; i < printers.size(); i++) {
            printersArray[i] = printers.get(i).getName();
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Which printer is this?");
        dialogBuilder.setSingleChoiceItems(printersArray, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onPrinterSelected(printers.get(which), currentInfo);
                dialog.dismiss();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog d = dialogBuilder.create();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = (int) (width*0.5);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        d.show();
        d.getWindow().setAttributes(lp);
    }

    private Context getContext() {
        return this.context;
    }
}
