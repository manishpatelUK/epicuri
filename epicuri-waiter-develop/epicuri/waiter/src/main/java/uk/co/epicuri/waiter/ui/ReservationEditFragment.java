package uk.co.epicuri.waiter.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CloseReservationListener;
import uk.co.epicuri.waiter.interfaces.CustomerListener;
import uk.co.epicuri.waiter.interfaces.OnTimeSetListener;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.ReservationsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class ReservationEditFragment extends Fragment implements CustomerListener, LoaderManager.LoaderCallbacks<ArrayList<EpicuriReservation>> {
	private static final String EXTRA_DATE = "uk.co.epicuri.waiter.DATE";

	private static final int LOADER_RESERVATION  = 1;
	
	private TextView customerText;
	private Button customerLookup;
	private EditText partyName;
	private EditText phoneNumber;
	private EditText emailAddress;
	private EditText notes;
	private NumberPicker numberInParty; 
	private Button resDate;
	private Button resTime;
    private AppCompatCheckBox omitCheck;

	@InjectView(R.id.text1)
	EditText rejectMessage;

	private ProgressDialog pd;
	
	private TextView pendingReason_title;
	private TextView pendingReason_text;
	
	private boolean modified = false;
	
	private Calendar reservationTime; 
	
	private EpicuriReservation reservation;
	private EpicuriCustomer newCustomer;

    public static ReservationEditFragment onDate(Calendar cal){
		ReservationEditFragment frag = new ReservationEditFragment();
		Bundle args = new Bundle();
		args.putLong(EXTRA_DATE, cal.getTime().getTime());
		frag.setArguments(args);
		return frag;
	}

	public static ReservationEditFragment forReservationId(String reservationId) {
		ReservationEditFragment frag = new ReservationEditFragment();
		Bundle args = new Bundle();
		args.putString(GlobalSettings.EXTRA_RESERVATION_ID, reservationId);
		frag.setArguments(args);
		return frag;
	}

//	public static ReservationEditFragment withReservation(EpicuriReservation reservation){
//		ReservationEditFragment frag = new ReservationEditFragment();
//		Bundle args = new Bundle();
//		args.putParcelable(GlobalSettings.EXTRA_RESERVATION, reservation);
//		frag.setArguments(args);
//		return frag;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(null != getArguments()){
			if(getArguments().containsKey(GlobalSettings.EXTRA_RESERVATION_ID)){
				getLoaderManager().initLoader(LOADER_RESERVATION, getArguments(), ReservationEditFragment.this);
			} else if(getArguments().containsKey(GlobalSettings.EXTRA_RESERVATION)){
				reservation = getArguments().getParcelable(GlobalSettings.EXTRA_RESERVATION);
			} else {
				// assume it's a new reservation
			}
		}
		reservationTime = Calendar.getInstance();
		reservationTime.set(Calendar.MILLISECOND, 0);
		reservationTime.set(Calendar.SECOND, 0);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_reservation, container, false);

		TextWatcher watcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				validate();
			}
		};
		
		partyName = (EditText) v.findViewById(R.id.name_edit);
		phoneNumber = (EditText) v.findViewById(R.id.phonenumber_edit);
		emailAddress = (EditText) v.findViewById(R.id.email_edit);
		notes = (EditText)v.findViewById(R.id.notes_edit);
		numberInParty = (NumberPicker) v.findViewById(R.id.numberInParty_picker);
		resDate = (Button)v.findViewById(R.id.resdate_button);
		resTime = (Button)v.findViewById(R.id.restime_button);
		customerText = (TextView)v.findViewById(R.id.customer_edit);
		customerLookup = (Button)v.findViewById(R.id.customerlookup_button);
		
		pendingReason_text = (TextView)v.findViewById(R.id.pendingreason_text);
		pendingReason_title = (TextView)v.findViewById(R.id.pendingreason_title);
		omitCheck = v.findViewById(R.id.omitCheck);
		resDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDatePicker();
			}
		});
		resTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTimePicker();
			}
		});


		numberInParty.setMaxValue(50);
		numberInParty.setMinValue(1);

		if(null != reservation){
			updateUi(reservation);
			
		} else if(getArguments() != null && getArguments().containsKey(EXTRA_DATE)){
			Calendar c = Calendar.getInstance();
			
			// try to add on the minimum delay for takeaways
			EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
			String defaultWindow = restaurant.getRestaurantDefault(EpicuriRestaurant.DEFAULT_RESERVATIONMINIMUMTIME);
			if(null != defaultWindow){
				int delay = Integer.parseInt(defaultWindow);
				c.add(Calendar.MINUTE, delay);
			}

			Date requestedDate = new Date(getArguments().getLong(EXTRA_DATE));
			if(c.getTime().before(requestedDate)){
				// requested date is not within bounds so use that
				c.setTime(requestedDate);
			}

			c.add(Calendar.MINUTE, 10 - c.get(Calendar.MINUTE) % 10);
			reservationTime.setTime(c.getTime());
			updateTimeDisplay();
		}

		// TODO: cannot be in the past
