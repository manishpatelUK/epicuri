package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.joda.money.Money;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Course;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.interfaces.TakeAweyOrderRemoveListener;

public class TakeawayOrderRemoveFragment extends Fragment {
	public static final String EXTRA_PENDING_ORDERS = "uk.co.epicuri.waiter.PendingOrders";

	private static final int LOADER_COURSES = 1;
	private static final int LOADER_MODIFIERS = 2;
	private static final String FRAGMENT_PENDING_ORDER = "pendingOrder";

	//	private EpicuriSessionDetail session;
	private ArrayList<Course> courses;
	private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;

	private PendingOrderFragment pendingOrderFragment;

	public static TakeawayOrderRemoveFragment newInstance(){
		TakeawayOrderRemoveFragment frag = new TakeawayOrderRemoveFragment();
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		Bundle extras = getArguments();

		courses = new ArrayList<Course>(1);
		courses.add(EpicuriMenu.Course.getDummyCourse("Takeaway"));

		getLoaderManager().initLoader(LOADER_MODIFIERS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<ModifierGroup>>> onCreateLoader(
					int id, Bundle args) {
				return new EpicuriLoader<ArrayList<ModifierGroup>>(getActivity(), new ModifierGroupLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader,
					LoaderWrapper<ArrayList<ModifierGroup>> data) {
				if(null == data){ // nothing returned, ignore
					return;
				}else if(data.isError()){
					Toast.makeText(getActivity(), "TakeawayOrderRemoveFragment error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				modifierGroups = data.getPayload();
				pendingOrderFragment.setModifierGroups(modifierGroups);
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader) {
				
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.activity_submitorder, container, false);

		if(null == savedInstanceState) {
			pendingOrderFragment = PendingOrderFragment.newInstance(courses, modifierGroups);
			getChildFragmentManager().beginTransaction()
					.replace(R.id.pendingOrderFrame, pendingOrderFragment, FRAGMENT_PENDING_ORDER)
					.commit();
		} else  {
			pendingOrderFragment = (PendingOrderFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_PENDING_ORDER);
		}
		v.findViewById(R.id.dinerChooserPlaceholder).setVisibility(View.GONE);
		return v;
	}


	@Override
	public void onResume() {
		super.onResume();
		pendingOrderFragment.setPendingItems(((TakeAweyOrderRemoveListener) getActivity()).getOrder());
	}
	
	@Override
	public void onPause() {
		((TakeAweyOrderRemoveListener)getActivity()).setOrder(pendingOrderFragment.getOrders());
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_takeawayremove, menu);
		MenuItem next = menu.findItem(R.id.menu_submitOrder);
		next.setEnabled(pendingOrderFragment.getOrders().size() > 0);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_additems:
			((TakeAweyOrderRemoveListener)getActivity()).addItems();
			return true;
		case R.id.menu_submitOrder:
			checkMoneyThenValidateOrders();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private void checkMoneyThenValidateOrders(){
		EpicuriRestaurant r = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
		double maxTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MAXTAKEAWAYVALUE));
		double minTakeaway = Double.parseDouble(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_MINTAKEAWAYVALUE));
		Money maxMoney = Money.of(LocalSettings.getCurrencyUnit(), maxTakeaway);
		Money minMoney = Money.of(LocalSettings.getCurrencyUnit(), minTakeaway);
		Money orderTotal = Money.zero(LocalSettings.getCurrencyUnit());
		for(EpicuriOrderItem item : pendingOrderFragment.getOrders()){
			orderTotal = orderTotal.plus(item.getCalculatedPriceIncludingQuantity());
		}
		boolean tooHigh = orderTotal.isGreaterThan(maxMoney);
		boolean tooLow = orderTotal.isLessThan(minMoney);
		if(tooLow || tooHigh){
			StringBuilder alertMessage = new StringBuilder(getString(R.string.submitWarnings));
			if(tooHigh){
				alertMessage.append("\n * High takeaway order value (Max ").append(LocalSettings.formatMoneyAmount(maxMoney, true)).append(")");
			} else if(tooLow){
				alertMessage.append("\n * Low takeaway order value (Min ").append(LocalSettings.formatMoneyAmount(minMoney, true)).append(")");
			} else {
				throw new IllegalStateException();
			}
			new AlertDialog.Builder(getActivity())
					.setTitle("Order value too " + (tooHigh ? "high" : "low"))
					.setMessage(alertMessage)
					.setPositiveButton("Continue Anyway", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							((TakeAweyOrderRemoveListener) getActivity()).showConfirmScreen();
						}
					})
					.setNegativeButton("Go Back", null)
					.show();
			return;
		}
		// if we haven't shown the error, then proceed with the next step
		((TakeAweyOrderRemoveListener) getActivity()).showConfirmScreen();
	}
}
