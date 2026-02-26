package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CashUpAdapter;
import uk.co.epicuri.waiter.interfaces.OnCashUpListener;
import uk.co.epicuri.waiter.interfaces.OnCashUpPrintListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.CashUpLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterRedirectLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriCashUp;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.printing.CashUpFragment;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CheckSessionStateWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateCashUpWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by pharris on 23/09/14.
 */
public class CashUpActivity extends EpicuriBaseActivity implements OnCashUpListener, OnCashUpPrintListener {

    @InjectView(android.R.id.list)
	ListView listView;

    CashUpAdapter adapter;

	public static DateFormat dateFormat;
	public static DateFormat timeFormat;

	ArrayList<EpicuriCashUp> cashUps;
	Date latestCashupDate;
	private Map<String,EpicuriMenu.Printer> printers;
	private Map<String,EpicuriPrintRedirect> printerRedirects;

	private static boolean cashUpIsRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cashup);
		ButterKnife.inject(this);

		dateFormat = android.text.format.DateFormat.getDateFormat(this);
		timeFormat = android.text.format.DateFormat.getTimeFormat(this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showCashUp((EpicuriCashUp) adapter.getItem(position));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_CASH_UPS, null, callback);

		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_PRINTERS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
			@Override
			public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
				return new OneOffLoader<>(CashUpActivity.this, new PrinterLoaderTemplate());
			}

			@Override
			public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> result) {
				if(null == result) return;
				printers = new HashMap<>(result.size());
				for (EpicuriMenu.Printer p : result) {
					printers.put(p.getId(), p);
				}
			}

			@Override
			public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
				// don't care
			}
		});

		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_PRINTER_REDIRECTS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriPrintRedirect>>() {
			@Override
			public Loader<ArrayList<EpicuriPrintRedirect>> onCreateLoader(int i, Bundle bundle) {
				return new OneOffLoader<>(CashUpActivity.this, new PrinterRedirectLoaderTemplate());
			}

			@Override
			public void onLoadFinished(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader, ArrayList<EpicuriPrintRedirect> result) {
				if(null == result) return;
				printerRedirects = new HashMap<>(result.size());
				for (EpicuriPrintRedirect p : result) {
					printerRedirects.put(p.getSourcePrinter().getId(), p);
				}
			}

			@Override
			public void onLoaderReset(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader) {
				// don't care
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_cashup, menu);
		try {
			LocalSettings localSettings = LocalSettings.getInstance(CashUpActivity.this);
			for(int i = 0; i < menu.size(); i++) {
				if(menu.getItem(i).getItemId() == R.id.menu_endOfDay) {
					menu.getItem(i).setEnabled(localSettings.isAllowed(WaiterAppFeature.CASH_UP));
					break;
				}
			}
		} catch (Exception ex) {
			Log.e("CashUpActivity", "Error trying to set button state", ex);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_cashUp: {
				checkCashUpState();
				return true;
			}
			case R.id.menu_endOfDay: {
				showEndOfDay();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}


	final LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCashUp>>> callback = new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCashUp>>>() {
		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriCashUp>>> onCreateLoader(int id, Bundle args) {
			CashUpLoaderTemplate t = new CashUpLoaderTemplate();
			return new EpicuriLoader<>(CashUpActivity.this, t);
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriCashUp>>> loader, LoaderWrapper<ArrayList<EpicuriCashUp>> data) {
			if(null == data || data.isError()){
				return;
			}
			cashUps = new ArrayList<EpicuriCashUp>();
			for(EpicuriCashUp c: data.getPayload()){
				if(c.isCashup()){
					cashUps.add(c);
				}
			}
			Collections.sort(cashUps, new Comparator<EpicuriCashUp>() {
			            @Override
			            public int compare(EpicuriCashUp c1, EpicuriCashUp c2) {
			                return c2.getEndTime().compareTo(c1.getEndTime());
			            }
			        }
			);

			// work out the most recent cash up date
			latestCashupDate = null;
			for(EpicuriCashUp c: cashUps){
				if(null == latestCashupDate || c.getEndTime().after(latestCashupDate)){
					latestCashupDate = c.getEndTime();
				}
			}
		//	adapter.notifyDataSetChanged();
			listView.setAdapter(adapter = new CashUpAdapter(CashUpActivity.this, cashUps));
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriCashUp>>> loader) {

		}
	};

	void checkCashUpState(){
		// check state, if
		WebServiceTask task = new WebServiceTask(this, new CheckSessionStateWebServiceCall(), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				showNewCashUp(true);
			}
		});
		task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
			@Override
			public void onError(int code, String response) {
				// cannot cash up
				showNewCashUp(false);

			}
		});
		task.setIndicatorText("Checking state...");
		task.execute();
	}

	void showNewCashUp(boolean canCashup){
		AddCashUpDialogFragment.newInstance(latestCashupDate, canCashup).show(getSupportFragmentManager(), null);
	}

	@Override
	public void showEndOfDay() {
		Intent endOfDayIntent = new Intent(this, EndOfDayActivity.class);
		startActivity(endOfDayIntent);
	}

	@Override
	public void onCashUp(final Date fromDate, final Date toDate, final boolean simulate, final boolean canCashUp) {
		if(simulate){
			WebServiceTask task = new WebServiceTask(this, new CreateCashUpWebServiceCall(fromDate, toDate, true), true);
			task.setIndicatorText("Simulating cash up...");
			task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
				@Override
				public void onSuccess(int code, String response) {
					try {
						EpicuriCashUp cashUp = new EpicuriCashUp(new JSONObject(response));
						showSimulatedCashUp(cashUp, fromDate, toDate, canCashUp);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			task.execute();
		} else {
			if(null == toDate) throw new IllegalArgumentException("To date cannot be null");
			new AlertDialog.Builder(this)
					.setTitle("Cash up")
					.setMessage(getString(R.string.cashup_are_you_sure))
					.setPositiveButton("Cash up", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							if(cashUpIsRunning) {
								Toast.makeText(CashUpActivity.this, "Cash up is already in progress. Please wait.", Toast.LENGTH_SHORT).show();
								return;
							}
							WebServiceTask task = new WebServiceTask(CashUpActivity.this, new CreateCashUpWebServiceCall(fromDate, toDate), true);
							task.setIndicatorText("Cashing up...");
							cashUpIsRunning = true;
							task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                                @Override
                                public void onSuccess(int code, String response) {
                                    // if cashup simulate is visible, then dismiss it
                                    CashUpFragment frag = (CashUpFragment) getSupportFragmentManager().findFragmentByTag(GlobalSettings.FRAGMENT_CASH_UP);
                                    if(null != frag){
                                        frag.dismiss();
                                    }
                                    cashUpIsRunning = false;
                                }
                            });
							task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
								@Override
								public void onError(int code, String response) {
									cashUpIsRunning = false;
								}
							});
							task.execute();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
		}
	}

	void showSimulatedCashUp(EpicuriCashUp cashUp, Date fromDate, Date toDate, boolean canGenerateCashUp) {
		CashUpFragment.newInstance(getLoggedInUser().getName(), cashUp, fromDate, toDate, canGenerateCashUp).show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_CASH_UP);
	}

	void showCashUp(EpicuriCashUp cashUp){
		CashUpFragment.newInstance(getLoggedInUser().getName(), cashUp).show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_CASH_UP);
	}
}
