package uk.co.epicuri.waiter.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.CreateOrUpdateTakeawayListener;
import uk.co.epicuri.waiter.interfaces.PandingOrderListener;
import uk.co.epicuri.waiter.interfaces.SetPandingTakeawayListener;
import uk.co.epicuri.waiter.interfaces.TakeAweyOrderRemoveListener;
import uk.co.epicuri.waiter.interfaces.TakeawayDetailsListener;
import uk.co.epicuri.waiter.interfaces.TakeawayOrderListener;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.printing.PrintDirectlyService;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.ClearTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.SubmitOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class TakeawayActivity extends SessionActivity implements
		TakeawayDetailsListener, CreateOrUpdateTakeawayListener, TakeawayOrderListener,
		TakeAweyOrderRemoveListener, PandingOrderListener, SetPandingTakeawayListener {

	private static final String FRAGMENT_EDIT_TAKEAWAY = "editTakeaway";
	private static final String FRAGMENT_TAKEAWAY_DETAIL = "takeawayDetail";
	private static final String FRAGMENT_ORDERING = "ordering";
	private static final String FRAGMENT_REMOVE_ITEMS = "removeItems";
	private static final String FRAGMENT_CONFIRM = "confirm";

	public static final String EXTRA_DATE = "uk.co.epicuri.DATE";
	public static final String TAG_PENDING_TAKEAWAY = "PendingTakeaway";
	public static final String TAG_PENDING_ORDERS = "PendingOrders";
	public static final String TAG_DATE_BEFORE_EDIT = "DateBeforeEdit";

	private ArrayList<EpicuriOrderItem> pendingOrders = new ArrayList<EpicuriOrderItem>(0);
	private Date dateBeforeEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.simple_frame);

        if(null == savedInstanceState){
        	Bundle args = getIntent().getExtras();

			System.out.println("Session ID " + GlobalSettings.EXTRA_SESSION_ID);
			if(args.containsKey(GlobalSettings.EXTRA_SESSION_ID)){
				// viewing an existing takeaway
        		TakeawayFragment frag = new TakeawayFragment();
	    		frag.setArguments(getIntent().getExtras());
	    		getSupportFragmentManager().beginTransaction()
	    			.add(R.id.content_frame, frag, FRAGMENT_TAKEAWAY_DETAIL)
	    			.commit();
        	} else if(args.containsKey(EXTRA_DATE)) {
        		// create takeaway on specified date
    			Calendar d = (Calendar)args.getSerializable(EXTRA_DATE);
    			TakeawayDetailFragment frag = TakeawayDetailFragment.onDate(d);
    			getSupportFragmentManager().beginTransaction()
    				.add(R.id.content_frame, frag, FRAGMENT_EDIT_TAKEAWAY)
    				.commit();
    		} else {
    			throw new IllegalArgumentException("Cannot work out what to do");
    		}
    	} else {
			pendingTakeaway = savedInstanceState.getParcelable(TAG_PENDING_TAKEAWAY);
			pendingOrders = savedInstanceState.getParcelableArrayList(TAG_PENDING_ORDERS);

			if(savedInstanceState.containsKey(TAG_DATE_BEFORE_EDIT)){
				dateBeforeEdit = new Date(savedInstanceState.getLong(TAG_DATE_BEFORE_EDIT));
			}
		}
	}

	@Override
	void onSessionLoaded(EpicuriSessionDetail session) {
		// don't do anything, all handled in fragments
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(TAG_PENDING_TAKEAWAY, pendingTakeaway);
		outState.putParcelableArrayList(TAG_PENDING_ORDERS, pendingOrders);
		if(null != dateBeforeEdit) outState.putLong(TAG_DATE_BEFORE_EDIT, dateBeforeEdit.getTime());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(null != session && session.isClosed()) {
			if (getLoggedInUser().isManager()) {
				getMenuInflater().inflate(R.menu.activity_sessiondetail_closed, menu);
				menu.findItem(R.id.menu_void).setVisible(!session.isVoided());
				menu.findItem(R.id.menu_unvoid).setVisible(session.isVoided());
			}
		}
		return super.onCreateOptionsMenu(menu);
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
			Intent intent = new Intent(this, TakeawaysActivity.class);
			if(null != pendingTakeaway){
				intent.putExtra(TakeawaysActivity.EXTRA_DATE, pendingTakeaway.getTime().getTime());
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public ArrayList<EpicuriOrderItem> getOrder() {
		return pendingOrders;
	}
	@Override
	public void setOrder(ArrayList<EpicuriOrderItem> items) {
		pendingOrders = items;
	}

	@Override
	public void closeTakeawayEdit() {
		getSupportFragmentManager().popBackStack();
	}


	@Override
	public void editTakeawayDetails(EpicuriSessionDetail session) {
		pendingTakeaway = PendingTakeaway.from(session);
		pendingOrders = session.getOrders();

		dateBeforeEdit = session.getExpectedTime();

		// indices are used to reference the item for editing etc.
		for(int i=0; i<pendingOrders.size(); i++){
			pendingOrders.get(i).setId(i+"");
		}

		TakeawayDetailFragment frag = TakeawayDetailFragment.newInstance();

		getSupportFragmentManager().beginTransaction()
			.replace(R.id.content_frame, frag, FRAGMENT_EDIT_TAKEAWAY)
			.addToBackStack(FRAGMENT_EDIT_TAKEAWAY)
			.commit();
	}

	@Override
	public void closeTakeaway() {
		FragmentManager fm = getSupportFragmentManager();
		if(fm.getBackStackEntryCount() > 0){
			fm.popBackStack();
		} else {
			finish();
		}
	}

	@Override
	public void OnAllItemsRemoved() {
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void finishAdding() {
		getSupportFragmentManager().popBackStack();
	}

    @Override
    public void editItems() {
        TakeawayOrderRemoveFragment reviewFragment = TakeawayOrderRemoveFragment.newInstance();
        final FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction().replace(R.id.content_frame, reviewFragment, FRAGMENT_REMOVE_ITEMS).addToBackStack(FRAGMENT_REMOVE_ITEMS).commit();

        if(pendingOrders.isEmpty()){
            // if we don't have any items yet, also launch the add items fragment
            addItems();
        }
    }

	@Override
	public void addItems() {
		TakeawayOrderFragment frag = new TakeawayOrderFragment();
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.content_frame, frag, FRAGMENT_ORDERING)
			.addToBackStack(FRAGMENT_ORDERING).commit();
	}

	private PendingTakeaway pendingTakeaway;

	public void setPendingTakeaway(PendingTakeaway pendingTakeaway) {
		this.pendingTakeaway = pendingTakeaway;
	}
	public PendingTakeaway getPendingTakeaway() {
		return pendingTakeaway;
	}

	public static class PendingTakeaway implements Parcelable {
		private String id;
		private boolean delivery;
		private String name;
		private String phoneNumber;
		private String note;
		private Date time;
		private EpicuriCustomer.Address address;
		private EpicuriCustomer customer;
		private Money deliveryCost = Money.of(LocalSettings.getCurrencyUnit(), 20);
		private String rejectedReason = null;

		public PendingTakeaway(){}

		// generate PendingTakeaway from existing takeaway session
		public static PendingTakeaway from(EpicuriSessionDetail session){
			PendingTakeaway pt = new PendingTakeaway();
			pt.id = session.getId();
			pt.delivery = session.getType().equals(EpicuriSessionDetail.SessionType.DELIVERY);
			pt.name = session.getName();
			pt.phoneNumber = session.getDeliveryPhoneNumber();
			pt.note = session.getMessage();
			pt.time = session.getExpectedTime();
			pt.address = session.getDeliveryAddress();
			pt.customer = session.getDiners().get(0).getEpicuriCustomer(); // risky, might be null
			if(!session.isAccepted() && !session.isDeleted()){
				pt.rejectedReason = session.getRejectedReason();
			}
			return pt;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public boolean isDelivery() {
			return delivery;
		}

		public void setDelivery(boolean delivery) {
			this.delivery = delivery;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}

		public Date getTime() {
			return time;
		}

		public void setTime(Date time) {
			this.time = time;
		}

		public EpicuriCustomer.Address getAddress() {
			return address;
		}

		public void setAddress(EpicuriCustomer.Address address) {
			this.address = address;
		}

		public EpicuriCustomer getCustomer() {
			return customer;
		}

		public void setCustomer(EpicuriCustomer customer) {
			this.customer = customer;
		}

		public String getRejectedReason() {
			return rejectedReason;
		}

		public void setRejectedReason(String rejectedReason) {
			this.rejectedReason = rejectedReason;
		}

		public Money getDeliveryCost() {
			return deliveryCost;
		}

		public void setDeliveryCost(Money deliveryCost) {
			this.deliveryCost = deliveryCost;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel parcel, int i) {
			parcel.writeString(id);
			parcel.writeString(name);
			parcel.writeString(phoneNumber);
			parcel.writeString(note);
			parcel.writeLong(time.getTime());
			parcel.writeParcelable(address, 0);
			parcel.writeParcelable(customer, 0);
			parcel.writeSerializable(deliveryCost);
		}
		private PendingTakeaway(Parcel in){
			id = in.readString();
			name = in.readString();
			phoneNumber = in.readString();
			note = in.readString();
			time = new Date(in.readLong());
			address = in.readParcelable(EpicuriCustomer.Address.class.getClassLoader());
			customer = in.readParcelable(EpicuriCustomer.class.getClassLoader());
			deliveryCost = (Money)in.readSerializable();
		}

		public static final Parcelable.Creator<PendingTakeaway> CREATOR  = new Creator<PendingTakeaway>() {
			@Override
			public PendingTakeaway createFromParcel(Parcel parcel) {
				return new PendingTakeaway(parcel);
			}

			@Override
			public PendingTakeaway[] newArray(int i) {
				return new PendingTakeaway[0];
			}
		};
	}

	@Override
	public void showConfirmScreen() {
		TakeawayConfirmFragment frag = new TakeawayConfirmFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, frag, FRAGMENT_CONFIRM)
				.addToBackStack(FRAGMENT_CONFIRM)
				.commit();
	}

	@Override
	public void createOrUpdateTakeaway(){
		PendingTakeaway pt = pendingTakeaway;
		WebServiceCall call;
		if(pt.getId() != null && !pt.getId().equals("-1") && !pt.getId().equals("0")){
			call = new CreateEditTakeawayWebServiceCall(
				pt.getId(),
				pt.isDelivery(),
				pt.getName(),
				pt.getPhoneNumber(),
				pt.getNote(),
				pt.getTime(),
				pt.getAddress(),
				pt.getCustomer());
		} else {
			call = new CreateEditTakeawayWebServiceCall(
					"-1",
					pt.isDelivery(),
					pt.getName(),
					pt.getPhoneNumber(),
					pt.getNote(),
					pt.getTime(),
					pt.getAddress(),
					pt.getCustomer(),
					true);
		}


		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				try {
					JSONObject responseJson = new JSONObject(response);
					EpicuriSessionDetail session = new EpicuriSessionDetail(responseJson);
					clearExistingItems(session);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	private void clearExistingItems(final EpicuriSessionDetail session){
		WebServiceTask task = new WebServiceTask(this, new ClearTakeawayWebServiceCall(session.getId()), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				submitPendingItems(session);
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	private void submitPendingItems(final EpicuriSessionDetail session){

		// choose the first diner in the session and they'll receive all items
		EpicuriSessionDetail.Diner diner = session.getDiners().get(0);

		for(EpicuriOrderItem item: pendingOrders){
			item.setDinerId(diner.getId());
		}
		WebServiceTask task = new WebServiceTask(this,  new SubmitOrderWebServiceCall(session, pendingOrders), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);
				JSONObject batches = null;
				boolean supportsImmediatePrinting = EpicuriApplication.getInstance(TakeawayActivity.this).getApiVersion() >= GlobalSettings.API_VERSION_6;
				if(supportsImmediatePrinting) {
					try {
						batches = new JSONObject(response);
						JSONArray array = batches.getJSONArray("batches");
						if (array != null)
							for (int i = 0; i < array.length(); ++i) {
								EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
								objBatches.add(batch);
							}
					} catch (JSONException e) {
						e.printStackTrace();
					}

					Intent intent = new Intent(TakeawayActivity.this, PrintDirectlyService.class);
					intent.putExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
					startService(intent);
				}
				// remove pending items from cache
				Uri theUri = EpicuriContent.SESSION_URI.buildUpon().appendEncodedPath(String.valueOf(session.getId())).build();

				UpdateService.requestUpdate(TakeawayActivity.this, theUri);
				orderSubmitted(session.getId());
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	private void orderSubmitted(String sessionId) {
		// if waiter has edited, warn if it's close to delivery time
		if(dateBeforeEdit != null){

			// if edited within takeaway window, warn waiter
			EpicuriRestaurant restaurant = LocalSettings.getInstance(this).getCachedRestaurant();
			int takeawayMinTime = Integer.parseInt(restaurant.getRestaurantDefault(EpicuriRestaurant.DEFAULT_TAKEAWAYLOCKWINDOW));
			Calendar now = Calendar.getInstance();
			now.add(Calendar.MINUTE, takeawayMinTime);
			Date nowTime = now.getTime();
			if(nowTime.after(pendingTakeaway.getTime()) || nowTime.after(dateBeforeEdit)){
				showLockEditDialog(takeawayMinTime);
			}
		}

		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		TakeawayFragment frag = TakeawayFragment.newInstance(sessionId);
		fm.beginTransaction()
				.replace(R.id.content_frame, frag, FRAGMENT_TAKEAWAY_DETAIL)
				.commit();
		loadSession(sessionId);
	}

	private void showLockEditDialog(int takeawayMinTime){
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.takeawaylockedit_title))
				.setMessage(getString(R.string.takeawaylockedit_message, takeawayMinTime))
				.setPositiveButton("Dismiss", null)
				.show();
	}
}
