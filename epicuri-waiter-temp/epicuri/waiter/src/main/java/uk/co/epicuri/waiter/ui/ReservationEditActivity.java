package uk.co.epicuri.waiter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.Date;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CloseReservationListener;

public class ReservationEditActivity extends EpicuriBaseActivity implements CloseReservationListener {
	private static final String FRAGMENT_EDIT = "edit";

	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_CREATE = "create";

	public static final String EXTRA_DATE = "date";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reservationedit);
		if(null == savedInstanceState){
			ReservationEditFragment frag;
			final String action = getIntent().getAction();
			if(ACTION_CREATE.equals(action)){
				final Calendar day = (Calendar) getIntent().getExtras().getSerializable(EXTRA_DATE);
				frag = ReservationEditFragment.onDate(day);
			} else if(ACTION_EDIT.equals(action)) {
				final String reservationId = getIntent().getExtras().getString(GlobalSettings.EXTRA_RESERVATION_ID);
				frag = ReservationEditFragment.forReservationId(reservationId);
			} else {
				throw new IllegalArgumentException("Must be edit or create");
			}
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag, FRAGMENT_EDIT).commit();
		}
	}

	@Override
	public void closeReservationEdit() {
		finish();
	}

	@Override
	public void closeReservationEdit(Date d) {
		Intent i = getIntent();
		i.putExtra(EXTRA_DATE, d);
		setResult(RESULT_OK, i);
		finish();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home: {
				Intent intent = new Intent(this, ReservationsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

}
