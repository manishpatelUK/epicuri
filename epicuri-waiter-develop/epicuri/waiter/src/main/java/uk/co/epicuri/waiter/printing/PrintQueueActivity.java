package uk.co.epicuri.waiter.printing;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.PrintQueueListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterRedirectLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateRedirectWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteRedirectWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class PrintQueueActivity extends EpicuriBaseActivity implements OnItemClickListener, IPowerConnectionListener {
	private static final String LOG = "PrintQueueActivity";


	private static final int LOADER_PRINTER_REDIRECTS = 1;
	private static final int LOADER_PRINTERS = 2;

	@InjectView(R.id.status)
	TextView statusText;
	@InjectView(R.id.errors_text)
	TextView errorText;
	@InjectView(R.id.toggleSwitch)
	ToggleButton checkBox;
	@InjectView(R.id.list)
	ListView listview;
	@InjectView(R.id.redirectContainer_layout)
	ViewGroup redirectContainer;
	@InjectView(R.id.redirectTitle_text)
	TextView redirectTitleText;
	@InjectView(R.id.redirect_separator)
	View redirectSeparator;

	public ArrayAdapter<EpicuriPrintBatch> failedJobAdapter;

	private PrintQueueService mService;
	private boolean mBound = false;
    private PowerConnectionReceiver powerConnectionReceiver;
	private Toast cancelledAllToast;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_printqueue);
		ButterKnife.inject(this);
		Button clearerrorsButton = (Button) findViewById(R.id.cancelAll_button);
		cancelledAllToast = Toast.makeText(PrintQueueActivity.this, "Cancelled all items - queue will refresh soon.", Toast.LENGTH_SHORT);
		clearerrorsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			    new AlertDialog.Builder(PrintQueueActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("This will cancel all prints in the queue")
                        .setPositiveButton("Cancel All", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (mBound) {
                                    mService.cancelAll();
                                    cancelledAllToast.show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
			}
		});
		Button requeueAllButton = (Button) findViewById(R.id.requeueAll_button);
		requeueAllButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBound) {
					if (EpicuriApplication.getInstance(PrintQueueActivity.this).getApiVersion()
							>= GlobalSettings.API_VERSION_6) {
						mService.requeueApiCall();
						return;
					}

					mService.requeueAll();
				}
			}
		});

		statusText.setText("Unknown state");
		errorText.setText("No errors");
		checkBox.setEnabled(false);

		checkBox.setOnCheckedChangeListener(ccl);
		listview.setOnItemClickListener(this);

		getSupportLoaderManager().initLoader(LOADER_PRINTER_REDIRECTS, null, redirectLoader);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        powerConnectionReceiver = new PowerConnectionReceiver(this);
        registerReceiver(powerConnectionReceiver, filter);
	}

	private final CompoundButton.OnCheckedChangeListener ccl = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				startPrintService();
			} else {
				stopPrintService();
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(failedJobAdapter == null) {
			return;
		}

		EpicuriPrintBatch batch = failedJobAdapter.getItem(position);
		if(batch != null) {
			previewBatch(batch);
		}
	}

	private PrintQueueListener listener = new PrintQueueListener() {
		@Override
		public void statusChanged() {
            checkBox.setEnabled(PrintQueueServiceState.LAST_KNOWN_CHARGE_STATE == ChargeState.NOT_CHARGING);
            statusText.setText(mService.getStatus());
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(mService.isRunning());
			checkBox.setOnCheckedChangeListener(ccl);
		}

		@Override
		public void itemPrinted(EpicuriPrintBatch batch) {
			// don't care
			statusText.setText(mService.getStatus());
		}

		@Override
		public void itemFailed() {
			refreshJobsList();
			statusChanged();
		}

		private void refreshJobsList() {
			List<EpicuriPrintBatch> failedItems = PrintQueueServiceState.getErroredJobs();
			if(failedJobAdapter == null) {
				failedJobAdapter = new ArrayAdapter<>(PrintQueueActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, failedItems);
				listview.setAdapter(failedJobAdapter);
			}
			failedJobAdapter.clear();
			failedJobAdapter.addAll(failedItems);
			errorText.setText(getString(R.string.failedJobs, failedJobAdapter.getCount()));
		}

		@Override
		public void itemCancelOrRequeue() {
			refreshJobsList();
		}
	};

	private final LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriPrintRedirect>>> redirectLoader = new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriPrintRedirect>>>() {
		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriPrintRedirect>>> onCreateLoader(int i, Bundle bundle) {
			return new EpicuriLoader<ArrayList<EpicuriPrintRedirect>>(PrintQueueActivity.this, new PrinterRedirectLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriPrintRedirect>>> arrayListLoader, LoaderWrapper<ArrayList<EpicuriPrintRedirect>> result) {
			if (null == result|| result.isError())  {

			} else {
				LayoutInflater inflater = LayoutInflater.from(PrintQueueActivity.this);
				ArrayList<EpicuriPrintRedirect> epicuriPrintRedirects = result.getPayload();
				redirectContainer.removeAllViews();
				if(epicuriPrintRedirects.isEmpty()){
					// no redirects in place
					redirectTitleText.setVisibility(View.GONE);
					redirectSeparator.setVisibility(View.GONE);
				} else {
					// show list of active redirects with 'cancel' button
					redirectTitleText.setVisibility(View.VISIBLE);
					redirectSeparator.setVisibility(View.VISIBLE);
					for (EpicuriPrintRedirect redirect : epicuriPrintRedirects) {
						View v = inflater.inflate(R.layout.row_printer_redirect, redirectContainer, false);
						RedirectViewHolder vh = new RedirectViewHolder();
						ButterKnife.inject(vh, v);
						vh.description.setText(redirect.getSourcePrinter().getName() + " to " + redirect.getDestinationPrinter().getName());
						vh.cancelButton.setTag(redirect);
						vh.cancelButton.setOnClickListener(cancelRedirectListener);
						redirectContainer.addView(v);
					}
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriPrintRedirect>>> arrayListLoader) {
			redirectContainer.removeAllViews();
		}
	};
	private final View.OnClickListener cancelRedirectListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			final EpicuriPrintRedirect redirect = (EpicuriPrintRedirect) view.getTag();
			new AlertDialog.Builder(PrintQueueActivity.this)
					.setTitle("Remove printer redirect?")
					.setMessage("This will remove the redirect")
					.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							WebServiceTask task = new WebServiceTask(PrintQueueActivity.this, new DeleteRedirectWebServiceCall(redirect.getRedirectId()), true);
							task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
								@Override
								public void onSuccess(int code, String response) {
									// tell service to refresh printer list if we've changed it
									if(mBound){
										mService.loadPrinters();
									}
								}
							});
							task.execute();
						}
					})
					.setNegativeButton("Do nothing", null)
					.show();
		}
	};

    @Override
    public void onConnectionStatus(Context context, Intent intent, boolean isCharging) {
        checkBox.setEnabled(!isCharging);
    }

    static class RedirectViewHolder {
		@InjectView(R.id.description)
		TextView description;
		@InjectView(R.id.cancelButton)
		Button cancelButton;
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent serviceIntent = new Intent(this, PrintQueueService.class);
		boolean bound = bindService(serviceIntent, mConnection, Service.BIND_AUTO_CREATE);
		if (!bound) {
			statusText.setText("Connecting to print service...");
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConnection);
		mBound = false;
		try {
			unregisterReceiver(powerConnectionReceiver);
		} catch (IllegalArgumentException ex) {
			//can sometimes happen on restart
			Log.e(LOG, ex.getMessage());
		}
	}

	@Override
	protected void onResume() {
		if (mBound) {
			mService.setListener(listener);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mBound) {
			mService.setListener(null);
		}
		super.onPause();
	}

	private void startPrintService() {
		if (mBound) {
			checkBox.setEnabled(false);
			statusText.setText("Starting print service...");
			mService.startProcessingPrintQueue();
			PrintQueueServiceState.SWITCHED_ON_MANUALLY = true;
		} else {
			Toast.makeText(this, "Cannot talk to print queue", Toast.LENGTH_SHORT).show();
		}
	}

	private void stopPrintService() {
		if (mBound) {
			checkBox.setEnabled(false);
			statusText.setText("Stopping print service...");
			mService.stopProcessingPrintQueue(false);
		} else {
			Toast.makeText(this, "Cannot talk to print queue", Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * Defines callbacks for service binding, passed to bindService()
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
		                               IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			PrintQueueService.PrintQueueBinder binder = (PrintQueueService.PrintQueueBinder) service;
			mService = binder.getService();
			mBound = true;

			checkBox.setEnabled(mService != null && mService.isRunning() && PrintQueueServiceState.LAST_KNOWN_CHARGE_STATE == ChargeState.NOT_CHARGING);
			mService.setListener(listener);

			// refresh UI from service
			listener.statusChanged();
			listener.itemFailed();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			statusText.setText("Print service disconnected");
		}
	};


	/**
	 * show print job as a dialog
	 *
	 * @param batch
	 */
	private void previewBatch(final EpicuriPrintBatch batch) {

		new AlertDialog.Builder(this)
				.setTitle("Print preview")
				.setMessage(batch.getPrintText())
				.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mBound) {
							if(EpicuriApplication.getInstance(PrintQueueActivity.this)
									.getApiVersion() >= GlobalSettings.API_VERSION_6) {
								mService.requeueSingleCall(batch);
								return;
							}

							mService.requeueJob(batch);
						}
					}
				})
				.setNeutralButton("Cancel Job", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mBound) mService.cancelJob(batch);
					}
				})
				.setNegativeButton("Dismiss", null)
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_printqueue, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_addRedirect:{
			showRedirectDialog();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showRedirectDialog(){
		View view = getLayoutInflater().inflate(R.layout.dialog_addprinterredirect, null, false);

		final Spinner fromPrinter = (Spinner) view.findViewById(R.id.fromPrinter_spinner);
		final Spinner toPrinter = (Spinner) view.findViewById(R.id.toPrinter_spinner);

		LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>> printerCallback = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
			@Override
			public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
				return new OneOffLoader<ArrayList<EpicuriMenu.Printer>>(PrintQueueActivity.this, new PrinterLoaderTemplate());
			}

			@Override
			public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> printers) {
				ArrayList<EpicuriMenu.Printer> physicalPrinters = new ArrayList<EpicuriMenu.Printer>();
				for(EpicuriMenu.Printer p: printers){
					if(p.isPhysical()){
						physicalPrinters.add(p);
					}
				}
				ArrayAdapter<EpicuriMenu.Printer> adapter;

				adapter = new ArrayAdapter<EpicuriMenu.Printer>(PrintQueueActivity.this, android.R.layout.simple_spinner_item, physicalPrinters);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				fromPrinter.setAdapter(adapter);

				adapter = new ArrayAdapter<EpicuriMenu.Printer>(PrintQueueActivity.this, android.R.layout.simple_spinner_item, physicalPrinters);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				toPrinter.setAdapter(adapter);
			}

			@Override
			public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {

			}
		};
		getSupportLoaderManager().initLoader(LOADER_PRINTERS, null, printerCallback);

		new AlertDialog.Builder(this)
				.setTitle("Add Printer Redirect")
				.setView(view)
				.setPositiveButton("Add Redirect", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
//							WebServiceTask task = new WebServiceTask(PrintQueueActivity.this, new DeleteRedirectWebServiceCall(redirect.getRedirectId()));
//							task.execute();
						if (fromPrinter.getSelectedItem() != null && toPrinter.getSelectedItem() != null) {
							String fromId = ((EpicuriMenu.Printer) fromPrinter.getSelectedItem()).getId();
							String toId = ((EpicuriMenu.Printer) toPrinter.getSelectedItem()).getId();
							if (!fromId.equals(toId)) {
								WebServiceTask task = new WebServiceTask(PrintQueueActivity.this, new CreateRedirectWebServiceCall(fromId, toId), true);
								task.setIndicatorText("Creating redirect");
								task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
									@Override
									public void onSuccess(int code, String response) {
										// tell service to refresh printer list if we've changed it
										if(mBound){
											mService.loadPrinters();
										}
									}
								});
								task.execute();
								return;
							}
						}
						new AlertDialog.Builder(PrintQueueActivity.this)
								.setTitle("Error")
								.setMessage("Could not set up redirect")
								.show();
					}
				})
				.setNegativeButton("Do Nothing", null)
				.show();
	}

}
