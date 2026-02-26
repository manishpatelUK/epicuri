package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.CourseLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.interfaces.OnDinerClickListener;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.adapters.OrderAdapter;

import static uk.co.epicuri.waiter.ui.MenuFragment.EXTRA_QUICK_ORDER_MENU;

public class OrderingActivity extends EpicuriBaseActivity implements
		OnItemQueuedListener, SessionContainer, OnEpicuriMenuItemsSelectedListener {

	private static final int LOADER_COURSES = 1;
	private static final int LOADER_MODIFIERS = 2;
	
	private static final int REQUEST_REVIEWORDERS = 1;
	
	private static final String FRAGMENT_DINER = "diner";
    private static final String FRAGMENT_DINER_DETAILS = "dinerDetailsFragment";
	private static final String FRAGMENT_MENU = "menu";

	private EpicuriOrderItem pendingItem = null;
	private ViewGroup pendingItemView;
	private OrderAdapter.MenuItemViewHolder pendingItemViewHolder;
	
	private EpicuriSessionDetail session;
	private EpicuriSessionDetail.Diner selectedDiner;

	private DinerChooserFragment dinerChooserFragment;
	
	private ArrayList<EpicuriMenu.Course> courses;

	private ArrayList<EpicuriOrderItem> pendingItems;
	private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;
	private ArrayList<String> itemNames;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if(null == extras
				|| !extras.containsKey(GlobalSettings.EXTRA_SESSION)
				|| !extras.containsKey(GlobalSettings.EXTRA_DINER)){
			throw new IllegalArgumentException("Expected session, diner");
		}
		session = extras.getParcelable(GlobalSettings.EXTRA_SESSION);
		selectedDiner = extras.getParcelable(GlobalSettings.EXTRA_DINER);
		
		if(null == selectedDiner){
			selectedDiner = session.getDiners().get(0); // default to zero'th diner
		}
		
		courses = new ArrayList<EpicuriMenu.Course>();

		if(null != savedInstanceState){
			pendingItems = savedInstanceState.getParcelableArrayList(SubmitOrderActivity.EXTRA_PENDING_ORDERS);
		} else {
			pendingItems = new ArrayList<EpicuriOrderItem>();
		}
		
		setContentView(R.layout.activity_ordering);
		
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

		if (session.getType() == SessionType.DINE) {
			dinerChooserFragment = new DinerChooserFragment();
            if(session == null) {
                Crashlytics.log("Session is null in OrderingActivity - Line ~110");
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
							pendingItemView.setVisibility(View.GONE);
                            invalidateOptionsMenu();
						}
					});
		} else {
			findViewById(R.id.dinerChooserPlaceholder).setVisibility(View.GONE);
		}
		View v = findViewById(R.id.dinerChooserPlaceholder);
		v.setVisibility(View.GONE);
		pendingItemView = (ViewGroup)findViewById(R.id.orderItem);
		pendingItemViewHolder = new OrderAdapter.MenuItemViewHolder(pendingItemView);
		
		pendingItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == modifierGroups) return;
				MenuItemFragment.newInstance(selectedDiner, pendingItem, courses, modifierGroups)
					.show(getSupportFragmentManager(), "menuitem");
			}
		});

		final String preSelectMenuId = extras.getString(GlobalSettings.EXTRA_MENU_ID);

        MenuFragment f;
        if(getIntent().getExtras().getBoolean(EXTRA_QUICK_ORDER_MENU)){
            f = MenuFragment.newInstance(true);
        }else if(preSelectMenuId != null && !preSelectMenuId.equals("-1") && !preSelectMenuId.equals("0") && !session.getType().equals(SessionType.DINE)){
            f = MenuFragment.newInstance(preSelectMenuId, true);
        } else {
            f = MenuFragment.newInstance(preSelectMenuId, false);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menuFrame, f, FRAGMENT_MENU)
                .commit();
        getSupportLoaderManager().initLoader(LOADER_COURSES, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Course>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> onCreateLoader(int id,
					Bundle args) {
				return new EpicuriLoader<ArrayList<EpicuriMenu.Course>>(OrderingActivity.this, new CourseLoaderTemplate(session.getServiceId()));
			}

			@Override
			public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader,
					LoaderWrapper<ArrayList<EpicuriMenu.Course>> data) {
				if(null == data){ // nothing returned, ignore
					return;
				}else if(data.isError()){
					Toast.makeText(OrderingActivity.this, "OrderingActivity error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				courses = new ArrayList<EpicuriMenu.Course>();
				for(EpicuriMenu.Course c: data.getPayload()){
					if(c.getServiceId().equals(session.getServiceId())){
						courses.add(c);
					}
				}
			}

			@Override
			public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader) {
			}

		});

		getSupportLoaderManager().initLoader(LOADER_MODIFIERS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<ModifierGroup>>> onCreateLoader(
					int id, Bundle args) {
				return new EpicuriLoader<ArrayList<ModifierGroup>>(OrderingActivity.this, new ModifierGroupLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader,
					LoaderWrapper<ArrayList<ModifierGroup>> data) {
				if(null == data){ // nothing returned, ignore
					return;
				}else if(data.isError()){
					Toast.makeText(OrderingActivity.this, "OrderingActivity error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				modifierGroups = data.getPayload();
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader) {
				
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(SubmitOrderActivity.EXTRA_PENDING_ORDERS, pendingItems);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_ordering, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem reviewSendMenuItem = menu.findItem(R.id.menu_reviewOrders);
		reviewSendMenuItem.setEnabled(pendingItems.size() > 0);
		int orderCount  =0;
		for(EpicuriOrderItem i: pendingItems){
			orderCount += i.getQuantity();
		}
		reviewSendMenuItem.setTitle(String.format(Locale.UK, "Review order (%d items)", orderCount));

        MenuItem dinersMenuItem = menu.findItem(R.id.menu_showDiners);
        MenuItem dinerDetails = menu.findItem(R.id.menu_dinerDetail);
		if(null == dinerChooserFragment){
			dinersMenuItem.setVisible(false);
            dinerDetails.setVisible(false);
		} else {
			View v = findViewById(R.id.dinerChooserPlaceholder);
			dinersMenuItem.setTitle(v.getVisibility() == View.GONE ? "Show Guests" : "Hide Guests");
            dinerDetails.setVisible(v.getVisibility() != View.GONE && selectedDiner != null && !selectedDiner.isTable()); // don't show "diner details " if diners are hidden
            dinerDetails.setEnabled(selectedDiner != null && selectedDiner.getEpicuriCustomer() != null);
		}
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_showDiners: {
			View v = findViewById(R.id.dinerChooserPlaceholder); 
			v.setVisibility(v.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			invalidateOptionsMenu();
			return true;
		}
		case R.id.menu_reviewOrders: {
			pendingItemView.setVisibility(View.GONE);
			checkMoneyThenValidateOrders();
			return true;
		}
		case R.id.menu_dinerDetail: {
            DinerDetailsFragment frag = DinerDetailsFragment.newInstance(selectedDiner.getEpicuriCustomer(), selectedDiner.isBirthday());
            frag.show(getSupportFragmentManager(), FRAGMENT_DINER_DETAILS);
            return true;
		}
		case android.R.id.home: {
			validateExit();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		validateExit();
	}
	
	private void checkMoneyThenValidateOrders(){
		if(getIntent().getBooleanExtra(GlobalSettings.EXTRA_ENFORCE_LIMITS, false)){
			EpicuriRestaurant r = LocalSettings.getInstance(this).getCachedRestaurant();
			double maxTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MAXTAKEAWAYVALUE));
			double minTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MINTAKEAWAYVALUE));
			
			Money orderTotal = Money.zero(LocalSettings.getCurrencyUnit());
			for(EpicuriOrderItem item : pendingItems){
				orderTotal = orderTotal.plus(item.getCalculatedPriceIncludingQuantity());
			}
			boolean tooHigh = orderTotal.isGreaterThan(Money.of(LocalSettings.getCurrencyUnit(), maxTakeaway));
			boolean tooLow = orderTotal.isLessThan(Money.of(LocalSettings.getCurrencyUnit(), minTakeaway));
			if(tooLow || tooHigh){
				StringBuilder alertMessage = new StringBuilder(getString(R.string.submitWarnings));
				if(tooHigh){
					alertMessage.append("\n * High takeaway order value");
				} else if(tooLow){
					alertMessage.append("\n * Low takeaway order value");
				} else {
					throw new IllegalStateException();
				}
				new AlertDialog.Builder(this)
				.setTitle("Order value too " + (tooHigh ? "high" : "low"))
				.setMessage(alertMessage)
				.setPositiveButton("Continue Anyway", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						validateOrders();
					}
				})
				.setNegativeButton("Go Back", null)
				.show();
				return;
			}
		}
		// if we haven't shown the error, then proceed with the next step
		validateOrders();
	}
	
	private void validateOrders(){
        Toast.makeText(this, getString(R.string.toast_reviewOrders), Toast.LENGTH_SHORT).show();
		Intent reviewOrdersIntent = new Intent(this, SubmitOrderActivity.class);
		reviewOrdersIntent.putExtra(SubmitOrderActivity.EXTRA_PENDING_ORDERS, pendingItems);
		reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
		reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_DINER, (Parcelable) selectedDiner);
		reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_MODIFIER_GROUPS, modifierGroups);
		reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_COURSES, courses);
		reviewOrdersIntent.putExtra(GlobalSettings.EXTRA_ENFORCE_LIMITS, getIntent().getBooleanArrayExtra(GlobalSettings.EXTRA_ENFORCE_LIMITS));
		startActivityForResult(reviewOrdersIntent, REQUEST_REVIEWORDERS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_REVIEWORDERS:
			switch(resultCode){
			case RESULT_OK:
				setResult(RESULT_OK);
				// submitted orders, so close window
                Toast.makeText(this, R.string.toast_orderSubmitted, Toast.LENGTH_SHORT).show();
				finish();
				return;
			case RESULT_CANCELED:
				if(data == null || data.getExtras() == null) pendingItems = new ArrayList<>();
				else pendingItems = data.getExtras().getParcelableArrayList(SubmitOrderActivity.EXTRA_PENDING_ORDERS);
				invalidateOptionsMenu();
				return;
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onEpicuriMenuItemSelected(Item item) {
		Diner diner = selectedDiner;
		if(null == diner){
			diner = session.getTableDiner();
		}
		
		EpicuriOrderItem newItem = new EpicuriOrderItem(item, item.getCourseForService(session.getServiceId()));
		if(null == newItem.getCourse() && courses.size() == 1){
			newItem.setCourse(courses.get(0));
		}
		boolean autoAdd = true;
		
		if(null == newItem.getCourse()){
			// have not been able to automatically determine course for this item in this service
			autoAdd = false;
		}
		
		if(autoAdd){
			for(String modifierId: item.getModifierGroupIds()){
				EpicuriMenu.ModifierGroup group = null;
				if(modifierGroups == null) break;
				for(EpicuriMenu.ModifierGroup g: modifierGroups){
					if(g.getId().equals(modifierId)){
						group = g;
						break;
					}
				}
				if(null == group) throw new RuntimeException("Modifier not found");
				if(group.getLowerLimit() > 0){
					autoAdd = false;
					break;
				}
			}
		}
		
		if(!autoAdd) {
			MenuItemFragment.newInstance(diner, newItem, courses, modifierGroups).show(getSupportFragmentManager(), "menuitem");
		} else {
			queueItem(newItem, diner);
		}
	}
	
	public void setPendingItem(EpicuriOrderItem item) {
		pendingItem = item;
		if(null == item){
			pendingItemView.setVisibility(View.GONE);
			return;
		}
		
		pendingItemView.setVisibility(View.VISIBLE);
		pendingItemViewHolder.show(item, false);
	}


	Toast lastToast;
	public void showAToast(String toastMessage){
		try{ lastToast.getView().isShown();
			lastToast.setText(toastMessage);
		} catch (Exception e) {
			lastToast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
		}
		lastToast.show();
	}

	@Override
	public void queueItem(EpicuriOrderItem item, Diner diner) {
		item.setDinerId(diner.getId());
		if(item.getId() != null && !item.getId().equals(EpicuriOrderItem.DEFAULT_ID_VALUE)){
			int position = Integer.parseInt(item.getId());
			pendingItems.remove(position);
			pendingItems.add(position, item);
			Toast.makeText(this, getString(R.string.toast_itemUpdated), Toast.LENGTH_SHORT).show();
		} else {
			item.setId(String.valueOf(pendingItems.size()));
			pendingItems.add(item);
			showAToast(getString(R.string.toast_itemAdded));
		}

		setPendingItem(item);
		invalidateOptionsMenu();
	}

	@Override
	public void unQueueItem(EpicuriOrderItem orderItem, Diner diner) {
		if(Integer.parseInt(orderItem.getId()) >= 0){
			int position = Integer.parseInt(orderItem.getId());

			if(pendingItems.size() > 0) pendingItems.remove(position);
			// now renumber the remaining items
			for(int i=position; i<pendingItems.size(); i++){
				pendingItems.get(i).setId(i+"");
			}
			Toast.makeText(this, "Removed from pending items", Toast.LENGTH_SHORT).show();
			setPendingItem(null);	
		} else {
			Toast.makeText(this, "Could not remove item", Toast.LENGTH_SHORT).show();
		}
		invalidateOptionsMenu();
	}
	
	private long clickOneTime = 0;
	private void validateExit(){
		long now = new Date().getTime();
		if(0 == clickOneTime || (now - clickOneTime) > 4000){
			Toast.makeText(this, "Abandon this order? Click again to exit", Toast.LENGTH_SHORT).show();
			clickOneTime = now;
		} else {
			finish();
		}
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
