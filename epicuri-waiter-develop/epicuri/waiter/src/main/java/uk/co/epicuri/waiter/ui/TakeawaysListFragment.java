package uk.co.epicuri.waiter.ui;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.TakeawayAdapter;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.TakeawaysListListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.TakeawaysLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.webservice.AcceptTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.CancelTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.RequestBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class TakeawaysListFragment extends ListFragment implements ActionMode.Callback, OnClickListener, LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> {
	
	private static final int LOADER_TAKEAWAYS = 1;
	private static final int LOADER_PENDING_TAKEAWAYS = 2;
	
	private Button currentDateButton;
	private View todayButton;
	private TakeawayAdapter adapter;
	
	private ArrayList<EpicuriSessionDetail> takeaways = null;
	private ArrayList<EpicuriSessionDetail> pendingTakeaways;
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("E d MMM yyyy", Locale.UK);
	private Calendar now = Calendar.getInstance();

	private ActionMode mMode;
	private EpicuriSessionDetail activeTakeaway;

	private String preselectItemId = "0";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(null != savedInstanceState){
			now.setTimeInMillis(savedInstanceState.getLong("now"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong("now", now.getTime().getTime());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.datefilterlist, container, false);

		ListView listView = (ListView)view.findViewById(android.R.id.list);
        listView.setAdapter(adapter = new TakeawayAdapter(getActivity()));
		listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		listView.setEmptyView(view.findViewById(android.R.id.empty));

        for(int resId: new int[]{R.id.date_back, R.id.date_forward}){
        	View button = view.findViewById(resId);
        	button.setOnClickListener(this);
        }
        currentDateButton = (Button)view.findViewById(R.id.date_now);
        currentDateButton.setOnClickListener(this);
        
        todayButton = view.findViewById(R.id.today);
        todayButton.setOnClickListener(this);

		if(getArguments() != null && getArguments().containsKey(TakeawaysActivity.EXTRA_DATE)){
			now.setTimeInMillis(getArguments().getLong(TakeawaysActivity.EXTRA_DATE));
		}
        setDate(now);
        
        getLoaderManager().restartLoader(LOADER_PENDING_TAKEAWAYS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(
					int id, Bundle args) {
				EpicuriLoader loader = new EpicuriLoader<>(getActivity(), new TakeawaysLoaderTemplate());
				loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
				return loader;
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
					LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
				if(null == data){
					pendingTakeaways = null;
					getActivity().invalidateOptionsMenu();
					return;
				}else if(data.isError()){
					Toast.makeText(getActivity(), "TakeawaysListFragment error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				pendingTakeaways = data.getPayload();
				// sort pending takeaways
				Collections.sort(pendingTakeaways, new Comparator<EpicuriSessionDetail>() {
					@Override
					public int compare(EpicuriSessionDetail lhs,
							EpicuriSessionDetail rhs) {
						return lhs.getExpectedTime().compareTo(rhs.getExpectedTime());
					}
				});

				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> arg0) {
			}
		});

        return view;
    }

	@Override
	public void onResume() {
		getLoaderManager().restartLoader(LOADER_TAKEAWAYS, null, this);
		super.onResume();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		EpicuriSessionDetail newTakeaway = takeaways.get(position);
		if(newTakeaway.equals(activeTakeaway)){
			// "double click"
			String sessionId = newTakeaway.getId();
			((TakeawaysListListener)getActivity()).showTakeaway(sessionId, false);
			mMode.finish();
			return;
		}
		activeTakeaway = newTakeaway;
		fireActionMode();
	}

	private void fireActionMode(){
		if(null == activeTakeaway){
			if(null != mMode) mMode.finish();
		} else {
			if(null == mMode){
				mMode = ((AppCompatActivity)getActivity()).startSupportActionMode(this);
                // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
                if(null != mMode) mMode.invalidate();
            } else {
				mMode.invalidate();
			}
		}
	}

	@Override
	public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id, Bundle args) {
    	Calendar startOfTOday = Calendar.getInstance(now.getTimeZone());
    	startOfTOday.clear();
    	startOfTOday.set(
    			now.get(Calendar.YEAR),
    			now.get(Calendar.MONTH), 
    			now.get(Calendar.DAY_OF_MONTH), 0,0);
    	Date startTime = startOfTOday.getTime();
    	startOfTOday.add(Calendar.DAY_OF_MONTH, 1);
    	Date endTime = startOfTOday.getTime();
    	
    	EpicuriLoader loader = new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(getActivity(), new  TakeawaysLoaderTemplate(startTime, endTime));
		loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
			LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
		if(null == data){ // nothing returned, ignore
			return;
		}else if(data.isError()){
			Toast.makeText(getActivity(), "error loading data", Toast.LENGTH_SHORT).show();
			return;
		}
		takeaways = data.getPayload();
		
		Collections.sort(takeaways, new Comparator<EpicuriSessionDetail>() {
			@Override
			public int compare(EpicuriSessionDetail lhs,
					EpicuriSessionDetail rhs) {
				return lhs.getExpectedTime().compareTo(rhs.getExpectedTime());
			}
		});
		adapter.setState(takeaways);

		if(preselectItemId != null && !preselectItemId.equals("0") && !preselectItemId.equals("-1")){
			for(int i=0; i<takeaways.size(); i++){
				EpicuriSessionDetail ta = takeaways.get(i);
				if(ta.getId() != null && ta.getId().equals(preselectItemId)){
					activeTakeaway = ta;
					getListView().setItemChecked(i, true);
					break;
				}
			}
		}
		fireActionMode();
	}

	@Override
	public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> data) {
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.today: {
			now = Calendar.getInstance();
			break;
		}
		case R.id.date_now: {
			OnDateSetListener dateSetListener = new OnDateSetListener() {
				
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					now.clear();
					now.set(year, monthOfYear, dayOfMonth);
					setDate(now);
				}
			};
			new DatePickerDialog(getActivity(), dateSetListener, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
			break;
		}
		case R.id.date_back: {
			now.add(Calendar.DAY_OF_WEEK, -1);
			break;
		}
		case R.id.date_forward: {
			now.add(Calendar.DAY_OF_WEEK, 1);
			break;
		}
		}
		setDate(now);
	}

	/**
	 * jump to the specified day on the day view
	 * @param date
	 */
	public void jumpTo(Calendar date){
		now.setTime(date.getTime());
		setDate(now);
	}
	
	private boolean isToday = false;
	private void setDate(Calendar d){
		adapter.setState(null);
		currentDateButton.setText(dateFormat.format(now.getTime()));
		Calendar today = Calendar.getInstance();
		isToday = now.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
				&& now.get(Calendar.MONTH) == today.get(Calendar.MONTH)
				&& now.get(Calendar.YEAR) == today.get(Calendar.YEAR);
		todayButton.setEnabled(!isToday);
        getLoaderManager().restartLoader(LOADER_TAKEAWAYS, null, this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_takeaways, menu);
		
		MenuItem pendingMenuItem = menu.findItem(R.id.menu_pending);
		View pendingActionView = pendingMenuItem.getActionView();
		((ImageView)pendingActionView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_action_takeaways);
		TextView pendingActionViewText = ((TextView)pendingActionView.findViewById(R.id.count_text));
		pendingActionViewText.setVisibility(View.VISIBLE);
		pendingActionView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPending();
			}
		});

		List<EpicuriSessionDetail> pending = new ArrayList<>(1);
		Date now = new Date();

		if (pendingTakeaways != null)
			for (EpicuriSessionDetail session : pendingTakeaways) {
				if (session.getExpectedTime() != null && session.getExpectedTime().before(now))
					continue;

				if (session.getType() != EpicuriSessionDetail.SessionType.DINE
						&& !session.isDeleted()) {
					pending.add(session);
				}
			}

		if(pending.isEmpty()){
			pendingMenuItem.setVisible(false);
		} else {
			pendingMenuItem.setVisible(true);
			pendingActionViewText.setText(String.valueOf(pending.size()));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_add: {
			Calendar currentTime = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);
			((TakeawaysListListener)getActivity()).addTakeaway(now);
			return true;
		}
		case R.id.menu_pending: {
			showPending();
			return true;
		}
		case R.id.menu_refresh: {
			for(int loaderId: new int[]{LOADER_PENDING_TAKEAWAYS, LOADER_TAKEAWAYS}){
				UpdateService.requestUpdate(getActivity(), ((EpicuriLoader<?>)(Loader<?>)getLoaderManager().getLoader(loaderId)).getContentUri());
			}
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showPending(){
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		final ArrayList<EpicuriSessionDetail> pendingTakeawaysSnapshot = new ArrayList<EpicuriSessionDetail>(pendingTakeaways.size());
		pendingTakeawaysSnapshot.addAll(pendingTakeaways);
		
		ArrayAdapter<EpicuriSessionDetail> takeawaysAdapter = new ArrayAdapter<EpicuriSessionDetail>(getActivity(), android.R.layout.simple_list_item_2, pendingTakeawaysSnapshot){

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(null == convertView) convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
				EpicuriSessionDetail takeaway = getItem(position);
				((TextView)convertView.findViewById(android.R.id.text1)).setText(takeaway.getName());
				((TextView)convertView.findViewById(android.R.id.text2)).setText("For " + SimpleDateFormat.getDateInstance().format(takeaway.getExpectedTime()));
				return convertView;
			}
			
		};
		new AlertDialog.Builder(getActivity())
				.setTitle("Takeaways Pending Approval")
				.setAdapter(takeawaysAdapter, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EpicuriSessionDetail takeaway = pendingTakeawaysSnapshot.get(which);

						now.clear();
						now.setTime(takeaway.getExpectedTime());
						setDate(now);
						preselectItemId = takeaway.getId();
						dialog.dismiss();
					}
				}).show();
	}

	private void cancelTakeaway(final EpicuriSessionDetail session){
		WebServiceTask task = new WebServiceTask(getActivity(), new CancelTakeawayWebServiceCall(session.getId()), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				UpdateService.requestUpdate(getActivity(), ((EpicuriLoader) getLoaderManager().getLoader(LOADER_TAKEAWAYS)).getContentUri());
				UpdateService.requestUpdate(getActivity(), ((EpicuriLoader) getLoaderManager().getLoader(LOADER_PENDING_TAKEAWAYS)).getContentUri());

				// work out whether this is within the takeaway min time
				EpicuriRestaurant r = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
				int takeawayLockWindow = Integer.parseInt(r.getRestaurantDefault(EpicuriRestaurant.DEFAULT_TAKEAWAYLOCKWINDOW));
				Calendar now = Calendar.getInstance();
				now.add(Calendar.MINUTE, takeawayLockWindow);
				Date expectedTime = session.getExpectedTime();
				boolean takeawayTooSoon = now.getTimeInMillis() > expectedTime.getTime();

				if(takeawayTooSoon){
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.takeawaylockcancel_title)
							.setMessage(getString(R.string.takeawaylockcancel_message, takeawayLockWindow))
							.setPositiveButton("Dismiss", null)
							.setCancelable(false)
							.show();
				}
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}


	private void acceptTakeaway(String takeawayId){
		AcceptTakeawayWebServiceCall call = new AcceptTakeawayWebServiceCall(takeawayId);
		WebServiceTask task = new WebServiceTask(getActivity(), call);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(getActivity(), new Uri[]{EpicuriContent.TAKEAWAY_URI.buildUpon().appendQueryParameter("pendingWaiterAction", "true").build()});

				for(int loaderId: new int[]{LOADER_PENDING_TAKEAWAYS, LOADER_TAKEAWAYS}){
					UpdateService.requestUpdate(getActivity(), ((EpicuriLoader<?>)(Loader<?>)getLoaderManager().getLoader(loaderId)).getContentUri());
				}

			}
		});
		task.execute();
	}
	private void rejectTakeaway(String takeawayId, CharSequence message){
		RejectTakeawayWebServiceCall call = new RejectTakeawayWebServiceCall(takeawayId, message);
		WebServiceTask task = new WebServiceTask(getActivity(), call);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(getActivity(), new Uri[]{EpicuriContent.TAKEAWAY_URI.buildUpon().appendQueryParameter("pendingWaiterAction", "true").build()});
				for(int loaderId: new int[]{LOADER_PENDING_TAKEAWAYS, LOADER_TAKEAWAYS}){
					UpdateService.requestUpdate(getActivity(), ((EpicuriLoader<?>)(Loader<?>)getLoaderManager().getLoader(loaderId)).getContentUri());
				}

			}
		});
		task.execute();
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
		if(activeTakeaway.isDeleted() || activeTakeaway.isRejected()){
			Toast.makeText(getActivity(), "This takeaway been " + (activeTakeaway.isDeleted() ? "cancelled" : "rejected"), Toast.LENGTH_SHORT).show();
			activeTakeaway = null;
			getListView().clearChoices();
			preselectItemId = "0";

			// bug in angroid http://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
			getListView().requestLayout();
			return false;
		}

		actionMode.getMenuInflater().inflate(R.menu.action_takeaway, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
		if(activeTakeaway.isDeleted() || activeTakeaway.isRejected()){
			Toast.makeText(getActivity(), "This takeaway been " + (activeTakeaway.isDeleted() ? "cancelled" : "rejected"), Toast.LENGTH_SHORT).show();
			actionMode.finish();
			return true;
		}
		boolean closed = activeTakeaway.isClosed() || activeTakeaway.isDeleted();
		boolean accepted = !closed && activeTakeaway.isAccepted();
		boolean pending = !closed && !activeTakeaway.isAccepted();
		boolean inFuture = activeTakeaway.getExpectedTime().after(Calendar.getInstance().getTime());

		menu.setGroupVisible(R.id.menugroup_notaccepted, pending);
		menu.setGroupVisible(R.id.menugroup_accepted, accepted);
		menu.setGroupVisible(R.id.menugroup_closed, closed);
		menu.findItem(R.id.menu_requestbill).setEnabled(!activeTakeaway.isBillRequested());
		menu.findItem(R.id.menu_delete).setVisible(inFuture && accepted);
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
		switch(menuItem.getItemId()){
			case R.id.menu_viewDetails:
			case R.id.menu_viewDetails_notaccepted:
			case R.id.menu_edit: {
 				String sessionId = activeTakeaway.getId();
				((TakeawaysListListener)getActivity()).showTakeaway(sessionId, false);
				break;
			}
			case R.id.menu_requestbill: {
				final EpicuriSessionDetail session = activeTakeaway;
				if(session.isBillRequested()){
					Toast.makeText(getActivity(), "Bill already requested", Toast.LENGTH_SHORT).show();
					((TakeawaysListListener)getActivity()).showTakeaway(session.getId(), false);
				} else {
					WebServiceTask task = new WebServiceTask(getActivity(), new RequestBillWebServiceCall(session.getId()));
					task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

						@Override
						public void onSuccess(int code, String response) {
							((TakeawaysListListener)getActivity()).showTakeaway(session.getId(), false);
							Toast.makeText(getActivity(), R.string.toast_billRequested, Toast.LENGTH_SHORT).show();
						}
					});
					task.setIndicatorText(getString(R.string.webservicetask_alertbody));
					task.execute();
				}
				break;
			}
			case R.id.menu_delete: {
				final EpicuriSessionDetail takeawayToCancel = activeTakeaway;
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.cancelTakeaway_title)
						.setMessage(R.string.cancelTakeaway_message)
						.setPositiveButton("Cancel Takeaway",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
														int which) {
										cancelTakeaway(takeawayToCancel);
									}
								})
						.setNegativeButton("Do nothing", null)
						.show();
				break;
			}
			case R.id.menu_approve: {
				final EpicuriSessionDetail session = activeTakeaway;
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.acceptTakeaway_title)
						.setMessage(getString(R.string.acceptTakeaway_message, session.getName(), LocalSettings.getDateFormatWithDate().format(session.getExpectedTime())))
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								acceptTakeaway(session.getId());
							}
						})
						.setNegativeButton("No", null)
						.show();
				break;
			}
			case R.id.menu_reject: {
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_takeawayreject, null, false);
				final EditText rejectMessage = (EditText) view.findViewById(R.id.rejectmessage);
				rejectMessage.setSelectAllOnFocus(true);
				rejectMessage.setText(activeTakeaway.getRejectedReason());
                final String toReject = activeTakeaway.getId();
				new AlertDialog.Builder(getActivity()).setTitle("Reject takeaway with message")
						.setView(view)
						.setTitle("Reject this takeaway")
						.setNegativeButton("Cancel", null)
						.setPositiveButton("Reject", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								rejectTakeaway(toReject, rejectMessage.getText());
							}
						}).show();

				break;
			}
			default: {
				Log.e("TakeawaysListFragment", "Option not recognised from Takeaways actionmode: " + menuItem.getItemId());
			}
		}
		mMode.finish();
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode) {
		mMode = null;
		activeTakeaway = null;
		getListView().clearChoices();
		preselectItemId = "0";

		// bug in angroid http://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
		getListView().requestLayout();
	}
}
