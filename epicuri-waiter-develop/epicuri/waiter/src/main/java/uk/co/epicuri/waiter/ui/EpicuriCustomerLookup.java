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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.interfaces.CustomerListener;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.LookupCustomerWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;



public class EpicuriCustomerLookup extends DialogFragment {
	
	private ListView listview;
	private EditText email_edit;
	private EditText phone_edit;
	
	private ImageButton searchButton;
	
	private ArrayList<EpicuriCustomer> customers;

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_epicuricustomerlookup, null);

		email_edit = (EditText)view.findViewById(R.id.email_edit);
		phone_edit = (EditText)view.findViewById(R.id.phone_edit);

		searchButton = (ImageButton)view.findViewById(R.id.search_button);
		searchButton .setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendRequest();
			}
		});
		
		email_edit.setImeActionLabel("Lookup", 1);
		email_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				sendRequest();
				return true;
			}
		});

		phone_edit.setImeActionLabel("Lookup", 1);
		phone_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				sendRequest();
				return true;
			}
		});
		listview = (ListView)view.findViewById(android.R.id.list);
		listview.setEmptyView(view.findViewById(android.R.id.empty));
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				EpicuriCustomer customer = customers.get(position);
				String targetFragmentTag = getArguments().getString(GlobalSettings.EXTRA_TARGET_FRAGMENT);
				try{
					CustomerListener targetFragment = (CustomerListener)getFragmentManager().findFragmentByTag(targetFragmentTag);
					targetFragment.setCustomer(customer);
					dismiss();
				} catch (ClassCastException e){
					throw new RuntimeException("Calling fragment must implement CustomerListener");
				}
			}
			
		});
		
		return new AlertDialog.Builder(getActivity())
			.setTitle("Search for customer")
			.setView(view)
			.setNegativeButton("Cancel", null)
			.create();
	}

	private void sendRequest(){
		String phoneNumber = phone_edit.getText().toString();
		String email = email_edit.getText().toString();
		LookupCustomerWebServiceCall call = new LookupCustomerWebServiceCall(phoneNumber, email);
		WebServiceTask task = new WebServiceTask(getActivity(), call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				
				try{
					JSONArray customersJson = new JSONArray(response);
					customers = new ArrayList<EpicuriCustomer>(customersJson.length());
					for(int i=0; i<customersJson.length(); i++){
						JSONObject aCustomerJson = customersJson.getJSONObject(i);
						customers.add(new EpicuriCustomer(aCustomerJson));
					}
					listview.setAdapter(new ArrayAdapter<EpicuriCustomer>(getActivity(), android.R.layout.simple_list_item_single_choice, customers));
				} catch (JSONException e){
					
				}
			}
		});
		task.setIndicatorText("Looking up Customer");
		task.execute();
	}
	
}
