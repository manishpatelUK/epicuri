package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.money.Money;

import java.util.ArrayList;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CreateOrUpdateTakeawayListener;
import uk.co.epicuri.waiter.adapters.OrderAdapter;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.LocalSettings;

public class TakeawayConfirmFragment extends Fragment {

    private static final int LOADER_SESSIONDETAIL = 1;
	private static final int REQUEST_ORDER = 1;
    
    public static TakeawayConfirmFragment newInstance(int sessionId){
    	TakeawayConfirmFragment frag = new TakeawayConfirmFragment();
		Bundle args = new Bundle();
		args.putInt(GlobalSettings.EXTRA_SESSION_ID, sessionId);
		frag.setArguments(args);
		return frag;
    }
    
    private ListView orderList;
    private OrderAdapter orderAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_takeaway_confirm, container, false);
        
        orderList = (ListView)view.findViewById(R.id.orderList);
        orderList.setEmptyView(view.findViewById(android.R.id.empty));
        
        orderAdapter = new OrderAdapter(getActivity());
        orderAdapter.setCombineSimilarItems(true);
        orderList.setAdapter(orderAdapter);
        
        return view;
    }

	@Override
	public void onResume() {
		super.onResume();
		TakeawayActivity.PendingTakeaway pt = ((TakeawayActivity) getActivity()).getPendingTakeaway();
		updateUi(pt);
	}

	private void updateUi(TakeawayActivity.PendingTakeaway pt){

		TextView tv;
        View view = getView();
        
        tv = (TextView)view.findViewById(R.id.name_text);
		tv.setText(pt.getName());

		tv = (TextView)view.findViewById(R.id.status_text);
		if(pt.getId() != null && !pt.getId().equals("-1") && !pt.getId().equals("0")){
			tv.setText("Unsaved changes");
		} else {
			tv.setText("Unsaved takeaway");
		}

		view.findViewById(R.id.epicuriCustomer_image).setVisibility(pt.getCustomer() != null ? View.VISIBLE : View.GONE);

		tv = ((TextView)view.findViewById(R.id.notes_text));
		if(TextUtils.isEmpty(pt.getNote())){
			tv.setVisibility(View.GONE);
		} else {
			tv.setText(pt.getNote());
			tv.setVisibility(View.VISIBLE);
		}

        tv = (TextView)view.findViewById(R.id.time_text);
        tv.setText("Due: " + LocalSettings.getDateFormatWithDate().format(pt.getTime()));
        
        tv = (TextView)view.findViewById(R.id.address_text);
        if(pt.isDelivery()){
			tv.setText(pt.getAddress().toString());
        } else {
			tv.setVisibility(View.GONE);
        }
        
        tv = (TextView)view.findViewById(R.id.phoneNumber_text);
        if(null == pt.getPhoneNumber()){
        	tv.setVisibility(View.GONE);
        } else {
        	tv.setText(pt.getPhoneNumber());
        }

		view.findViewById(R.id.notAccepted_layout).setVisibility(View.GONE);


		ArrayList<EpicuriOrderItem> pendingItems = ((TakeawayActivity) getActivity()).getOrder();

		Money itemCost = Money.zero(LocalSettings.getCurrencyUnit());
		int numberOfItems = 0;
		for(EpicuriOrderItem item: pendingItems){
			itemCost = itemCost.plus(item.getCalculatedPriceIncludingQuantity());
			numberOfItems += item.getQuantity();
		}

		((TextView)view.findViewById(R.id.orderQuantity)).setText(getString(R.string.orderQuantity, numberOfItems));

		// TODO retrieve delivery cost from server
		Money totalCost = itemCost;
		String deliveryCostString = null;
		if(pt.isDelivery()){
			Money deliveryCost = pt.getDeliveryCost();
			if(deliveryCost == null){
				deliveryCostString = "Unknown delivery cost";
			} else if(deliveryCost.isZero()){
				deliveryCostString = "Free Delivery";
			} else {
				totalCost = totalCost.plus(deliveryCost);
				deliveryCostString = "Delivery cost: " + LocalSettings.formatMoneyAmount(deliveryCost, true);
			}
		}
//		((TextView)view.findViewById(R.id.orderWaiterCorrection_label)).setText(deliveryCostString);
//		((TextView)view.findViewById(R.id.orderLargeGroupTip_label)).setVisibility(View.GONE);

		((TextView)view.findViewById(R.id.orderTotal)).setText("Total: " + LocalSettings.formatMoneyAmount(totalCost, true));
        
        tv = (TextView)view.findViewById(R.id.takeawayType_text);
        tv.setText(pt.isDelivery() ? R.string.takeaway_delivery : R.string.takeaway_collection);

        orderAdapter.changeData(pendingItems);
        
        getActivity().invalidateOptionsMenu();
	}
	
	private final Handler han = new Handler();

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_takeawayconfirm, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_submitOrder: {
				((CreateOrUpdateTakeawayListener)getActivity()).createOrUpdateTakeaway();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
