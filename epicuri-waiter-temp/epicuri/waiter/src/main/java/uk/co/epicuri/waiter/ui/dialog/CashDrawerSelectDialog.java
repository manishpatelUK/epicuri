package uk.co.epicuri.waiter.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.printing.PrintUtil;
import uk.co.epicuri.waiter.printing.PrinterType;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class CashDrawerSelectDialog {
    private List<EpicuriMenu.Printer> printers;
    private Context context;
    private OnDrawerKicked listener;
    private static int lastSelected = 0;
    public interface OnDrawerKicked{
        void onDrawerKicked(boolean success);
    }

    public static CashDrawerSelectDialog newInstance() {
        return new CashDrawerSelectDialog();
    }

    private void onSelection(EpicuriMenu.Printer printer) {
        PrintUtil.kickDrawer(getContext(), printer);
        if(listener != null) listener.onDrawerKicked(true);
    }

    private Context getContext() {
        return this.context;
    }

    public void show(Context context, OnDrawerKicked listener, List<EpicuriMenu.Printer> validPrinters) {
        this.context = context;
        this.listener = listener;
        this.printers = validPrinters;
        showDialog();
    }

    private void showDialog() {
        try {
            final CharSequence[] printersArray = new CharSequence[printers.size()];
            for (int i = 0; i < printers.size(); i++) {
                printersArray[i] = printers.get(i).getName();
            }
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle("Please select which drawer to kick");
            dialogBuilder.setSingleChoiceItems(printersArray, lastSelected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    lastSelected = which;
                }
            })
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        onSelection(printers.get(lastSelected));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        dialog.dismiss();
                    }
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
        }catch (Exception e){
            //Activity is probably finished
        }

    }
}
