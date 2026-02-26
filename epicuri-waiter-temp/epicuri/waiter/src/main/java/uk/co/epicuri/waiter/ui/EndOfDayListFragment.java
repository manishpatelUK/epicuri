package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CloseSessionAdapter;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.ReservationsLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.TakeawaysLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CloseSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by pharris on 18/05/15.
 */
public class EndOfDayListFragment extends ListFragment {

    private CloseSessionAdapter sessionAdapter;
    private CloseReservationAdapter reservationAdapter;

    public enum Type {
        SESSION, PENDING_TAKEAWAY, PENDING_RESERVATION, RESERVATION
    }

    private Type currentType;

    public static EndOfDayListFragment newInstance (Type type){
        EndOfDayListFragment frag;
        frag = new EndOfDayListFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(GlobalSettings.EXTRA_TYPE, type);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        currentType = (Type) getArguments().getSerializable(GlobalSettings.EXTRA_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadData();
    }
    
    private void reloadData(){
        switch(currentType){
            case SESSION: {
                setEmptyText(getString(R.string.empty_sessions));
                getLoaderManager().restartLoader(GlobalSettings.LOADER_LIST, null, sessionLoaderCallbacks);
                break;
            }
            case PENDING_TAKEAWAY: {
                setEmptyText(getString(R.string.empty_pendingTakeaways));
                getLoaderManager().restartLoader(GlobalSettings.LOADER_LIST, null, takeawayLoaderCallbacks);
                break;
            }
            case PENDING_RESERVATION:
            case RESERVATION: {
                setEmptyText(getString(currentType == Type.RESERVATION ? R.string.empty_reservations : R.string.empty_pendingReservations));
                getLoaderManager().restartLoader(GlobalSettings.LOADER_LIST, null, reservationLoaderCallbacks);
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_endofday, menu);
        MenuItem item = menu.findItem(R.id.menu_go);
        switch(currentType){
            case RESERVATION:
                item.setTitle("Cancel Reservations");
                break;
            case PENDING_RESERVATION:
                item.setTitle("Reject Reservations");
                break;
            case SESSION:
                item.setTitle("Close Sessions");
                break;
            case PENDING_TAKEAWAY:
                item.setTitle("Reject Pending Takeaways");
                break;
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_go: {
                switch(currentType){
                    case SESSION: {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Are you sure")
                                .setMessage(getString(R.string.endofday_close_sessions_message))
                                .setPositiveButton("Close Sessions", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LinkedList<String> sessionIds = new LinkedList<String>();
                                        for(int i=0; i<sessionAdapter.getCount(); i++){
                                            sessionIds.add(sessionAdapter.getItem(i).getId());
                                        }
                                        closeSessions(sessionIds);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                    }
                    case PENDING_TAKEAWAY:{
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Are you sure")
                                .setMessage("This will reject all the pending takeaways below.")
                                .setPositiveButton("Reject takeaways", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LinkedList<String> sessionIds = new LinkedList<String>();
                                        for(int i=0; i<sessionAdapter.getCount(); i++){
                                            sessionIds.add(sessionAdapter.getItem(i).getId());
                                        }
                                        rejectTakeaways(sessionIds);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                    }
                    case RESERVATION:{
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Are you sure")
                                .setMessage(getString(R.string.endofday_close_takeaways_message))
                                .setPositiveButton("Close Reservations", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LinkedList<String> reservationIds = new LinkedList<String>();
                                        for(int i=0; i<reservationAdapter.getCount(); i++){
                                            reservationIds.add(reservationAdapter.getItem(i).getId());
                                        }
                                        closeReservations(reservationIds);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                    }
                    case PENDING_RESERVATION:{
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Are you sure")
                                .setMessage("This will reject all the pending reservations below")
                                .setPositiveButton("Reject Reservations", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LinkedList<String> reservationIds = new LinkedList<String>();
                                        for(int i=0; i<reservationAdapter.getCount(); i++){
                                            reservationIds.add(reservationAdapter.getItem(i).getId());
                                        }
                                        rejectReservations(reservationIds);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private LoaderManager.LoaderCallbacks<? extends Object> sessionLoaderCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriSessionDetail>>() {
        @Override
        public Loader<ArrayList<EpicuriSessionDetail>> onCreateLoader(int id, Bundle args) {
            return new OneOffLoader(getActivity(), new SessionsLoaderTemplate());
        }
        @Override
        public void onLoadFinished(Loader<ArrayList<EpicuriSessionDetail>> loader,
                                   ArrayList<EpicuriSessionDetail> result) {
            Calendar now = Calendar.getInstance();
            Calendar event = Calendar.getInstance();
            ArrayList<EpicuriSessionDetail> sessions = new ArrayList<EpicuriSessionDetail>();
            for(EpicuriSessionDetail s: result){
                if(s.getType() == EpicuriSessionDetail.SessionType.DINE){
                    sessions.add(s);
                } else {
                    event.setTime(s.getExpectedTime());
                    boolean inThePast = true;
                    for(int i: new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR}){
                        int eventVal = event.get(i);
                        int nowVal = now.get(i);
                        if(eventVal < nowVal){
                            inThePast = true;
                            break;
                        } else if(eventVal > nowVal){
                            inThePast = false;
                            break;
                        }
                    }
                    if(inThePast) sessions.add(s);
                }
            }
            setListAdapter(sessionAdapter = new CloseSessionAdapter(getActivity(), sessions, currentType));
        }
        @Override
        public void onLoaderReset(Loader<ArrayList<EpicuriSessionDetail>> loader) {

        }
    };

    LoaderManager.LoaderCallbacks<ArrayList<EpicuriSessionDetail>> takeawayLoaderCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriSessionDetail>>() {
        @Override
        public Loader<ArrayList<EpicuriSessionDetail>> onCreateLoader(int id, Bundle args) {
            return new OneOffLoader<ArrayList<EpicuriSessionDetail>>(getActivity(), new TakeawaysLoaderTemplate());
        }
        @Override
        public void onLoadFinished(Loader<ArrayList<EpicuriSessionDetail>> loader,
                ArrayList<EpicuriSessionDetail> result) {
            Calendar now = Calendar.getInstance();
            Calendar event = Calendar.getInstance();
            ArrayList<EpicuriSessionDetail> sessions = new ArrayList<EpicuriSessionDetail>();
            for(EpicuriSessionDetail s: result){
                if(s.getType() == EpicuriSessionDetail.SessionType.DINE){
                    sessions.add(s);
                } else {
                    event.setTime(s.getExpectedTime());
                    boolean inThePast = true;
                    for(int i: new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR}){
                        int eventVal = event.get(i);
                        int nowVal = now.get(i);
                        if(eventVal < nowVal){
                            inThePast = true;
                            break;
                        } else if(eventVal > nowVal){
                            inThePast = false;
                            break;
                        }
                    }
                    if(inThePast) sessions.add(s);
                }
            }

        //    CloseSessionAdapter adapter = new CloseSessionAdapter(getActivity(), sessions, currentType);

            setListAdapter(sessionAdapter = new CloseSessionAdapter(getActivity(), sessions, currentType));
        }
        @Override
        public void onLoaderReset(Loader<ArrayList<EpicuriSessionDetail>> loader) {

        }
    };


    private LoaderManager.LoaderCallbacks<? extends Object> reservationLoaderCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriReservation>>() {
        @Override
        public Loader<ArrayList<EpicuriReservation>> onCreateLoader(int id, Bundle args) {
            Date beginningOfTime = new Date(0);
            Calendar endOfToday = Calendar.getInstance();
            endOfToday.set(Calendar.HOUR, 23);
            endOfToday.set(Calendar.MINUTE, 59);
            endOfToday.set(Calendar.SECOND, 59);
            endOfToday.set(Calendar.MILLISECOND, 99);
            return new OneOffLoader<ArrayList<EpicuriReservation>>(getActivity(), new ReservationsLoaderTemplate(beginningOfTime, endOfToday.getTime()));
        }
        @Override
        public void onLoadFinished(Loader<ArrayList<EpicuriReservation>> loader,
                                   ArrayList<EpicuriReservation> result) {
            if(null == result) return;
            Calendar now = Calendar.getInstance();
            Calendar event = Calendar.getInstance();
            boolean onlyAccepted = currentType == Type.RESERVATION;

            ArrayList<EpicuriReservation> reservations = new ArrayList<EpicuriReservation>();
            for(EpicuriReservation r: result){
                // if reservation is not deleted and the party hasn't arrived
                if(!r.isDeleted() && r.getArrivedTime() == null && ( onlyAccepted == r.isAccepted())){
                    reservations.add(r);
                }
            }
            setListAdapter(reservationAdapter = new CloseReservationAdapter(getActivity(), reservations, onlyAccepted));
        }
        @Override
        public void onLoaderReset(Loader<ArrayList<EpicuriReservation>> loader) {

        }
    };

    private void closeSessions(final LinkedList<String> sessionsToClose){
        if(sessionsToClose.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Sessions Closed")
                    .setMessage("The sessions have been closed")
                    .setPositiveButton("Dismiss", null)
                    .show();
            reloadData();
            return;
        }
        // take the first item
        String sessionId = sessionsToClose.pop();
        WebServiceCall call = new CloseSessionWebServiceCall(sessionId, true);

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Closing session " + sessionId);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if(code >= 200 && code < 300){
                    // close the rest of the items
                    closeSessions(sessionsToClose);
                }
            }
        });
        task.execute();
    }

    private void rejectTakeaways(final LinkedList<String> takeawaysToReject){
        if(takeawaysToReject.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Takeaways rejected")
                    .setMessage("The takeaways have been rejected")
                    .setPositiveButton("Dismiss", null)
                    .show();
            reloadData();
            return;
        }

        // take the first item
        String sessionId = takeawaysToReject.pop();
        WebServiceCall call = new RejectTakeawayWebServiceCall(sessionId, "Takeaway Rejected");

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Rejecting Takeaway " + sessionId);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if(code >= 200 && code < 300){
                    // close the rest of the items
                    rejectTakeaways(takeawaysToReject);
                }
            }
        });
        task.execute();
    }

    private void closeReservations(final LinkedList<String> reservationsToClose){
        if(reservationsToClose.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Reservations Closed")
                    .setMessage("The reservations have been closed")
                    .setPositiveButton("Dismiss", null)
                    .show();
            reloadData();
            return;
        }
        // take the first item
        String reservationId = reservationsToClose.pop();

        WebServiceCall call = new DeleteReservationWebServiceCall(reservationId, true);

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Closing Reservation " + reservationId);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if(code >= 200 && code < 300){
                    // close the rest of the items
                    closeReservations(reservationsToClose);
                }
            }
        });
        task.execute();
    }

    private void rejectReservations(final LinkedList<String> reservationsToReject){
        if(reservationsToReject.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Reservations Rejected")
                    .setMessage("The reservations have been rejected")
                    .setPositiveButton("Dismiss", null)
                    .show();
            reloadData();
            return;
        }
        // take the first item
        String reservationId = reservationsToReject.pop();

        WebServiceCall call = new RejectReservationWebServiceCall(reservationId, "Reservation Rejected");

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Rejecting Reservation " + reservationId);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if(code >= 200 && code < 300){
                    // close the rest of the items
                    rejectReservations(reservationsToReject);
                }
            }
        });
        task.execute();
    }

