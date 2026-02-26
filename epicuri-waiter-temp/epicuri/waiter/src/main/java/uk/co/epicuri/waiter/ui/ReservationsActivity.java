package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Calendar;
import java.util.Date;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.AddReservationListener;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class ReservationsActivity extends EpicuriBaseActivity implements AddReservationListener {

    private static final String FRAGMENT_EDIT_RESERVATION = "editReservation";
    private static final String FRAGMENT_RESERVATION_LIST = "reservationList";
	private static final int REQUEST_RESERVATION = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.simple_frame);
        
        if(null == savedInstanceState){
        	getSupportFragmentManager().beginTransaction().add(R.id.content_frame, new ReservationsListFragment(), FRAGMENT_RESERVATION_LIST).commit();
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home: {
			if(checkChanges()){
				return true;
			}
			FragmentManager fm = getSupportFragmentManager();
			if(fm.getBackStackEntryCount() > 0){
				View focus = getCurrentFocus();
				if(null != focus){
					InputMethodManager imm = (InputMethodManager) focus.getContext().getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
				}
				fm.popBackStack();
				return true;
			}
			Intent intent = new Intent(this, HubActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void addReservation(Calendar day) {
		final Intent addReservationIntent = new Intent(this, ReservationEditActivity.class);
		addReservationIntent.setAction(ReservationEditActivity.ACTION_CREATE);
		addReservationIntent.putExtra(ReservationEditActivity.EXTRA_DATE, day);
		startActivityForResult(addReservationIntent, REQUEST_RESERVATION);
	}
	@Override
	public void editReservation(EpicuriReservation reservation) {
		final Intent editReservationIntent = new Intent(this, ReservationEditActivity.class);
		editReservationIntent.setAction(ReservationEditActivity.ACTION_EDIT);
		editReservationIntent.putExtra(GlobalSettings.EXTRA_RESERVATION_ID, reservation.getId());
		startActivityForResult(editReservationIntent, REQUEST_RESERVATION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_RESERVATION){
			if(null != data && data.hasExtra(ReservationEditActivity.EXTRA_DATE)){
				Date d = (Date) data.getSerializableExtra(ReservationEditActivity.EXTRA_DATE);
				ReservationsListFragment lf = (ReservationsListFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_RESERVATION_LIST);
				if(null != lf){
					lf.setDate(d);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean checkChanges(){
		FragmentManager fm = getSupportFragmentManager();
		ReservationEditFragment frag = (ReservationEditFragment)fm.findFragmentByTag(FRAGMENT_EDIT_RESERVATION);
		if(null != frag && frag.isModified()){
			new AlertDialog.Builder(this)
				.setTitle("Booking has been modified")
				.setMessage("Abandon booking?")
				.setPositiveButton("Abandon", new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ReservationsActivity.super.onBackPressed();
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
			return true;
		} else {
			return false;
		}

	}
	
	@Override
	public void onBackPressed() {
		if(checkChanges()){
			return;
		} else {
			super.onBackPressed();
		}
	}
}
