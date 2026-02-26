package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.HelpFiles;
import uk.co.epicuri.waiter.LoginSessionService;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.model.EpicuriLogin;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.printing.HubPowerConnectionListenerImpl;
import uk.co.epicuri.waiter.printing.PowerConnectionReceiver;
import uk.co.epicuri.waiter.printing.PrintUtil;
import uk.co.epicuri.waiter.ui.dialog.CashDrawerSelectDialog;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class EpicuriBaseActivity extends AppCompatActivity {

	LoginSessionService boundService = null;
	boolean bound = false;

	@InjectView(R.id.toolbar_progress_spinner)
	@Optional
	ProgressBar progressSpinner;
    private PowerConnectionReceiver powerConnectionReceiver;
	@Override
	protected void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		PreferenceManager.setDefaultValues(this, R.xml.network, false);
		super.onCreate(arg0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (bound) {
			unbindService(mConnection);
			bound = false;
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

		// Unregisters BroadcastReceiver when app is destroyed.
		if (receiver != null) {
			this.unregisterReceiver(receiver);
			receiver = null;
		}
		if(updateServiceResponseReceiver != null){
			unregisterReceiver(updateServiceResponseReceiver);
			updateServiceResponseReceiver = null;
		}
		dismissPleaseWaitDialog();
		unregisterReceiver(powerConnectionReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, LoginSessionService.class);

		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        IntentFilter batteryIntent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        powerConnectionReceiver = new PowerConnectionReceiver(new HubPowerConnectionListenerImpl());

        registerReceiver(powerConnectionReceiver, batteryIntent);
		{
			IntentFilter localFilter = new IntentFilter(LoginSessionService.ACTION_SHOW_LOCK_SCREEN);
			localFilter.addCategory(Intent.CATEGORY_DEFAULT);
			localFilter.addAction(UpdateService.ACTION_CONNECTION_ERROR);
			LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, localFilter);
		}

		{
			// Registers BroadcastReceiver to track network connection changes.
			IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			receiver = new NetworkReceiver();
			registerReceiver(receiver, filter);
		}

		{
			IntentFilter filter = new IntentFilter();
			filter.addAction(UpdateService.ACTION_REFRESH_STARTED);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			filter.addAction(UpdateService.ACTION_REFRESH_STOPPED);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			filter.addAction(WebServiceTask.ACTION_UPDATE_REQUIRED);
			filter.addCategory(Intent.CATEGORY_DEFAULT);

			updateServiceResponseReceiver = new UpdateServiceResponseReceiver();
			registerReceiver(updateServiceResponseReceiver, filter);
		}
	}

	ArrayList<CharSequence> errors = new ArrayList<CharSequence>();
	private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(LoginSessionService.ACTION_SHOW_LOCK_SCREEN)){
				showLockScreen();
				return;
			} else if(intent.getAction().equals(UpdateService.ACTION_CONNECTION_ERROR)){
				CharSequence errorMessage = intent.getExtras().getCharSequence(Intent.EXTRA_TEXT);
				errors.add(null == errorMessage ? "Unknown error" : errorMessage);
				invalidateOptionsMenu();
				return;
			}
		}
	};

	private void showLockScreen() {
		if(this instanceof WebViewActivity){
			// this might be the kitchen view
			if(getIntent().hasExtra(WebViewActivity.EXTRA_TYPE)) {
				WebViewActivity.Type type = (WebViewActivity.Type) getIntent().getSerializableExtra(WebViewActivity.EXTRA_TYPE);
				if (type == WebViewActivity.Type.KITCHEN) {
					// do nothing, we don't lock the kitchen view
					return;
				}
			}
		}
		// if activity is running...
		Intent lockScreenIntent = new Intent(this, LockActivity.class);
		startActivity(lockScreenIntent);
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LoginSessionService.LocalBinder binder = (LoginSessionService.LocalBinder) service;
			boundService = binder.getService();
			bound = true;
			onBind();
			if (!boundService.isLoggedIn()) {
				logout();
			} else if (boundService.isLocked()) {
				showLockScreen();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			boundService = null;
			bound = false;
		}
	};

	protected void onBind(){}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev != null && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
			if(null != boundService) boundService.resetTimer();
		}
		return super.dispatchTouchEvent(ev);
	}

	public EpicuriLogin getLoggedInUser() {
		if(null == boundService) return null;
		return boundService.getLoggedInUser();
	}

	private void logout() {
		TokenManager.newTokenAction(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_base, menu);

		String name = getClass().getSimpleName();
		String helpUrl = HelpFiles.getUrlForClass(name);
		menu.findItem(R.id.menu_help).setVisible(helpUrl != null);

		menu.findItem(R.id.menu_networkerror).setVisible(!isNetworkAvailable() || errors.size() > 0);

		super.onCreateOptionsMenu(menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_networkerror: {
				AlertDialog.Builder ab = new AlertDialog.Builder(this);
				ab.setTitle("Errors");
				if(!isNetworkAvailable()){
					ab.setMessage("No network connection detected");
				} else if(errors.size() > 0) {
					ab.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1, errors), null);
				} else {
					ab.setMessage("Network Error");
				}
				ab.show();
				// clear errors array
				errors = new ArrayList<CharSequence>();
				invalidateOptionsMenu();
				return true;
			}
			case R.id.menu_help: {
				Intent helpIntent = new Intent(this, WebViewActivity.class);
				helpIntent.putExtra(WebViewActivity.EXTRA_TYPE, WebViewActivity.Type.HELP);
				String name = getClass().getSimpleName();
				String helpUrl = HelpFiles.getUrlForClass(name);
				if(null == helpUrl){
					helpIntent.putExtra(WebViewActivity.EXTRA_URL, "http://lmgtfy.com/?q=How+does+" + name + "+work%3F");
				} else {
					helpIntent.putExtra(WebViewActivity.EXTRA_URL, helpUrl);
				}
				startActivity(helpIntent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private UpdateServiceResponseReceiver updateServiceResponseReceiver;
	private class UpdateServiceResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(UpdateService.ACTION_REFRESH_STARTED)){
				if(null != progressSpinner) progressSpinner.setVisibility(View.VISIBLE);
//                setProgressBarIndeterminateVisibility(true);
			} else if(intent.getAction().equals(UpdateService.ACTION_REFRESH_STOPPED)){
				if(null != progressSpinner) progressSpinner.setVisibility(View.GONE);
//				setProgressBarIndeterminateVisibility(false);
			}

			if(intent.getAction().equals(WebServiceTask.ACTION_UPDATE_REQUIRED)){
				View errorView = findViewById(R.id.upgradeRequired);
				if(null != errorView){
					errorView.setVisibility(View.VISIBLE);
					errorView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Intent upgradeIntent = new Intent(Intent.ACTION_VIEW);
							upgradeIntent.setData(Uri.parse("market://details?id=uk.co.epicuri.waiter"));
							upgradeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							EpicuriBaseActivity.this.startActivity(upgradeIntent);
						}
					});
				}
			}
		}
	}

	private class NetworkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			invalidateOptionsMenu();

			if(isNetworkAvailable()){
				runOnUiThread(new HideNeworkUnavailableUiRunnable());
			} else {
				runOnUiThread(new ShowNeworkUnavailableUiRunnable());
			}

		}
	}

	private class ShowNeworkUnavailableUiRunnable implements Runnable {

		@Override
		public void run() {
			View errorView = findViewById(R.id.noNetwork);
			if(null != errorView){
				errorView.setVisibility(View.VISIBLE);
			}
		}
	}
	private class HideNeworkUnavailableUiRunnable implements Runnable {

		@Override
		public void run() {
			View errorView = findViewById(R.id.noNetwork);
			if(null != errorView){
				errorView.setVisibility(View.GONE);
			}
		}
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	// The BroadcastReceiver that tracks network connectivity changes.
	private NetworkReceiver receiver = new NetworkReceiver();

	public void showPleaseWaitDialog(CharSequence message){
		if(null != pd){
			pd.dismiss();
		}
		pd = ProgressDialog.show(this, getString(R.string.webservicetask_alerttitle), message);
	}

	public void showPleaseWaitDialog(CharSequence message, DialogInterface.OnClickListener listener){
		if(null != pd){
			pd.dismiss();
		}
		pd = new ProgressDialog(this);
		pd.setTitle(getString(R.string.webservicetask_alerttitle));
		pd.setMessage(message);
		pd.setCancelable(false);
		pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", listener);
		pd.show();
	}

	private ProgressDialog pd;
	public void dismissPleaseWaitDialog(){
		if(null != pd){
			pd.dismiss();
			pd = null;
		}
	}

	public void changeDialogText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd != null) pd.setMessage(text);
			}
		});
	}

	public static void conditionalDrawerKick(Context context, Map<String, EpicuriMenu.Printer> printers, CashDrawerSelectDialog.OnDrawerKicked listener) {
		if(!LocalSettings.getInstance(context).isAllowed(WaiterAppFeature.DRAWER_KICK_NO_SALE)){
			listener.onDrawerKicked(false);
			return;
		}
		if (printers == null) {
			Toast.makeText(context, "Printers not loaded", Toast.LENGTH_SHORT).show();
			return;
		}

		LocalSettings settings = LocalSettings.getInstance(context);
		EpicuriRestaurant restaurant = settings.getCachedRestaurant();
		if(restaurant == null) {
			return;
		}

		List<EpicuriMenu.Printer> validPrinters = new ArrayList<>();
		for(EpicuriMenu.Printer printer : printers.values()) {
			if(restaurant.getConnectedCashDrawers().contains(printer.getId())) {
				validPrinters.add(printer);
			}
		}

		if(validPrinters.size() == 1) {
			PrintUtil.kickDrawer(context,validPrinters.get(0));
			if(listener != null) listener.onDrawerKicked(true);
		} else if(validPrinters.size() > 0){
			CashDrawerSelectDialog.newInstance().show(context, listener, validPrinters);
		}
	}
}