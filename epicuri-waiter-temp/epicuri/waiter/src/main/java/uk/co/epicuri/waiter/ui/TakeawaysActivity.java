package uk.co.epicuri.waiter.ui;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import java.util.Calendar;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.TakeawaysListListener;


public class TakeawaysActivity extends EpicuriBaseActivity implements TakeawaysListListener {

    private static final String FRAGMENT_TAKEAWAYS_LIST = "takeawaysList";
	private static final int REQUEST_NEW_TAKEAWAY = 1;

	public static final String EXTRA_DATE = "uk.co.epicuri.DATE";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.simple_frame);
        
        if(null == savedInstanceState){
			TakeawaysListFragment frag = new TakeawaysListFragment();
			if(null != getIntent().getExtras() && getIntent().getExtras().containsKey(EXTRA_DATE)){
				Bundle args = new Bundle();
				args.putLong(EXTRA_DATE, getIntent().getExtras().getLong(EXTRA_DATE));
				frag.setArguments(args);
			}
			getSupportFragmentManager().beginTransaction()
					.add(R.id.content_frame, frag, FRAGMENT_TAKEAWAYS_LIST)
					.commit();
        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(null != intent.getExtras() && intent.getExtras().containsKey(EXTRA_DATE)){
			TakeawaysListFragment frag = (TakeawaysListFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAKEAWAYS_LIST);
			if(null != frag){
				Calendar date = Calendar.getInstance();
				date.setTimeInMillis(intent.getExtras().getLong(EXTRA_DATE));
				frag.jumpTo(date);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home: {
			FragmentManager fm = getSupportFragmentManager();
			if(fm.getBackStackEntryCount() > 0){
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
	public void addTakeaway(Calendar date) {
		Intent newTakeaway = new Intent(this, TakeawayActivity.class);
		newTakeaway.putExtra(TakeawayActivity.EXTRA_DATE, date);
		startActivityForResult(newTakeaway, REQUEST_NEW_TAKEAWAY);
	}
	
	@Override
	public void showTakeaway(String sessionId, boolean autoShowItems) {
		Intent viewTakeaway = new Intent(this, TakeawayActivity.class);
		viewTakeaway.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
		viewTakeaway.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
		startActivity(viewTakeaway);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_NEW_TAKEAWAY: {
			// TODO: jump to specified day
			return;
		}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
