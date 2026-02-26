package uk.co.epicuri.waiter.ui;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import butterknife.ButterKnife;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.ReservationsAdapter;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.AddReservationListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.ReservationsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.AcceptReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.MarkReservationArrivedWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class ReservationsListFragment extends android.support.v4.app.ListFragment implements
        OnClickListener,
        LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriReservation>>> {

    private static final int LOADER_RESERVATIONS = 1;
    private static final int LOADER_PENDING_RESERVATIONS = 2;

    private AddReservationListener listener;

    private Button currentDateButton;
    private ReservationsAdapter adapter;

    private ArrayList<EpicuriReservation> reservations = null;
    private ArrayList<EpicuriReservation> pendingReservations;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("E d MMM yyyy", Locale.UK);
    private Calendar now = Calendar.getInstance();

    private ActionMode mMode;
    private EditText rejectMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (AddReservationListener) getActivity();
        setHasOptionsMenu(true);
        if (null != savedInstanceState) {
            now.setTimeInMillis(savedInstanceState.getLong("now"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("now", now.getTime().getTime());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        EpicuriReservation newReservationSelected = reservations.get(position);
        if (newReservationSelected.equals(activeReservation)) {
            // "double click"
            if (activeReservation.getSessionId() != null
                    && !activeReservation.getSessionId().equals("-1")
                    && !activeReservation.getSessionId().equals("0")) {
                viewSession(activeReservation);
                mMode.finish();
            } else if (!activeReservation.isDeleted()
                    & activeReservation.getArrivedTime() == null) {
                // edit
                listener.editReservation(activeReservation);
                mMode.finish();
            }
            return;
        }
        activeReservation = newReservationSelected;
        fireActionmode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.datefilterlist, container, false);
        ButterKnife.inject(this, view);

        ListView listview = (ListView) view.findViewById(android.R.id.list);
        listview.setAdapter(adapter = new ReservationsAdapter(getActivity()));
//        listview.setEmptyView(view.findViewById(android.R.id.empty));
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        for (int resId : new int[]{R.id.today, R.id.date_back, R.id.date_forward}) {
            View button = view.findViewById(resId);
            button.setOnClickListener(this);
        }
        currentDateButton = (Button) view.findViewById(R.id.date_now);
        currentDateButton.setOnClickListener(this);

        getLoaderManager().initLoader(LOADER_PENDING_RESERVATIONS, null,
                new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriReservation>>>() {

                    @Override
                    public Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> onCreateLoader(
                            int id, Bundle arguments) {
                        EpicuriLoader loader = new EpicuriLoader<>(getActivity(),
                                new ReservationsLoaderTemplate());
                        loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                        return loader;
                    }

                    @Override
                    public void onLoadFinished(
                            Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> loader,
                            LoaderWrapper<ArrayList<EpicuriReservation>> data) {
                        if (null == data) {
                            pendingReservations = null;
                            getActivity().invalidateOptionsMenu();
                            return;
                        } else if (data.isError()) {
                            Toast.makeText(getActivity(),
                                    "ReeservationsListFragment error loading data",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pendingReservations = data.getPayload();
                        // sort pending takeaways
                        Collections.sort(pendingReservations, new Comparator<EpicuriReservation>() {
                            @Override
                            public int compare(EpicuriReservation lhs,
                                    EpicuriReservation rhs) {
                                return lhs.getStartDate().compareTo(rhs.getStartDate());
                            }
                        });
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onLoaderReset(
                            Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> data) {
                    }

                });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateDateUI();
    }

    @Override
    public Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> onCreateLoader(int id,
            Bundle args) {
        Calendar startOfTOday = Calendar.getInstance(now.getTimeZone());
        startOfTOday.clear();
        startOfTOday.set(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), 0, 0);
        Date startTime = startOfTOday.getTime();
        startOfTOday.add(Calendar.DAY_OF_MONTH, 1);
        Date endTime = startOfTOday.getTime();

        EpicuriLoader loader = new EpicuriLoader<ArrayList<EpicuriReservation>>(getActivity(),
                new ReservationsLoaderTemplate(startTime, endTime));
        loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> loader,
            LoaderWrapper<ArrayList<EpicuriReservation>> data) {
        if (null == data) { // nothing returned, ignore
            return;
        } else if (data.isError()) {
            Toast.makeText(getActivity(), "error loading data", Toast.LENGTH_SHORT).show();
            return;
        }
        reservations = data.getPayload();
        adapter.setState(reservations);

        activeReservation = null;
        if (preselectItemId != null && !preselectItemId.equals("-1") && !preselectItemId.equals(
                "0")) {
            for (int i = 0; i < reservations.size(); i++) {
                EpicuriReservation r = reservations.get(i);
                if (r.getId() != null && !r.getId().equals("-1") && r.getId().equals(
                        preselectItemId)) {
                    activeReservation = r;
                    getListView().setItemChecked(i, true);
                    break;
                }
            }
        }
        fireActionmode();
    }

    @Override
    public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> data) {
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.today: {
                now = Calendar.getInstance();
                break;
            }
            case R.id.date_now: {
                OnDateSetListener dateSetListener = new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        now.clear();
                        now.set(year, monthOfYear, dayOfMonth);
                        updateDateUI();
                    }
                };
                new DatePickerDialog(getActivity(), dateSetListener, now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
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
        updateDateUI();
    }

    /** true if the calendar is showing today */
    private boolean isToday = false;

    /**
     * update UI to show the specified date
     */
    public void setDate(Date date) {
        now.setTime(date);
        if (isVisible()) {
            updateDateUI();
        }
    }

    private void updateDateUI() {
        adapter.setState(null);
        currentDateButton.setText(dateFormat.format(now.getTime()));

        Calendar today = Calendar.getInstance();
        // if this view is visible, then update the UI
        isToday = now.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                && now.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && now.get(Calendar.YEAR) == today.get(Calendar.YEAR);
        getView().findViewById(R.id.today).setEnabled(!isToday);
        getLoaderManager().restartLoader(LOADER_RESERVATIONS, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_reservations, menu);

        MenuItem pendingMenuItem = menu.findItem(R.id.menu_pending);
        View pendingActionView = pendingMenuItem.getActionView();
        ((ImageView) pendingActionView.findViewById(R.id.icon)).setImageResource(
                R.drawable.ic_action_reservations);
        TextView pendingActionViewText = ((TextView) pendingActionView.findViewById(
                R.id.count_text));
        pendingActionViewText.setVisibility(View.VISIBLE);
        pendingActionView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPending();
            }
        });


        List<EpicuriReservation> pending = new ArrayList<>(1);
        Date now = new Date();

        if (pendingReservations != null) {
            for (EpicuriReservation reservation : pendingReservations) {
                if ((reservation.getArrivedTime() != null && reservation.getArrivedTime().before
                        (now)) || reservation.isDeleted())
                continue;

                pending.add(reservation);
            }
        }

        if (pending.isEmpty()) {
            pendingMenuItem.setVisible(false);
        } else {
            pendingMenuItem.setVisible(true);
            pendingActionViewText.setText(String.valueOf(pending.size()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add: {
                Calendar currentTime = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY));
                now.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE));
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                listener.addReservation(now);
                return true;
            }
            case R.id.menu_refresh: {
                UpdateService.requestUpdate(getActivity(),
                        ((EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(
                                LOADER_RESERVATIONS)).getContentUri());
                UpdateService.requestUpdate(getActivity(),
                        ((EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(
                                LOADER_PENDING_RESERVATIONS)).getContentUri());
                return true;
            }
            case R.id.menu_pending: {
                showPending();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    String preselectItemId = "0";

    private void showPending() {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        final ArrayList<EpicuriReservation> chosenReservations = new ArrayList<EpicuriReservation>(
                pendingReservations.size());
        chosenReservations.addAll(pendingReservations);

        ArrayAdapter<EpicuriReservation> reservationsAdapter = new ArrayAdapter<EpicuriReservation>(
                getActivity(), android.R.layout.simple_list_item_2, chosenReservations) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    convertView = inflater.inflate(
                            android.R.layout.simple_list_item_2, parent, false);
                }
                EpicuriReservation res = getItem(position);
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(res.getName());
                ((TextView) convertView.findViewById(android.R.id.text2)).setText(
                        "For " + SimpleDateFormat.getDateInstance().format(res.getStartDate()));
                return convertView;
            }

        };
        new AlertDialog.Builder(getActivity())
                .setTitle("Reservations Pending Approval")
                .setAdapter(reservationsAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EpicuriReservation res = chosenReservations.get(which);

                                now.clear();
                                now.setTime(res.getStartDate());
                                updateDateUI();
                                preselectItemId = res.getId();
                                dialog.dismiss();
                            }
                        }).show();
    }

    private void acceptReservation(String reservationId) {
        AcceptReservationWebServiceCall call = new AcceptReservationWebServiceCall(reservationId);
        WebServiceTask task = new WebServiceTask(getActivity(), call);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                UpdateService.expireData(getActivity(),
                        new Uri[]{EpicuriContent.RESERVATIONS_URI.buildUpon().appendQueryParameter(
                                "pendingWaiterAction", "true").build()});
                for (int loaderId : new int[]{LOADER_RESERVATIONS, LOADER_PENDING_RESERVATIONS}) {
                    EpicuriLoader<?> l =
                            (EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(loaderId);
                    UpdateService.requestUpdate(getActivity(), l.getContentUri());
                }
            }
        });

        task.execute();
    }

    private void rejectReservation(String reservationId, CharSequence message) {
        RejectReservationWebServiceCall call = new RejectReservationWebServiceCall(reservationId,
                message);
        WebServiceTask task = new WebServiceTask(getActivity(), call);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                UpdateService.expireData(getActivity(),
                        new Uri[]{EpicuriContent.RESERVATIONS_URI.buildUpon().appendQueryParameter(
                                "pendingWaiterAction", "true").build()});
                for (int loaderId : new int[]{LOADER_RESERVATIONS, LOADER_PENDING_RESERVATIONS}) {
                    EpicuriLoader<?> l =
                            (EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(loaderId);
                    UpdateService.requestUpdate(getActivity(), l.getContentUri());
                }
            }
        });

        task.execute();
    }

    private void deleteReservation(String id, boolean withBlackMark) {
        WebServiceCall call = new DeleteReservationWebServiceCall(id, withBlackMark);
        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                EpicuriLoader<?> l = (EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(
                        LOADER_RESERVATIONS);
                UpdateService.requestUpdate(getActivity(), l.getContentUri());
            }
        });

        task.execute();
    }

    private void markAsArrived(EpicuriReservation reservation) {
        WebServiceCall call = new MarkReservationArrivedWebServiceCall(reservation.getId());

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                EpicuriLoader<?> l = (EpicuriLoader<?>) (Loader<?>) getLoaderManager().getLoader(
                        LOADER_RESERVATIONS);
                UpdateService.requestUpdate(getActivity(), l.getContentUri());

                // go to the hub activity, unseated parties tab
                Intent intent = new Intent(getActivity(), HubActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(GlobalSettings.EXTRA_TAB, HubActivity.TAB_UNSEATED);
                startActivity(intent);
            }
        });
        task.execute();
    }

    private void fireActionmode() {
        if (null == activeReservation) {
            if (null == mMode) return;
            mMode.finish();
        } else {
            if (null == mMode) {
                mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(
                        new ReservationActionMode());
                // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
                if (null != mMode) mMode.invalidate();
            } else {
                mMode.invalidate();
            }
        }
    }

    private EpicuriReservation activeReservation;

    private class ReservationActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (activeReservation.isDeleted()) {
                Toast.makeText(getActivity(), "This reservation has been cancelled",
                        Toast.LENGTH_SHORT).show();
                activeReservation = null;
                getListView().clearChoices();
                preselectItemId = "0";

                // bug in angroid http://stackoverflow
                // .com/questions/9754170/listview-selection-remains-persistent-after-exiting
                // -choice-mode
                getListView().requestLayout();
                return false;
            }

            mode.getMenuInflater().inflate(R.menu.action_reservation, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (activeReservation.isDeleted()) {
                Toast.makeText(getActivity(), "This reservation has been cancelled",
                        Toast.LENGTH_SHORT).show();
                mode.finish();

                return true;
            }
            boolean hasSession = activeReservation.getSessionId() != null && !(
                    activeReservation.getSessionId().equals("0")
                            || activeReservation.getSessionId().equals("-1"));
            if (hasSession) {
                menu.findItem(R.id.menu_viewsession).setVisible(true);
                menu.setGroupVisible(R.id.menugroup_notinsession, false);
                menu.setGroupVisible(R.id.menugroup_notaccepted, false);
                menu.setGroupVisible(R.id.menugroup_notarrived, false);
            } else if (activeReservation.getArrivedTime() != null) {
                menu.findItem(R.id.menu_viewsession).setVisible(false);
                menu.setGroupVisible(R.id.menugroup_notinsession, false);
                menu.setGroupVisible(R.id.menugroup_notaccepted, false);
                menu.setGroupVisible(R.id.menugroup_notarrived, false);
            } else {
                menu.findItem(R.id.menu_viewsession).setVisible(false);
                menu.setGroupVisible(R.id.menugroup_notinsession, true);
                menu.setGroupVisible(R.id.menugroup_notaccepted, !activeReservation.isAccepted());
                menu.setGroupVisible(R.id.menugroup_notarrived,
                        isToday && activeReservation.isAccepted());
                menu.findItem(R.id.menu_delete).setVisible(
                        activeReservation.isAccepted()); // hide "cancel" if not accepted yet
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_viewsession: {
                    viewSession(activeReservation);
                    mode.finish();
                    return true;
                }
                case R.id.menu_accept: {
                    final String acceptId = activeReservation.getId();

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.acceptReservation_title)

                            .setMessage(getString(R.string.acceptReservation_message,
                                    activeReservation.getName(),
                                    LocalSettings.getDateFormatWithDate().format(
                                            activeReservation.getStartDate())))
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    acceptReservation(acceptId);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    mode.finish();
                    return true;
                }
                case R.id.menu_reject: {
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edittext,
                            null, false);
                    //			ButterKnife.inject(this, view);
                    rejectMessage = (EditText) view.findViewById(R.id.text1);
                    rejectMessage.setSelectAllOnFocus(true);
                    rejectMessage.setHint("Message to send guest about rejection");
                    rejectMessage.setText(activeReservation.getRejectedReason());
                    final String rejectId = activeReservation.getId();
                    new AlertDialog.Builder(getActivity()).setTitle(
                            "Reject reservation with message")
                            .setView(view)
                            .setPositiveButton("Reject", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    rejectReservation(rejectId, rejectMessage.getText());
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    mode.finish();
                    return true;
                }
                case R.id.menu_edit: {
                    listener.editReservation(activeReservation);
                    mode.finish();
                    return true;
                }
                case R.id.menu_delete: {
                    final String deleteId = activeReservation.getId();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Cancel Reservation")
                            .setMessage(
                                    getString(R.string.cancel_reservation,
                                            activeReservation.getName(),
                                            LocalSettings.niceFormat(
                                                    activeReservation.getStartDate())
                                    ))
                            .setPositiveButton("Cancel Reservation",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteReservation(deleteId, false);
                                        }
                                    }).setNegativeButton("Do Nothing", null).show();
                    mode.finish();
                    return true;
                }
                case R.id.menu_notarrived: {
                    final String deleteId = activeReservation.getId();
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.noshow_title)
                            .setMessage(
                                    getString(R.string.noshow_body, activeReservation.getName()))
                            .setPositiveButton(R.string.noshow_action,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteReservation(deleteId, true);
                                        }
                                    })
                            .setNegativeButton(R.string.noshow_cancel, null)
                            .show();
                    mode.finish();
                    return true;
                }
                case R.id.menu_arrived: {
                    markAsArrived(activeReservation);
                    mode.finish();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;
            activeReservation = null;
            getListView().clearChoices();
            preselectItemId = "0";

            // bug in angroid http://stackoverflow
            // .com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
            getListView().requestLayout();
        }

    }

    private void viewSession(EpicuriReservation reservation) {
        Intent launchSession = new Intent(getActivity(), SeatedSessionActivity.class);
        launchSession.putExtra(GlobalSettings.EXTRA_SESSION_ID, reservation.getSessionId());
        startActivity(launchSession);
    }
}

