package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.interfaces.ListenerMews;
import uk.co.epicuri.waiter.webservice.LookupMewsCustomerWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;


public class MewsCustomerLookup extends DialogFragment {
	protected static final String EXTRA_TARGET_FRAGMENT = "target";

	@InjectView(android.R.id.empty)
	View emptyView;
	@InjectView(android.R.id.list)
	ListView listview;

	@InjectView(R.id.room_edit)
	EditText room_edit;
	@InjectView(R.id.name_edit)
	EditText name_edit;
	
	private ArrayList<EpicuriMewsCustomer> customers;

	public static MewsCustomerLookup newInstance(){
		return new MewsCustomerLookup();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_mewscustomerlookup, null);
		ButterKnife.inject(this, view);

		room_edit.setImeActionLabel("Lookup", 1);
		room_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				sendRequest();
				return true;
			}
		});

		name_edit.setImeActionLabel("Lookup", 1);
		name_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				sendRequest();
				return true;
			}
		});
		listview.setEmptyView(emptyView);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				selectCustomer(customers.get(position));
			}

		});
		
		return new AlertDialog.Builder(getActivity())
			.setTitle("Search for customer")
			.setView(view)
			.setNegativeButton("Cancel", null)
			.create();
	}

	private void selectCustomer(EpicuriMewsCustomer customer) {
		try{
			ListenerMews targetFragment = (ListenerMews) getTargetFragment();
			targetFragment.setCustomer(customer);
			dismiss();
		} catch (ClassCastException e){
			throw new RuntimeException("Calling fragment must implement Listener");
		}
	}

	@OnClick(R.id.search_button)
	void sendRequest(){
		String name = name_edit.getText().toString();
		String roomNumber = room_edit.getText().toString();
		LookupMewsCustomerWebServiceCall call = new LookupMewsCustomerWebServiceCall(name, roomNumber);
		WebServiceTask task = new WebServiceTask(getActivity(), call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				
				try{
					JSONArray customersJson = new JSONArray(response);
					customers = new ArrayList<EpicuriMewsCustomer>(customersJson.length());
					for(int i=0; i<customersJson.length(); i++){
						JSONObject aCustomerJson = customersJson.getJSONObject(i);
						customers.add(new EpicuriMewsCustomer(aCustomerJson));
					}
					listview.setAdapter(new ArrayAdapter<EpicuriMewsCustomer>(getActivity(), android.R.layout.simple_list_item_single_choice, customers));
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
		});
		task.setIndicatorText(getString(R.string.mews_looking_up_customer));
		task.execute();
	}
	
}
