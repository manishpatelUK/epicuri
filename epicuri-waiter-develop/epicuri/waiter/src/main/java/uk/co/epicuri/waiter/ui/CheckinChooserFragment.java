package uk.co.epicuri.waiter.ui;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.webservice.CustomerOnDinerWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetCheckedInCustomers;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class CheckinChooserFragment extends DialogFragment {

	private static final int LOADER_CHECKINS = 1;
	@InjectView(android.R.id.list)
	ListView mList;
	@InjectView(android.R.id.empty)
	View mEmpty;

	private ArrayList<EpicuriCustomer.Checkin> checkins = new ArrayList<EpicuriCustomer.Checkin>();
	private ArrayAdapter<EpicuriCustomer.Checkin> customerAdapter;

	public static CheckinChooserFragment newInstance(Diner diner, String sessionId) {
		Bundle args = new Bundle(2);
		args.putParcelable(GlobalSettings.EXTRA_DINER, diner);
		args.putString(GlobalSettings.EXTRA_SESSION_ID, sessionId);
		CheckinChooserFragment frag = new CheckinChooserFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		if(null != customerAdapter && checkins != null) {
		    checkins.clear();
		    customerAdapter.clear();
        }

		WebServiceTask webServiceTask = new WebServiceTask(this.getContext(), new GetCheckedInCustomers(), true);
		webServiceTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                ArrayList<EpicuriCustomer.Checkin> data = new ArrayList<>();
                try {
                    JSONArray responseJson = new JSONArray(response);
                    for(int i=0; i<responseJson.length(); i++){
                        data.add(new EpicuriCustomer.Checkin(responseJson.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    return;
                }

                checkins = data;
                if (null != customerAdapter) {
                    customerAdapter.clear();
                    customerAdapter.addAll(checkins);
                }
            }
        });

		webServiceTask.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getActivity(), "Error loading Check Ins", Toast.LENGTH_SHORT).show();
            }
        });
		webServiceTask.execute();

		/*getLoaderManager().restartLoader(LOADER_CHECKINS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> onCreateLoader(
					int id, Bundle args) {
				EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>> loader = new EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>>(getActivity(), new CheckinLoaderTemplate());
				loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
				return loader;
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader,
					LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>> data) {
				if (null == data) { // nothing returned, ignore
					return;
				} else if (data.isError()) {
					Toast.makeText(getActivity(), "CheckinChooserFragment error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				checkins = data.getPayload();
				if (null != customerAdapter) {
					customerAdapter.clear();
					customerAdapter.addAll(checkins);
				}
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader) {
				checkins = null;
			}

		});*/
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_checkins, null, false);
		ButterKnife.inject(this, v);
		customerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, checkins);

		mList.setAdapter(customerAdapter);
		mList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			                        long id) {
				EpicuriCustomer customer = checkins.get(position).getCustomer();
				onCustomerChosen(customer);
			}
		});
		mList.setEmptyView(mEmpty);

		return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.choose_checked_in_customer))
                .setView(v)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();
	}

	public void onCustomerChosen(EpicuriCustomer customer) {
		Diner selectedDiner = getArguments().getParcelable(GlobalSettings.EXTRA_DINER);
		String sessionId = getArguments().getString(GlobalSettings.EXTRA_SESSION_ID);
		if (null != selectedDiner && null != customer) {
			WebServiceTask task = new WebServiceTask(getActivity(), new CustomerOnDinerWebServiceCall(selectedDiner, customer, sessionId), true);
			task.setIndicatorText(getString(R.string.webservicetask_alertbody));
			task.execute();
			getDialog().dismiss();
		}
	}
}
