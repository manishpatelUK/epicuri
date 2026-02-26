package uk.co.epicuri.waiter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.OnDinerClickListener;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Course;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;
import uk.co.epicuri.waiter.printing.PrintDirectlyService;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.GetSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.SubmitOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class SubmitOrderActivity extends EpicuriBaseActivity implements SessionContainer {
	public static final String EXTRA_PENDING_ORDERS = "uk.co.epicuri.waiter.PendingOrders";

	private static final String FRAGMENT_DINER = "diner";
	private static final String FRAGMENT_PENDING_ORDER = "pendingOrder";

	private EpicuriSessionDetail session;
	private ArrayList<Course> courses;
	private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;

	private DinerChooserFragment dinerChooserFragment;
	private Diner selectedDiner;
	private PendingOrderFragment pendingOrderFragment;
	private boolean isSubmitOrderClicked = false;

    MenuItem asapMenuItem;
    MenuItem defaultMenuItem;
    boolean isASAP;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle extras = getIntent().getExtras();

		selectedDiner = extras.getParcelable(GlobalSettings.EXTRA_DINER);
		String sessionId = extras.getString(GlobalSettings.EXTRA_SESSION_ID);
		WebServiceTask task = new WebServiceTask(getApplicationContext(), new GetSessionWebServiceCall(sessionId), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
		    public void onSuccess(int code, String response) {
				try {
					SubmitOrderActivity.this.session = new EpicuriSessionDetail(new JSONObject(response));
					setupView(savedInstanceState, extras);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
	    });
		task.execute();
	}

	private void setupView(Bundle savedInstanceState, Bundle extras) {
		courses = extras.getParcelableArrayList(GlobalSettings.EXTRA_COURSES);
		modifierGroups = extras.getParcelableArrayList(GlobalSettings.EXTRA_MODIFIER_GROUPS);
		if(null == courses) throw new IllegalArgumentException("Expecting courses");

		if(null == selectedDiner){
			selectedDiner = session.getDiners().get(0);
		}

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		setTitle("Pending orders");

		setContentView(R.layout.activity_submitorder);

		ArrayList<EpicuriOrderItem> pendingItems = null;
		if(null == savedInstanceState) {
			pendingItems = extras.getParcelableArrayList(EXTRA_PENDING_ORDERS);

			pendingOrderFragment = PendingOrderFragment.newInstance(pendingItems, courses, modifierGroups);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.pendingOrderFrame, pendingOrderFragment, FRAGMENT_PENDING_ORDER)
					.commit();
		} else {
			pendingOrderFragment = (PendingOrderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_PENDING_ORDER);
		}

		View v = findViewById(R.id.dinerChooserPlaceholder);
		v.setVisibility(View.GONE);
		if (session.getType() == SessionType.DINE) {
			dinerChooserFragment = new DinerChooserFragment();
            if(session == null) {
                Crashlytics.log("Session is null in SubmitOrderActivity - Line ~97");
            }
            dinerChooserFragment.onSessionChanged(session);
            getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.dinerChooserPlaceholder, dinerChooserFragment, FRAGMENT_DINER)
					.commit();
			dinerChooserFragment.setEnableDinerActionMode(false);
			dinerChooserFragment.setDiner(selectedDiner); // select previously selected diner
			dinerChooserFragment
					.setOnDinerChangeListener(new OnDinerClickListener() {
						@Override
						public void onDinerClick(Diner diner) {
							selectedDiner = diner;
							pendingOrderFragment.selectDiner(diner);
						}
					});
		} else {
			findViewById(R.id.dinerChooserPlaceholder).setVisibility(View.GONE);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_submitorder, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    asapMenuItem = menu.findItem(R.id.menu_setToASAP);
	    defaultMenuItem = menu.findItem(R.id.menu_setToDefault);
	    asapMenuItem.setVisible(!isASAP);
	    defaultMenuItem.setVisible(isASAP);
		MenuItem i = menu.findItem(R.id.menu_showDiners);
		if(null == dinerChooserFragment){
			i.setVisible(false);
		} else {
			View v = findViewById(R.id.dinerChooserPlaceholder); 
			i.setTitle(v.getVisibility() == View.GONE ? R.string.menu_perGuest : R.string.menu_allItems);
		}
		super.onPrepareOptionsMenu(menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
            case android.R.id.home:
                cancelAndReturn();
                return true;
            case R.id.menu_submitOrder:
                if(!isSubmitOrderClicked){
                    submitPendingItems();
                    isSubmitOrderClicked = true;
                }
                return true;
            case R.id.menu_showDiners:
                View v = findViewById(R.id.dinerChooserPlaceholder);
                if(v.getVisibility() == View.GONE){
                    v.setVisibility(View.VISIBLE);
                    dinerChooserFragment.setDiner(session.getDiners().get(0));
                } else {
                    v.setVisibility(View.GONE);
                    dinerChooserFragment.setDiner(null);
                }
                invalidateOptionsMenu();
                return true;
            case R.id.menu_setToASAP:
                pendingOrderFragment.allOrdersASAP(true);
                asapMenuItem.setVisible(false);
                defaultMenuItem.setVisible(true);
                isASAP = true;
                return true;
            case R.id.menu_setToDefault:
                pendingOrderFragment.allOrdersASAP(false);
                asapMenuItem.setVisible(true);
                defaultMenuItem.setVisible(false);
                isASAP = false;
                return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		cancelAndReturn();
	}


	private void cancelAndReturn(){
		Intent i = new Intent();
		i.putParcelableArrayListExtra(EXTRA_PENDING_ORDERS, pendingOrderFragment.getOrders());
		setResult(RESULT_CANCELED, i);
		finish();
	}


	public void submitPendingItems(){
		System.out.println("Number of iterations");
		WebServiceTask task = new WebServiceTask(this,  new SubmitOrderWebServiceCall(session, pendingOrderFragment.getOrders()), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				// remove pending items from cache
				ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);
				JSONObject batches = null;
				boolean supportsImmediatePrinting = EpicuriApplication.getInstance(SubmitOrderActivity.this).getApiVersion() >= GlobalSettings.API_VERSION_6;
				if(supportsImmediatePrinting) {
					try {
						batches = new JSONObject(response);
						JSONArray array = batches.getJSONArray("batches");
						if (array != null) {
							for (int i = 0; i < array.length(); ++i) {
								EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
								objBatches.add(batch);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Intent intent = new Intent(SubmitOrderActivity.this, PrintDirectlyService.class);
					intent.putParcelableArrayListExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
					startService(intent);
				}

				Uri theUri = EpicuriContent.SESSION_URI.buildUpon().appendEncodedPath(session.getId()).build();

				UpdateService.requestUpdate(SubmitOrderActivity.this, theUri);
				
				setResult(RESULT_OK);

				finish();
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	private final HashSet<OnSessionChangeListener> listeners = new HashSet<OnSessionChangeListener>();
	@Override
	public void registerSessionListener(OnSessionChangeListener listener) {
		listeners.add(listener);
		if(null != session) listener.onSessionChanged(session);
	}
	@Override
	public void deRegisterSessionListener(OnSessionChangeListener listener) {
		listeners.remove(listener);
	}
}
