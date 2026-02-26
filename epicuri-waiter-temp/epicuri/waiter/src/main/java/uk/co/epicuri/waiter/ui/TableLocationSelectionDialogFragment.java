package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.ILocationListener;

public class TableLocationSelectionDialogFragment extends DialogFragment {
    @InjectView(R.id.locationText)
    EditText locationText;
    private ILocationListener listener;

    private static final String LOCATION_ARG = "locationArg";
    public static final String DEFAULT_LOCATION = "N/A";
    private static final DateFormat dateFormat = new SimpleDateFormat("mmss", Locale.UK);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_location_selection, null,false);
        ButterKnife.inject(this, view);
        locationText.setTransformationMethod(null);
        locationText.setText(getDateTimeString());

        return new AlertDialog.Builder(getActivity())
                .setTitle("Enter Table/Location/Identifier")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOKButtonClick();
                    }
                })
                .setView(view)
                .create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ILocationListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static TableLocationSelectionDialogFragment newInstance(String initial) {
        if(initial == null) {
            initial = "";
        }

        TableLocationSelectionDialogFragment tableLocationSelectionDialogFragment = new TableLocationSelectionDialogFragment();

        Bundle args = new Bundle();
        args.putString(LOCATION_ARG, initial);
        tableLocationSelectionDialogFragment.setArguments(args);
        return tableLocationSelectionDialogFragment;
    }

    public void onOKButtonClick() {
        if(listener != null) {
            String location = locationText.getText().toString().trim();
            if(location.length() == 0) {
                location = getDateTimeString();
            }
            listener.onLocation(location);
        }
    }

    private String getDateTimeString() {
        try {
            Date currentTime = Calendar.getInstance().getTime();
            return dateFormat.format(currentTime);
        } catch (Exception ex) {
            return DEFAULT_LOCATION;
        }
    }
}