//		resDate.setMinDate(new Date().getTime());
		
		customerLookup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EpicuriCustomerLookup frag = new EpicuriCustomerLookup();
				Bundle args = new Bundle();
				args.putString(GlobalSettings.EXTRA_TARGET_FRAGMENT, ReservationEditFragment.this.getTag());
				frag.setArguments(args);
				frag.show(getFragmentManager(), null);
			}
		});
		
		partyName.addTextChangedListener(watcher);
		phoneNumber.addTextChangedListener(watcher);
		numberInParty.setWrapSelectorWheel(false);
		numberInParty.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				validate();	
			}
		});
				
		return v;
	}

	private void updateUi(EpicuriReservation res){
		if(null != reservation.getEpicuriUser()){
			newCustomer = reservation.getEpicuriUser();
			customerText.setText(reservation.getEpicuriUser().getName());
		}
		partyName.setText(reservation.getName());
		phoneNumber.setText(reservation.getPhoneNumber());
		emailAddress.setText(reservation.getEmail());
		notes.setText(reservation.getNotes());
		numberInParty.setValue(reservation.getNumberInParty());
		omitCheck.setChecked(reservation.isOmitFromChecks());

		if(!reservation.isDeleted() && !reservation.isAccepted() && null != reservation.getRejectedReason()){
			pendingReason_title.setVisibility(View.VISIBLE);
			pendingReason_text.setVisibility(View.VISIBLE);
			pendingReason_text.setText(reservation.getRejectedReason());
		}

		reservationTime.setTime(reservation.getStartDate());

		updateTimeDisplay();
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_reservationedit, menu);
		menu.findItem(R.id.menu_save).setTitle(reservation == null ? "Create" : (reservation.isAccepted() ? "Save" : "Save and accept"));
		menu.findItem(R.id.menu_reject).setVisible(reservation != null && !reservation.isAccepted());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(tapped) return true;
		switch(item.getItemId()){
		case R.id.menu_save: {
			validationFired = true;
			if(validate()){
				if(null != reservation && !reservation.isAccepted()){
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.acceptReservation_title)
					
					.setMessage(getString(R.string.acceptReservation_message, reservation.getName(), LocalSettings.getDateFormatWithDate().format(reservation.getStartDate())))
					.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							validateOnlineAndSubmit();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();

				} else {
					validateOnlineAndSubmit();
				}
			} else {
				Toast.makeText(getActivity(), "Please correct errors then submit again", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		case R.id.menu_reject: {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edittext, null, false);
			ButterKnife.inject(this, view);
			rejectMessage.setSelectAllOnFocus(true);
			rejectMessage.setHint("Message to send guest about rejection");
			rejectMessage.setText(reservation.getRejectedReason());
			final String rejectId = reservation.getId();
			new AlertDialog.Builder(getActivity()).setTitle("Reject reservation with message")
				.setView(view)
				.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						rejectReservation(rejectId, rejectMessage.getText());	
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
			return true;
		}
//		case R.id.menu_cancel: {
//		((Listener)getActivity()).closeReservationEdit();
//			return true;
//		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showDatePicker(){
		DatePickerDialog d = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				reservationTime.set(Calendar.YEAR, year);
				reservationTime.set(Calendar.MONTH, monthOfYear);
				reservationTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateTimeDisplay();
				validate();
			}
		}, reservationTime.get(Calendar.YEAR), reservationTime.get(Calendar.MONTH), reservationTime.get(Calendar.DAY_OF_MONTH));
		d.show();
	}

	private void showTimePicker(){
		TenMinuteTimePickerDialog d = new TenMinuteTimePickerDialog(getActivity(), new OnTimeSetListener() {

			@Override
			public void onTimeSet(TenMinuteTimePicker view, int hourOfDay,
					int minute) {
				reservationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				reservationTime.set(Calendar.MINUTE, minute);
				updateTimeDisplay();
				validate();
			}
		}, reservationTime.get(Calendar.HOUR_OF_DAY), reservationTime.get(Calendar.MINUTE), true);
		d.show();
	}
	
	public boolean isModified(){
		return modified;
	}

	private void rollDateToNow(){
		reservationTime = Calendar.getInstance();
		reservationTime.set(Calendar.MILLISECOND, 0);
		reservationTime.set(Calendar.SECOND, 0);
		reservationTime.add(Calendar.MINUTE, 10 - reservationTime.get(Calendar.MINUTE) % 10);
		updateTimeDisplay();
	}
	
	private void updateTimeDisplay(){
		resDate.setText(new SimpleDateFormat("E dd-MMM-yyyy", Locale.UK).format(reservationTime.getTime()));
		resTime.setText(new SimpleDateFormat("HH:mm", Locale.UK).format(reservationTime.getTime()));
	}

	private boolean validationFired = false;
	private boolean validate(){
		modified = true;
		if(!validationFired) return false;
		boolean valid = true;
		if(partyName.getText().length() == 0){
			partyName.setError("Cannot be blank");
			valid = false;
		} else {
			partyName.setError(null);
		}
		if(phoneNumber.getText().length() == 0){
			phoneNumber.setError("Cannot be blank");
			valid = false;
		} else {
			phoneNumber.setError(null);
		}
		if(numberInParty.getValue() <= 0){
			valid = false;
		}
		if(reservationTime.before(Calendar.getInstance())){
			Toast.makeText(getActivity(), "Date was in the past and has been reset to the current time", Toast.LENGTH_SHORT).show();
			rollDateToNow();
			valid = false;
		}

		return valid;
	}

	@Override
	public void setCustomer(final EpicuriCustomer customer) {
		if(null != customer){
			
			if(partyName.getText().length() > 0
					|| phoneNumber.getText().length() > 0){
				new AlertDialog.Builder(getActivity())
				.setTitle("Overwrite values")
				.setMessage("This will overwrite values")
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setCustomerFields(customer, true);
					}
				})
				.setNeutralButton("Keep", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setCustomerFields(customer, false);
					}
				})
				.show();
			} else {
				setCustomerFields(customer, true);
			}
		} else {
			newCustomer = null;
			customerText.setText("");
		}
	}

	public void setCustomerFields(EpicuriCustomer customer, boolean override) {
		newCustomer = customer;
		customerText.setText(customer.getName());
		if (override) {
			partyName.setText(customer.getName());
			phoneNumber.setText(customer.getPhoneNumber());
			emailAddress.setText(customer.getEmail());
		}
	}
	private boolean tapped;
	private void checkReservation(String id, CharSequence partyName,
			CharSequence phoneNumber, int numberInParty, Date date,
			CharSequence notes, EpicuriCustomer customer) {
		WebServiceCall call;
		if(id == null || id.equals("-1")){
			call = new CreateEditReservationWebServiceCall(id, partyName, phoneNumber, numberInParty, date, notes, customer, false, false);
		} else {
			call = new CreateEditReservationWebServiceCall(partyName, phoneNumber, numberInParty, date, notes, customer, false, false);
		}
		tapped = true;
		WebServiceTask task = new WebServiceTask(getActivity(), call, true);
		task.setIndicatorText("Checking reservation");
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
			    tapped = false;
				try{
					JSONObject responseJson = new JSONObject(response);
					JSONArray warnings = responseJson.getJSONArray("Warning");
					if(warnings.length() == 0){
						submit();
					} else {
						StringBuilder sb = new StringBuilder(getString(R.string.submitWarnings));
						for(int i=0; i<warnings.length(); i++){
							sb.append("\n * ").append(warnings.getString(i));
						}
						new AlertDialog.Builder(getActivity())
								.setTitle("Warnings")
								.setMessage(sb)
								.setPositiveButton("Submit anyway",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick( DialogInterface dialog, int which) {
												submit();
											}
										})
								.setNegativeButton("Go Back", null)
								.show();
					}
				} catch (JSONException e){
					new AlertDialog.Builder(getActivity())
							.setTitle("Submit Error")
							.setMessage("An error occurred parsing response)")
							.show();
							
				}
			}
		});
		task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override public void onError(int code, String response) {
                tapped = false;
            }
        });

		task.execute();
	}
	
	private void validateOnlineAndSubmit(){
	    if (omitCheck.isChecked()){
	        submit();
	        return;
        }

		String id = null == reservation ? "-1" : reservation.getId();
		checkReservation(id, partyName.getText(), phoneNumber.getText(), numberInParty.getValue(), reservationTime.getTime(), notes.getText(), newCustomer);
	}
	
	private void submit(){
		if(null == reservation){
			createReservation(partyName.getText(), phoneNumber.getText(), numberInParty.getValue(), reservationTime.getTime(), notes.getText(), newCustomer, emailAddress.getText());
		} else {
			updateReservation(reservation.getId(), partyName.getText(), phoneNumber.getText(), numberInParty.getValue(), reservationTime.getTime(), notes.getText(), newCustomer);
		}
	}
	
	private void rejectReservation(String reservationId, CharSequence message){
		RejectReservationWebServiceCall call = new RejectReservationWebServiceCall(reservationId, message);
		WebServiceTask task = new WebServiceTask(getActivity(), call);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				((CloseReservationListener)getActivity()).closeReservationEdit();

			}
		});
		task.execute();
	}
	
	private void createReservation(CharSequence partyName,
			CharSequence phoneNumber, int numberInParty, final Date date,
			CharSequence notes, EpicuriCustomer customer, CharSequence emailAddress) {
		WebServiceCall call = new CreateEditReservationWebServiceCall(partyName, phoneNumber, numberInParty, date, notes, customer, true, omitCheck.isChecked(), emailAddress);
		WebServiceTask task = new WebServiceTask(getActivity(), call, true);
		task.setIndicatorText("Creating reservation");
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				((CloseReservationListener)getActivity()).closeReservationEdit(date);
			}
		});

		task.execute();
	}

	private void updateReservation(String id, CharSequence partyName,
			CharSequence phoneNumber, int numberInParty, final Date date,
			CharSequence notes, EpicuriCustomer customer) {
		WebServiceCall call = new CreateEditReservationWebServiceCall(id, partyName, phoneNumber, numberInParty, date, notes, customer, true, omitCheck.isChecked());
		WebServiceTask task = new WebServiceTask(getActivity(), call, true);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
			    if(getActivity() != null)
				((CloseReservationListener)getActivity()).closeReservationEdit(date);
			}
		});

		task.execute();
	}

	@Override
	public Loader<ArrayList<EpicuriReservation>> onCreateLoader(int id, Bundle args) {
		pd = ProgressDialog.show(getActivity(), "Loading Reservation", "Please wait...");
		return new OneOffLoader<ArrayList<EpicuriReservation>>(getActivity(), new ReservationsLoaderTemplate(args.getString(GlobalSettings.EXTRA_RESERVATION_ID)));
	}


	@Override
	public void onLoadFinished(Loader<ArrayList<EpicuriReservation>> loader,
							   ArrayList<EpicuriReservation> data) {
        if(null != pd){
            pd.dismiss();
        }

        if(null == data || data.isEmpty()){
            new AlertDialog.Builder(getActivity()).setTitle("Cannot load reservation").show();
            return;
        }

        reservation = data.get(0);
		updateUi(reservation);
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<EpicuriReservation>> loader) {
	}
}