//    static class CloseSessionAdapter extends ArrayAdapter<EpicuriSessionDetail> {
//        private final LayoutInflater inflater;
//        private final Type currentType;
//
//        public CloseSessionAdapter(Context context, List<EpicuriSessionDetail> objects, Type activeTab) {
//            super(context, android.R.layout.simple_list_item_1, objects);
//            inflater = LayoutInflater.from(context);
//            currentType = activeTab;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder vh;
//            if(null == convertView){
//                convertView = inflater.inflate(R.layout.row_endofdaysession, parent, false);
//                vh = new ViewHolder(convertView);
//                convertView.setTag(vh);
//            } else {
//                vh = (ViewHolder)convertView.getTag();
//            }
//
//            EpicuriSessionDetail session = getItem(position);
//            vh.title.setText(session.getName());
//
//            boolean pendingTakeaway = false;
//            switch(session.getType()){
//                case DINE:{
//                    if(session.isTab()){
//                        vh.sessionType.setImageResource(R.drawable.inbar_light);
//                    } else {
//                        vh.sessionType.setImageResource(R.drawable.diner_light);
//                    }
//                    vh.arriveDate.setText(LocalSettings.niceFormat(session.getStartTime()));
//                    break;
//                }
//                case COLLECTION: {
//                    vh.sessionType.setImageResource(R.drawable.forcollection_light);
//                    vh.arriveDate.setText(LocalSettings.niceFormat(session.getExpectedTime()));
//                    break;
//                }
//                case DELIVERY: {
//                    vh.sessionType.setImageResource(R.drawable.forcollection_light);
//                    vh.arriveDate.setText(LocalSettings.niceFormat(session.getExpectedTime()));
//                    if(!session.isAccepted()){
//                        pendingTakeaway = true;
//                    }
//                    break;
//                }
//            }
//            String paidString = session.isPaid() ? " (Paid)" : "";
//            vh.cost.setText(LocalSettings.formatMoneyAmount(session.getTotal(), true) + paidString);
//
//            vh.id = session.getId().hashCode();
//
//            return convertView;
//        }
//
//
//        static class ViewHolder{
//            @InjectView(R.id.title)
//            TextView title;
//            @InjectView(R.id.cost) TextView cost;
//            @InjectView(R.id.arriveTime) TextView arriveDate;
//            @InjectView(R.id.sessionType)
//            ImageView sessionType;
//            int id;
//            public ViewHolder(View view){
//                ButterKnife.inject(this, view);
//            }
//        }
//    }



    static class CloseReservationAdapter extends ArrayAdapter<EpicuriReservation>{
        private final LayoutInflater inflater;
        private final boolean accepted;

        public CloseReservationAdapter(Context context, List<EpicuriReservation> objects, boolean accepted) {
            super(context, android.R.layout.simple_list_item_1, objects);
            inflater = LayoutInflater.from(context);
            this.accepted = accepted;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if(null == convertView){
                convertView = inflater.inflate(R.layout.row_endofdaysession, parent, false);
                vh = new ViewHolder(convertView);

                vh.cost.setVisibility(View.GONE);
                vh.sessionType.setImageResource(R.drawable.reservation_light);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder)convertView.getTag();
            }

            EpicuriReservation res = getItem(position);
            vh.title.setText(res.getName());
            vh.arriveDate.setText(LocalSettings.niceFormat(res.getStartDate()));

            vh.id = res.getId().hashCode();

            return convertView;
        }


        static class ViewHolder{
            @InjectView(R.id.title) TextView title;
            @InjectView(R.id.cost) TextView cost;
            @InjectView(R.id.arriveTime) TextView arriveDate;
            @InjectView(R.id.sessionType) ImageView sessionType;
            int id;
            public ViewHolder(View view){
                ButterKnife.inject(this, view);
            }
        }
    }
}
