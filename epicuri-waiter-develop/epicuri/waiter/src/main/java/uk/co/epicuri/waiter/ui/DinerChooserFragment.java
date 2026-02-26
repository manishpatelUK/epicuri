package uk.co.epicuri.waiter.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CustomDialogAdapter;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.DinerActionHandler;
import uk.co.epicuri.waiter.interfaces.OnDinerClickListener;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.Printable;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.CourseLoaderTemplate;
import uk.co.epicuri.waiter.model.CourseAwayMessage;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.printing.PrintUtil;
import uk.co.epicuri.waiter.printing.SendEmailHandlerImpl;
import uk.co.epicuri.waiter.ui.menueditor.PartyDetailsFragment;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CourseAwayWebServiceCall;
import uk.co.epicuri.waiter.webservice.CustomerOnDinerWebServiceCall;
import uk.co.epicuri.waiter.webservice.DetachCustomerFromDinerWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetPrintersWebServiceCall;
import uk.co.epicuri.waiter.webservice.SaveTableSeatingWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.ui.LoginActivity.KEY_LOGIN_USERNAME;
import static uk.co.epicuri.waiter.ui.LoginActivity.RESTAURANT_PREFS;


public class DinerChooserFragment extends Fragment implements OnDinerClickListener,
        OnSessionChangeListener {

    private static final String FRAGMENT_DINER_DETAILS = "DinerDetails";
    private static final String FRAGMENT_CUSTOMER_CHOOSER = "CustomerChooser";
    private static final int LOADER_COURSES = 8;

    public static EpicuriSessionDetail session;
    private Diner selectedDiner = null;

    @InjectView(R.id.dinerName)
    TextView dinerName;
    @InjectView(R.id.dinerNameContainer)
    View dinerNameContainer;
    @InjectView(R.id.edit_img)
    ImageView editImg;
    @InjectView(R.id.sessionOrDiner)
    ViewAnimator sessionOrDinerFlipper;
    @InjectView(R.id.tableClicker)
    TableSeatingView tableView;
    @InjectView(R.id.dinerDetailBlock)
    View dinerDetailBlock;
    @InjectView(R.id.dinerFavouriteFood_text)
    TextView dinerFavouriteFood;
    @InjectView(R.id.dinerFavouriteDrink_text)
    TextView dinerFavouriteDrink;
    @InjectView(R.id.dinerHatedFood_text)
    TextView dinerHatedFood;
    @InjectView(R.id.dinerBirthday)
    ImageView birthdayImage;
    @InjectView(R.id.courseAway)
    Button courseAway;

    private ActionMode dinerSelectionActionMode;
    private boolean enableDinerActionMode = true;
    private DinerActionHandler listener;

    final ArrayList<EpicuriMenu.Course> courses = new ArrayList<>();

    public interface IBillSplitHandler {
        void onConfirmChanges();

        void onDeselectAll();

        void ordersToTab();

        void printDinerBill();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((SessionContainer) context).registerSessionListener(this);
        if (context instanceof DinerActionHandler) {
            listener = (DinerActionHandler) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((SessionContainer) getActivity()).registerSessionListener(this);
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dinerchooser, container, false);
        ButterKnife.inject(this, v);
        final FragmentActivity activity = DinerChooserFragment.this.getActivity();

        if (null != session) {
            EpicuriTable.Shape tableShape = null;
            if (null != session.getTables() && !session.isTab()) {
                tableShape = session.getTables()[0].getShape();
            }
            tableView.setLayout(session.getTableLayout(), session.getDiners(), tableShape);
            updateView(session, v);
            if (session.getTables().length == 0) {
                tableView.setVisibility(View.GONE);
            } else {
                tableView.setVisibility(View.VISIBLE);
            }

            triggerGetCourses(activity);
        }
        tableView.selectDiner(selectedDiner);
        tableView.forceSelect(!enableDinerActionMode);
        tableView.setOnTableChangeListener(this);

        courseAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(courses.size() == 0) {
                    Toast.makeText(DinerChooserFragment.this.getActivity(), "Courses not loaded yet. Try again in a few seconds", Toast.LENGTH_SHORT).show();
                    triggerGetCourses(DinerChooserFragment.this.getActivity());
                }
                if(courses.size() == 1){
                    printCourse(String.format(getString(R.string.course_name), courses.get(0).getName()));
                }
                final String[] coursesArray = new String[courses.size()];
                for (int i = 0; i < coursesArray.length; i++){
                    StringBuilder stringBuilder = new StringBuilder();
                    EpicuriMenu.Course course = courses.get(i);
                    stringBuilder.append(course.getName());
                    if(session.getCourseAwayMessagesSent().get(course.getId()) != null){
                        int messagesCount = session.getCourseAwayMessagesSent().get(course.getId());
                        if(messagesCount > 0)
                            stringBuilder.append(" ✔");
                    }
                    coursesArray[i] = stringBuilder.toString();
                }
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                View view = getLayoutInflater().inflate(R.layout.dialog_custom_select, null);
                ((TextView) view.findViewById(R.id.title)).setText("Select course");
                ListView listView = view.findViewById(R.id.customDialogList);
                CustomDialogAdapter adapter = new CustomDialogAdapter(getContext(), coursesArray);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String courseName = String.format(getString(R.string.course_name), courses.get(position).getName());
                        printCourse(courseName);
                        EpicuriMenu.Course course = courses.get(position);

                        WebServiceCall call = new CourseAwayWebServiceCall(course.getId(), session.getId());
                        WebServiceTask task = new WebServiceTask(getContext(), call);
                        task.setIndicatorText("Sending course away...");
                        task.execute();
                        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                            @Override
                            public void onSuccess(int code, String response) {
                                if(getContext() != null && session != null){
                                    Uri uri = EpicuriContent.SESSION_URI.buildUpon().appendEncodedPath(session.getId()).build();
                                    UpdateService.requestUpdate(getContext(), uri);
                                }

                            }
                        });
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(view);
                dialog.show();
            }
        });

        updateDetail();
        return v;
    }

    private void triggerGetCourses(final FragmentActivity activity) {
        if(activity == null) {
            return;
        }

        activity.getSupportLoaderManager().initLoader(LOADER_COURSES, null,
                new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Course>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> onCreateLoader(int id, Bundle args) {
                String serviceId = null;
                if(session != null) serviceId = session.getServiceId();

                return new EpicuriLoader<>(activity, new CourseLoaderTemplate(serviceId));
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriMenu.Course>> data) {
                if(null == data){
                    return;
                }else if(data.isError()){
                    Toast.makeText(activity, "error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                courses.clear();
                for(EpicuriMenu.Course c: data.getPayload()) {
                    if (c.getServiceId().equals(session.getServiceId()) && !c.getName().equals("ASAP")) {
                        courses.add(c);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader) {
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(session.isBillRequested()){
            courseAway.setVisibility(View.GONE);
        } else {
            courseAway.setVisibility(View.VISIBLE);
        }
    }

    private void printCourse(String courseName){
        SharedPreferences prefs = getActivity().getSharedPreferences(RESTAURANT_PREFS,
                Context.MODE_PRIVATE);
        final EpicuriRestaurant restaurant = LocalSettings.getInstance(getContext()).getCachedRestaurant();
        final Printable printable = new CourseAwayMessage(courseName, session, prefs.getString(KEY_LOGIN_USERNAME, ""));
        WebServiceTask task = new WebServiceTask(getContext(), new GetPrintersWebServiceCall());
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try{
                    JSONArray responseJson = new JSONArray(response);
                    for (int i = 0; i < responseJson.length(); i++){
                        EpicuriMenu.Printer printer =  new EpicuriMenu.Printer(responseJson.getJSONObject(i));
                        if (printer.getId().equals(restaurant.getDefaultCourseAwayPrinterId())) {
                            PrintUtil.print(
                                    getContext(),
                                    printer.getIpAddress(),
                                    printable,
                                    null);
                            Toast.makeText(getContext(), "Course away sent", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }catch (JSONException e){
                    Toast.makeText(getContext(), "Cannot get printers - offline", Toast.LENGTH_SHORT).show();
                }
            }
        });
        task.execute();
    }

    @Override
    public void onSessionChanged(EpicuriSessionDetail session) {
        this.session = session;
        if (selectedDiner != null) selectedDiner = session.getDinerFromId(selectedDiner.getId());
        updateView(session, getView());
    }

    private void updateView(EpicuriSessionDetail session, View view) {
        if (courseAway != null) {
            if (session.isBillRequested()) {
                courseAway.setVisibility(View.GONE);
            } else {
                courseAway.setVisibility(View.VISIBLE);
            }
        }
        if (null != tableView) {
            EpicuriTable.Shape tableShape = null;
            if (null != session.getTables() && !session.isTab()) {
                tableShape = session.getTables()[0].getShape();
            }
            if (!tableView.isEditMode()) {
                // if not in editmode, refresh table layout
                tableView.setLayout(session.getTableLayout(), session.getDiners(), tableShape);
            }
        }
        Activity fa = getActivity();
        if (null != fa) {
            // possibly null if going from refresh to add itemsd
            fa.invalidateOptionsMenu();
        }

        if (null == view) return;

        updateUI(view);
    }

    private void updateUI(View view) {

        TextView tv;
        StringBuilder sb;

        tv = (TextView) view.findViewById(R.id.sessionName);
        sb = new StringBuilder(session.getName());
        if (session.isVoided()) {
            sb.append(" - VOIDED");
        } else if (session.isClosed() && !session.isPaid()) {
            sb.append(" - FORCE CLOSED");
        } else if (session.isClosed()){
            sb.append(" - CLOSED");
        }

        tv.setText(sb);
        tv = (TextView) view.findViewById(R.id.partySizeAndTable);
        if (session.isVoided() && session.getVoidReason() != null) {
            tv.setText(session.getVoidReason());
        } else {
            tv.setText(
                    String.format(Locale.UK, "Party of %d on Table: %s", session.getNumberInParty(),
                            session.getTablesString()));
        }
        if (session.getTables().length == 0) {
            tableView.setVisibility(View.GONE);
        } else {
            tableView.setVisibility(View.VISIBLE);
        }
        boolean hasBirthday = false;
        for (EpicuriSessionDetail.Diner d : session.getDiners()) {
            if (d.isBirthday()) {
                hasBirthday = true;
                break;
            }
        }
        view.findViewById(R.id.ic_birthday).setVisibility(hasBirthday ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.birthday).setVisibility(hasBirthday ? View.VISIBLE : View.GONE);

        ImageView iv = (ImageView) view.findViewById(R.id.ic_seated);
        final int visibilityOfService;
        if (session.isTab()) {
            iv.setImageResource(R.drawable.inbar);
            visibilityOfService = View.INVISIBLE;
        } else {
            iv.setImageResource(R.drawable.number_in_party);

            tv = (TextView) view.findViewById(R.id.actionDue);
            long lag = session.getLag();
            if (null != session.getNextScheduleItemDue()) {
                long extraOffset = new Date().getTime()
                        - session.getNextScheduleItemDue().getTime(); //TODO: possible issues
                // with BST
                if (extraOffset > 0) {
                    // if next item is overdue, then add the extra delay to the lag
                    lag += extraOffset;
                }
            }
            tv.setText(GlobalSettings.minsLate(lag));

            tv = (TextView) view.findViewById(R.id.service);
            tv.setText(session.getCourse());
            visibilityOfService = View.VISIBLE;
        }
        for (int viewId : new int[]{R.id.service, R.id.ic_service, R.id.actionDue,
                R.id.ic_actiondue}) {
            view.findViewById(viewId).setVisibility(visibilityOfService);
        }

        tv = (TextView) view.findViewById(R.id.seated);
        sb = new StringBuilder(
                LocalSettings.getDateFormatWithDate().format(session.getStartTime()));
        if (session.isClosed()) {
            sb.append(" - ").append(
                    LocalSettings.getDateFormatWithDate().format(session.getClosedTime()));
        }
        tv.setText(sb);

    }

    public void setDiner(Diner diner) {
        onDinerClick(diner);
    }

    public void setEnableDinerActionMode(boolean enableDinerActionMode) {
        this.enableDinerActionMode = enableDinerActionMode;
        if (null != tableView) {
            tableView.forceSelect(!enableDinerActionMode);
        }
    }

    private OnDinerClickListener externalListener;

    public void setOnDinerChangeListener(OnDinerClickListener listener) {
        externalListener = listener;
    }

    @Override
    public void onDinerClick(Diner diner) {
        selectedDiner = diner;

        if (null != externalListener) externalListener.onDinerClick(diner);
        if (null == session || !session.isClosed()) {
            if (null != dinerSelectionActionMode) {
                if (null == diner) {
                    dinerSelectionActionMode.finish();
                } else {
                    dinerSelectionActionMode.invalidate();
                }
            } else if (null != diner && enableDinerActionMode) {
                dinerSelectionActionMode =
                        ((AppCompatActivity) getActivity()).startSupportActionMode(
                                new DinerSelectionActionModeCallbacks());
                // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
                if (dinerSelectionActionMode != null) dinerSelectionActionMode.invalidate();
            }
        }
        if (isAdded()) {
            tableView.selectDiner(diner);
            updateDetail();
        }
    }

    private void updateDetail() {

        if (null == selectedDiner) {
            sessionOrDinerFlipper.setDisplayedChild(0);
            return;
        }
        sessionOrDinerFlipper.setDisplayedChild(1);
        final boolean isSelectedDinerEditable = selectedDiner.getEpicuriCustomer() == null && !selectedDiner.isTable();
        editImg.setVisibility(isSelectedDinerEditable? View.VISIBLE: View.GONE);

        dinerNameContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final EditText input = new EditText(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                if(getContext() == null) return;
                AlertDialog.Builder alertBuilder = new AlertDialog
                        .Builder(getContext())
                        .setTitle("Change name of guest")
                        .setView(input)
                        .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                final String newCustomerName = input.getText().toString();
                                final CustomerOnDinerWebServiceCall webServiceCall = new CustomerOnDinerWebServiceCall(selectedDiner, session.getId(), newCustomerName);
                                WebServiceTask task = new WebServiceTask(getActivity(), webServiceCall);
                                task.setIndicatorText("Saving changes");
                                task.execute();
                                task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                                    @Override public void onSuccess(int code, String response) {
                                        dinerName.setText(newCustomerName);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if(isSelectedDinerEditable){
                    alertBuilder.show();
                }
            }
        });
        if (null != selectedDiner.getEpicuriCustomer()) {
            final EpicuriCustomer customer = selectedDiner.getEpicuriCustomer();
            dinerName.setText(customer.getName());

            if (TextUtils.isEmpty(customer.getFavouriteDrink())) {
                dinerFavouriteDrink.setText("Favourite Drink: Not specified");
            } else {
                dinerFavouriteDrink.setText("Favourite Drink: " + customer.getFavouriteDrink());
            }

            final int visibility;
            if (session.isTab()) {
                // this is a tab, hide other fields
                visibility = View.GONE;
            } else {
                visibility = View.VISIBLE;
                if (TextUtils.isEmpty(customer.getFavouriteFood())) {
                    dinerFavouriteFood.setText("Favourite Food: Not specified");
                } else {
                    dinerFavouriteFood.setText("Favourite Food: " + customer.getFavouriteFood());
                }
                if (customer.getFoodPreferences().length == 0) {
                    dinerHatedFood.setText("Favourite Tastes: Not specified");
                } else {
                    StringBuilder sb = new StringBuilder("Favourite Tastes: ");
                    boolean comma = false;
                    for (String pref : customer.getFoodPreferences()) {
                        if (comma) sb.append(", ");
                        sb.append(pref);
                        comma = true;
                    }
                    dinerHatedFood.setText(sb);
                }
            }
            for (View v : new View[]{dinerHatedFood, dinerFavouriteFood}) {
                v.setVisibility(visibility);
            }

            birthdayImage.setVisibility(selectedDiner.isBirthday() ? View.VISIBLE : View.GONE);
            dinerDetailBlock.setVisibility(View.VISIBLE);
        } else {
            if (selectedDiner.isTable()) {
                dinerName.setText("For the table");
            } else {
                dinerName.setText(TextUtils.isEmpty(selectedDiner.getName()) ? "Guest" : selectedDiner.getName());
            }
            dinerDetailBlock.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (null == session) return;
        // can only add items to a seated session, not a takeaway
        if (getActivity() instanceof SeatedSessionActivity) {
            inflater.inflate(R.menu.fragment_dinerchooser, menu);
            // hide "edit table layout" once bill requested
            menu.findItem(R.id.menu_edit).setVisible(
                    !session.isBillRequested() && !session.isTab());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                tableView.setEditMode(true);
                mEditActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(
                        new EditTableLayoutActionModeCallbacks());
                // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
                if (null != mEditActionMode) mEditActionMode.invalidate();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (tableView.isEditMode()) {
            saveLayoutData(false);
        }
        super.onPause();
    }

    private void detachEpicuriAccount(Diner diner) {
        if (null == diner) return;
        WebServiceTask task = new WebServiceTask(getActivity(),
                new DetachCustomerFromDinerWebServiceCall(diner, session.getId()), true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void attachEpicuriAccount(Diner diner) {
        DialogFragment customerFrag = CheckinChooserFragment.newInstance(diner, session.getId());
        customerFrag.show(getFragmentManager(), FRAGMENT_CUSTOMER_CHOOSER);
    }

    private class EditTableLayoutActionModeCallbacks implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.action_dinerlayout, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_edit_fullscreen: {
                    Intent intent = new Intent(getActivity(), TableEditActivity.class);

                    intent.putExtra(TableEditActivity.EXTRA_FURNITURE, session.getTableLayout());
                    intent.putExtra(TableEditActivity.EXTRA_DINERS, session.getDiners());
                    if (session.getTables().length == 1) {
                        intent.putExtra(TableEditActivity.EXTRA_SHAPE,
                                session.getTables()[0].getShape().getId());
                    } else {
                        intent.putExtra(TableEditActivity.EXTRA_SHAPE,
                                EpicuriTable.Shape.SQUARE.getId());
                    }
                    intent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
                    startActivity(intent);
                    mEditActionMode.finish();
                    return true;
                }
                case R.id.menu_save: {
                    tableView.setEditMode(false);
                    saveLayoutData(true);
                    mEditActionMode.finish();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            tableView.setEditMode(false);
            saveLayoutData(true);
            mEditActionMode = null;
        }

    }

    private ActionMode mEditActionMode = null;

    private class DinerSelectionActionModeCallbacks implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            sessionOrDinerFlipper.setDisplayedChild(1);
            new MenuInflater(getActivity()).inflate(R.menu.action_dinerselected, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            boolean isDiner = null != selectedDiner && !selectedDiner.isTable();
            boolean isAttached = selectedDiner.getEpicuriCustomer() != null;
            menu.findItem(R.id.menu_send_email).setVisible(isDiner && LocalSettings.getInstance(getActivity()).getCachedRestaurant().isEmailReceiptsEnabled() && session.isBillRequested());
            menu.findItem(R.id.menu_dinerDetail).setVisible(
                    isDiner && isAttached && !session.isBillSplitMode());
            menu.findItem(R.id.menu_detachCustomer).setVisible(isDiner && isAttached);
            menu.findItem(R.id.menu_allocateCustomer).setVisible(isDiner && !isAttached &&
                    !session.isBillSplitMode());
            menu.findItem(R.id.menu_print).setVisible(session.isBillRequested() && isDiner);
            menu.findItem(R.id.menu_deselectall).setVisible(session.isBillSplitMode() && isDiner);
            menu.findItem(R.id.menu_pushnewsession).setVisible(
                    session.isBillSplitMode() &&
                    !session.isPaid() &&
                            session.getDiscountTotal().isZero() &&
                            session.getRemainingTotal().isEqual(session.getFudgedReceiptTotal()));
            menu.findItem(R.id.menu_confirm).setVisible(session.isBillSplitMode() &&
                    selectedDiner != null);
            menu.findItem(R.id.action_edit_session).setVisible(session.isBillSplitMode() &&
                    selectedDiner != null && !session.isClosed());

            if (null != listener
                    && !session.isBillRequested()) {
                menu.findItem(R.id.menu_additems).setVisible(true);
            } else {
                menu.findItem(R.id.menu_additems).setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_send_email:
                    if(DinerChooserFragment.session != null && selectedDiner != null && selectedDiner.getId() != null && selectedDiner.getId().contains(DinerChooserFragment.session.getId())) {
                        SendEmailHandlerImpl sendEmailHandler = new SendEmailHandlerImpl(getContext(), session);
                        sendEmailHandler.sendEmail();
                    }

                    return true;
                case R.id.menu_print: {
                    printDinerBill();
                    return true;
                }
                case R.id.menu_detachCustomer: {
                    final Diner d = selectedDiner;
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Detach customer?")
                            .setMessage(getString(R.string.detach_diner,
                                    selectedDiner.getEpicuriCustomer().getName()))
                            .setPositiveButton("Detach",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            detachEpicuriAccount(d);
                                        }
                                    })
                            .setNegativeButton("Do nothing", null)
                            .show();
                    dinerSelectionActionMode.finish();
                    return true;
                }
                case R.id.menu_confirm: {
                    final Diner d = selectedDiner;
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Assign items?")
                            .setMessage("Selected Items will be assigned to selected guest.\n"
                                    + "Deselected items will be reassigned back to the table.")
                            .setPositiveButton("Assign",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            confirmAssignChanges();
                                        }
                                    })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                case R.id.menu_allocateCustomer: {
                    attachEpicuriAccount(selectedDiner);
                    dinerSelectionActionMode.finish();
                    return true;
                }
                case R.id.menu_deselectall: {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Deselect all items?")
                            .setMessage("All items assigned to guest will be placed back onto the"
                                    + " table.")
                            .setPositiveButton("Deselect",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            deselectAll();
                                        }
                                    })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                case R.id.menu_pushnewsession: {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Push selected items to tab?")
                            .setMessage("Do you want to push all the selected items to a new "
                                    + "session?")
                            .setPositiveButton("Push",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,
                                                            int i) {
                                            ordersToTab();
                                        }
                                    })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
                case R.id.menu_dinerDetail: {
                    DinerDetailsFragment frag = DinerDetailsFragment.newInstance(
                            selectedDiner.getEpicuriCustomer(), selectedDiner.isBirthday());
                    frag.show(getFragmentManager(), FRAGMENT_DINER_DETAILS);
                    return true;
                }
                case R.id.menu_additems: {
                    listener.launchMenu();
                    dinerSelectionActionMode.finish();
                    return true;
                }

                case R.id.action_edit_session: {
                    if (session == null) return false;
                    PartyDetailsFragment fragment = PartyDetailsFragment.newInstance(session.getId(),
                            "", session.getName(), session.getNumberInParty(), true);
                    fragment.show(getActivity().getSupportFragmentManager(), "DetailsTag");
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // TODO: hide stuff
            dinerSelectionActionMode = null;
            onDinerClick(null);
        }
    }


    private void saveLayoutData(boolean withDialog) {
        String tableDefinition = tableView.getTableDefinition();
        WebServiceTask task = new WebServiceTask(getActivity(),
                new SaveTableSeatingWebServiceCall(tableDefinition, session.getId()), true);
        if (withDialog) {
            task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        }
        task.execute();
    }

    private void confirmAssignChanges() {
        if (getActivity() == null || !(getActivity() instanceof IBillSplitHandler)) return;

        IBillSplitHandler handler = (IBillSplitHandler) getActivity();
        handler.onConfirmChanges();
    }

    private void deselectAll() {
        if (getActivity() == null || !(getActivity() instanceof IBillSplitHandler)) return;

        IBillSplitHandler handler = (IBillSplitHandler) getActivity();
        handler.onDeselectAll();
    }

    private void ordersToTab() {
        if (getActivity() == null || !(getActivity() instanceof IBillSplitHandler)) return;

        IBillSplitHandler handler = (IBillSplitHandler) getActivity();
        handler.ordersToTab();
    }

    private void printDinerBill() {
        if (getActivity() == null || !(getActivity() instanceof IBillSplitHandler)) return;

        IBillSplitHandler handler = (IBillSplitHandler) getActivity();
        handler.printDinerBill();
    }
}