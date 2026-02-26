package uk.co.epicuri.waiter.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnCashUpListener;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class AddCashUpDialogFragment extends DialogFragment {

	TextView startDateText;
	TextView startTimeText;
	@InjectView(R.id.endDate)
	TextView endDateText;
	@InjectView(R.id.endTime)
	TextView endTimeText;
	@InjectView(R.id.cannot_cashup)
	TextView cannotCashupText;

	OnCashUpListener listener;

	DateFormat dateFormat;
	DateFormat timeFormat;

	Date latestCashupDate;

	Calendar startDate;
	Calendar endDate;

	private Button positiveButton;
	private Button neutralButton;
	private boolean simulateAllowed = false;
	private boolean cashupAllowed = false;

	public static AddCashUpDialogFragment newInstance(Date earliestCashUp, boolean canCashUp){
		Bundle args = new Bundle(2);
		if(null != earliestCashUp){
			args.putLong(GlobalSettings.ARG_EARLIEST_DATE, earliestCashUp.getTime());
		}
		args.putBoolean(GlobalSettings.ARG_CAN_CASH_UP, canCashUp);
		AddCashUpDialogFragment frag = new AddCashUpDialogFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		startDate = Calendar.getInstance();
		endDate = Calendar.getInstance();

		try {
			LocalSettings localSettings = LocalSettings.getInstance(getContext());
			cashupAllowed = localSettings.isAllowed(WaiterAppFeature.CASH_UP);
			simulateAllowed = localSettings.isAllowed(WaiterAppFeature.CASH_UP_SIMULATION);
		} catch (Exception ex) {
			Log.e("AddCashUpDialog", "Error trying to set booleans", ex);
		}

		if(getArguments().containsKey(GlobalSettings.ARG_EARLIEST_DATE)){
			latestCashupDate = new Date(getArguments().getLong(GlobalSettings.ARG_EARLIEST_DATE));
			startDate.setTime(latestCashupDate);
		} else {
			startDate.add(Calendar.DAY_OF_YEAR, -1);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (OnCashUpListener) context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_addcashup, null, false);
		ButterKnife.inject(this, view);

		final boolean canCashUp = getArguments().getBoolean(GlobalSettings.ARG_CAN_CASH_UP);
		if(canCashUp){
			cannotCashupText.setVisibility(View.GONE);
		}

		// if there is no "latest cashup date" then the start date is editable
		if(null == latestCashupDate){
			View startDateRow = view.findViewById(R.id.editableStartDate);
			startDateRow.setVisibility(View.VISIBLE);
			startDateText = (TextView) startDateRow.findViewById(R.id.startDateEdit);
			startTimeText = (TextView) startDateRow.findViewById(R.id.startTimeEdit);
			startDateText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickDate(v);
				}
			});
			startTimeText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickTime(v);
				}
			});
		} else {
			View startDateRow = view.findViewById(R.id.staticStartDate);
			startDateRow.setVisibility(View.VISIBLE);
			startDateText = (TextView) startDateRow.findViewById(R.id.startDate);
			startTimeText = (TextView) startDateRow.findViewById(R.id.startTime);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.create_new_cash_up))
				.setView(view)
				.setNeutralButton(getString(R.string.simulate), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						addCashUp(true);
					}
				})
				.setNegativeButton(getString(R.string.cancel),null);


        builder.setPositiveButton(getString(R.string.cash_up),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(canCashUp) {
                            addCashUp(false);
                        } else {
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Cannot perform Cash-up ")
                                    .setMessage(Html.fromHtml("Cashup cannot be performed while sessions are active. " +
                                            "Go back to the hub to close all active sessions or use 'End Service'.<br><br>" +
                                            "<b>Please retry cashup after all active sessions are closed</b>"))
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("End Service", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            listener.showEndOfDay();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    }
                }
        );

		return builder.create();
	}

	@Override
	public void onResume() {
		super.onResume();
		positiveButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
		neutralButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL);
		updateUi();
	}

	void updateUi(){
		// set values for UI
		if(null != startDate) {
			startDateText.setText(dateFormat.format(startDate.getTime()));
			startTimeText.setText(timeFormat.format(startDate.getTime()));
		}
		if(null != endDate) {
			endDateText.setText(dateFormat.format(endDate.getTime()));
			endTimeText.setText(timeFormat.format(endDate.getTime()));
		}
		positiveButton.setEnabled(endDate.after(startDate) && simulateAllowed && cashupAllowed);
		neutralButton.setEnabled(simulateAllowed);
	}

	@OnClick({R.id.endDate})
	void pickDate(View v){
		final Calendar theDate;
		if(v == endDateText) {
			theDate = endDate;
		} else if(v == startDateText) {
			theDate = startDate;
		} else {
			throw new IllegalArgumentException("Only end date supported");
		}

		new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				theDate.set(Calendar.YEAR, year);
				theDate.set(Calendar.MONTH, monthOfYear);
				theDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateUi();
			}
		}, theDate.get(Calendar.YEAR), theDate.get(Calendar.MONTH), theDate.get(Calendar.DAY_OF_MONTH)).show();
	}

	@OnClick({R.id.endTime})
	void pickTime(View v){
		final Calendar theDate;
		if(v == endTimeText) {
			theDate = endDate;
		} else if(v == startTimeText) {
			theDate = startDate;
		} else {
			throw new IllegalArgumentException("Only end time supported");
		}

		new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener () {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				theDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
				theDate.set(Calendar.MINUTE, minute);
				updateUi();
			}
		}, theDate.get(Calendar.HOUR_OF_DAY), theDate.get(Calendar.MINUTE), true).show();
	}

	private void addCashUp(boolean simulate) {
		listener.onCashUp((null == latestCashupDate ? startDate.getTime() : null), endDate.getTime(), simulate, getArguments().getBoolean(GlobalSettings.ARG_CAN_CASH_UP));
	}

}
