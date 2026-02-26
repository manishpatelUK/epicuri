package uk.co.epicuri.waiter.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.adapters.EventAdapter;
import uk.co.epicuri.waiter.webservice.AcknowledgeNotificationWebServiceCall;
import uk.co.epicuri.waiter.webservice.DelaySessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class SessionEventsFragment extends Fragment implements OnSessionChangeListener, AdapterView.OnItemClickListener {

	static final String FRAGMENT_ROW_AMEND = "RowAmend";

	@InjectView(android.R.id.list)
	ListView eventListView;
	@InjectView(android.R.id.empty)
	LoaderEmptyView ev;

	private EventAdapter eventAdapter;

	private EpicuriSessionDetail session;

	private ActionMode eventActionMode;
	private EpicuriEvent.Notification eventSelected;

	public SessionEventsFragment() {
        // Required empty public constructor
    }

    public static SessionEventsFragment newInstance(){
		return new SessionEventsFragment();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.sessiondetail_events, container, false);

	    ButterKnife.inject(this, view);

	    eventAdapter = new EventAdapter(getActivity());
	    eventListView.setAdapter(eventAdapter);
	    eventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

	    ev.setText("No Events Found");
	    eventListView.setEmptyView(ev);

	    eventListView.setOnItemClickListener(this);

	    return view;
    }

	@Override
	public void onResume() {
		super.onResume();
		((SessionContainer)getActivity()).registerSessionListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		((SessionContainer)getActivity()).deRegisterSessionListener(this);
		if(null != eventActionMode) eventActionMode.finish();
	}


	@Override
	public void onSessionChanged(EpicuriSessionDetail session) {
		this.session = session;

		eventAdapter.setState(session.getEvents());
		((LoaderEmptyView)eventListView.getEmptyView()).setDataLoaded();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(session.isPaid() || session.isClosed()) return;
		
		EpicuriEvent.Notification newEventSelected = (EpicuriEvent.Notification)eventAdapter.getItem(position);

		// don't activate if action is already acknowledged
		if(newEventSelected.getType() != EpicuriEvent.Type.TYPE_RECURRING && newEventSelected.getAcknowledgements().size() > 0){
			// hide action mode, or just clear selection
			if(null != eventActionMode){
				eventActionMode.finish();
			} else {
				eventListView.clearChoices();
				eventListView.requestLayout();
			}
			eventSelected = null;
			return;
		}

		if(newEventSelected.equals(eventSelected)){
			// "double click"
			acknowlege(eventSelected, true);
			eventActionMode.finish();
			return;
		}
		eventSelected = newEventSelected;

		if(null == eventActionMode){
			eventActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(new EventActionMode());
            // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
            if(null != eventActionMode) eventActionMode.invalidate();
        } else {
			eventActionMode.invalidate();
		}
	}


	public void postpone(EpicuriEvent.Notification notification) {
		WebServiceTask task = new WebServiceTask(getActivity(),  new DelaySessionWebServiceCall(
				notification,
				session.getId(),
				session.getLag()
		));
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				Toast.makeText(getActivity(), "Schedule delayed", Toast.LENGTH_SHORT).show();
			}
		});
		task.execute();
	}

	public void acknowlege(EpicuriEvent.Notification notification, boolean showUi) {
		WebServiceTask task = new WebServiceTask(getActivity(),  new AcknowledgeNotificationWebServiceCall(notification, session.getId()));
		if(showUi){
			task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		}
		task.execute();
	}


	private class EventActionMode implements ActionMode.Callback{

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getActivity().getMenuInflater().inflate(R.menu.action_eventhandler, menu);
			menu.findItem(R.id.menu_viewSession).setVisible(false);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.findItem(R.id.menu_postpone).setVisible(eventSelected.getType() == EpicuriEvent.Type.TYPE_SCHEDULED);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
				case R.id.menu_acknowledge: {
					acknowlege(eventSelected, true);
					eventActionMode.finish();
					return true;
				}
				case R.id.menu_postpone: {
					postpone(eventSelected);
					eventActionMode.finish();
					return true;
				}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			eventListView.clearChoices();
			eventListView.requestLayout();
			eventActionMode = null;
			eventSelected = null;
		}

	}
}
