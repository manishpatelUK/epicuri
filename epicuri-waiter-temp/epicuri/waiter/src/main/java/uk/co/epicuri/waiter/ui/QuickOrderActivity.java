package uk.co.epicuri.waiter.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.SessionAdapter;
import uk.co.epicuri.waiter.interfaces.ILocationListener;
import uk.co.epicuri.waiter.interfaces.NewPartyDialogListener;
import uk.co.epicuri.waiter.interfaces.OnEpicuriMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriPrintBatch;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.QuickOrderLandscapeState;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.printing.FakeReceiptFragment;
import uk.co.epicuri.waiter.printing.PrintDirectlyService;
import uk.co.epicuri.waiter.printing.SendEmailHandlerImpl;
import uk.co.epicuri.waiter.ui.dialog.CashDrawerSelectDialog;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.QuickOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.SubmitOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class QuickOrderActivity extends EpicuriBaseActivity implements
        OnEpicuriMenuItemsSelectedListener, OnItemQueuedListener,
        NewPartyDialogListener, PaymentDialogFragment.Listener, ILocationListener {

    private static final String LOGGER = "QuickOrderActivity";

    private static final int LOADER_MODIFIERS = 1;
    private static final int LOADER_SESSIONS = 2;
    private final static int LOADER_PRINTERS = 2;

    private static final String FRAGMENT_MENU_ITEM = "menuItem";
    private static final String FRAGMENT_PENDING_ORDER = "pendingOrder";
    private static final String FRAGMENT_NEW_PARTY = "newParty";
    private static final String QO_ORDERS = "QO_ORDERS";
    private static final String QO_SESSION = "QO_SESSION";

    EpicuriMenu.Course quickOrderCourse = EpicuriMenu.Course.getDummyCourse("Quick Order");

    private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;
    private PendingOrderFragment pendingOrderFragment;
    private QuickOrderLandscapeFragment quickOrderLandscapeFragment;
    private PowerManager.WakeLock mWakeLock;

    private Map<String, EpicuriMenu.Printer> printers;

    private int orientation;
    private boolean restrictedOrientation = false;

    @Optional
    @InjectView(R.id.orderToolbar)
    Toolbar orderToolbar;

    public static Intent newInstance(Activity activity, ArrayList<EpicuriOrderItem> orders, QuickOrderLandscapeState state) {
        Intent i = new Intent(activity, QuickOrderActivity.class);
        i.putExtra(QO_ORDERS, orders);
        if(state != null){
            i.putExtra(QO_SESSION, state);
        }
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBack = false;

        setContentView(R.layout.activity_quickorder);
        orientation = getResources().getConfiguration().orientation;

        if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
            if (null == savedInstanceState || getSupportFragmentManager().findFragmentByTag(
                    FRAGMENT_PENDING_ORDER) == null) {
                Bundle extras = getIntent().getExtras();
                if (extras != null && extras.get(QO_ORDERS) != null && extras.get(QO_SESSION) == null) {
                    pendingOrderFragment = PendingOrderFragment.newInstance((ArrayList<EpicuriOrderItem>) extras.get(QO_ORDERS), null, modifierGroups);
                } else {
                    pendingOrderFragment = PendingOrderFragment.newInstance(null, modifierGroups);
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.pendingOrderFrame, pendingOrderFragment,
                                FRAGMENT_PENDING_ORDER)
                        .commit();
            } else {
                pendingOrderFragment =
                        (PendingOrderFragment) getSupportFragmentManager().findFragmentByTag(
                                FRAGMENT_PENDING_ORDER);
            }

            MenuFragment menuFragment = MenuFragment.newInstance(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.menuFragment, menuFragment, "menu_fragment")
                    .commit();
        }else {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.get(QO_ORDERS) != null) {
                quickOrderLandscapeFragment = (QuickOrderLandscapeFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.quickOrderFragment);
                quickOrderLandscapeFragment.setOrders((ArrayList<EpicuriOrderItem>) extras.get(QO_ORDERS));
                quickOrderLandscapeFragment.setSession((QuickOrderLandscapeState) extras.get(QO_SESSION));
            }
        }

        ButterKnife.inject(this);

        if (orderToolbar != null) {
            orderToolbar.setTitle("Items");
            orderToolbar.inflateMenu(R.menu.toolbar_quickorder);
            final long[] lastClickTime = {0};
            orderToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (pendingOrderFragment.getOrders().isEmpty()) {
                        Toast.makeText(QuickOrderActivity.this, "Add some items first", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (SystemClock.elapsedRealtime() - lastClickTime[0] < 250) {
                        return true;
                    }
                    lastClickTime[0] = SystemClock.elapsedRealtime();
                    switch (item.getItemId()) {
                        case R.id.action_add_to_tab:
                            addToTab(true);
                            return true;
                        case R.id.action_session:
                            addToSession();
                            return true;
                        case R.id.action_ad_hoc:
                            createAdHoc();
                            return true;
                        case R.id.action_clear:
                            pendingOrderFragment.clearOrders();
                            return true;
                    }
                    return false;
                }
            });
        }

        getSupportLoaderManager().initLoader(LOADER_MODIFIERS, null, modifierGroupsLoaderCallbacks);
        LoaderManager.LoaderCallbacks printersLoader = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
            @NonNull
            @Override
            public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i,
                                                                         Bundle bundle) {
                return new OneOffLoader<ArrayList<EpicuriMenu.Printer>>(
                        QuickOrderActivity.this, new PrinterLoaderTemplate());
            }

            @Override
            public void onLoadFinished(
                    @NonNull Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader,
                    ArrayList<EpicuriMenu.Printer> result) {
                System.out.println("QuickOrderActivity result " + result);
                if (result != null) {
                    printers = new HashMap<>();
                    for (EpicuriMenu.Printer p : result) {
                        printers.put(p.getId(), p);
                    }
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
                // don't care
            }
        };

        getSupportLoaderManager().restartLoader(LOADER_PRINTERS, null, printersLoader);

        mWakeLock = Utils.keepUnlocked(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_quickorder, menu);
        if (menu.findItem(R.id.menu_drawerkick) != null)
            menu.findItem(R.id.menu_drawerkick).setVisible(LocalSettings.getInstance(this).isAllowed(WaiterAppFeature.MANUAL_DRAWER_KICK));
        billPrintItem = menu.findItem(R.id.receiptPrint);
        billPrint = LocalSettings.getInstance(this).isBillPrint();
        if (billPrintItem != null){
            billPrintItem.setChecked(billPrint);
        }

        orderPrint = LocalSettings.getInstance(this).isOrderPrint();
        orderPrintItem = menu.findItem(R.id.orderPrint);
        if (orderPrintItem != null){
            orderPrintItem.setChecked(orderPrint);
        }

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) getSupportActionBar().hide();
        return true;
    }
    boolean billPrint = true;
    boolean orderPrint = true;
    MenuItem billPrintItem;
    MenuItem orderPrintItem;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sessionHistory:
                showSessionHistory();
                return true;
            case R.id.menu_drawerkick:
                if (null != printers) {
                    kickDrawer(null);
                }
                break;
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.lock_screen: {
                Intent lockScreenIntent = new Intent(this, LockActivity.class);
                lockScreenIntent.putExtra(LockActivity.EXTRA_POPUP_SWITCH, true);
                startActivity(lockScreenIntent);
                return true;
            }
            case R.id.receiptPrint: {
                billPrint = !billPrint;
                billPrintItem.setChecked(billPrint);
                LocalSettings.getInstance(this).cacheBillPrint(billPrint);
                if(billPrint){
                    Toast.makeText(this, R.string.receipt_prints_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.receipt_prints_off, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.orderPrint: {
                orderPrint = !orderPrint;
                orderPrintItem.setChecked(orderPrint);
                LocalSettings.getInstance(this).cacheOrderPrint(orderPrint);
                if(orderPrint){
                    Toast.makeText(this, R.string.order_prints_on, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.order_prints_on, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEpicuriMenuItemSelected(EpicuriMenu.Item item) {
        EpicuriOrderItem orderItem = new EpicuriOrderItem(item, quickOrderCourse);

        boolean autoAdd = true;
        for (String modifierId : item.getModifierGroupIds()) {
            EpicuriMenu.ModifierGroup group = null;
            for (EpicuriMenu.ModifierGroup g : modifierGroups) {
                if (g.getId().equals(modifierId)) {
                    group = g;
                    break;
                }
            }
            if (null == group) {
                Log.e(LOGGER, "Bad reference to modifier: " + modifierId);
                continue;
            }
            if (group.getLowerLimit() > 0) {
                autoAdd = false;
                break;
            }
        }
        if (autoAdd) {
            pendingOrderFragment.queueItem(orderItem, null);
        } else {
            // need to prompt for input
            if (null == modifierGroups) return;
            MenuItemFragment.newInstance(null, orderItem, null, modifierGroups).show(
                    getSupportFragmentManager(), FRAGMENT_MENU_ITEM);
        }
    }

    @Override
    public void queueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner) {
        if (pendingOrderFragment == null) return;

        pendingOrderFragment.queueItem(orderItem, diner);

        if (restrictedOrientation) return;

        if (pendingOrderFragment.getOrders().size() > 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void unQueueItem(EpicuriOrderItem orderItem, EpicuriSessionDetail.Diner diner) {
        if (pendingOrderFragment == null) return;

        pendingOrderFragment.unQueueItem(orderItem, diner);

        if (restrictedOrientation) return;

        if (pendingOrderFragment.getOrders().size() == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>
            modifierGroupsLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu
                    .ModifierGroup>>>() {

                @Override
                public Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> onCreateLoader(
                        int id, Bundle args) {
                    return new EpicuriLoader<ArrayList<EpicuriMenu.ModifierGroup>>(
                            QuickOrderActivity.this, new ModifierGroupLoaderTemplate());
                }

                @Override
                public void onLoadFinished(
                        Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader,
                        LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>> data) {
                    if (null == data) { // nothing returned, ignore
                        return;
                    } else if (data.isError()) {
                        Toast.makeText(QuickOrderActivity.this,
                                "QuickOrderActivity error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    modifierGroups = data.getPayload();
                    if (pendingOrderFragment != null) {
                        pendingOrderFragment.setModifierGroups(modifierGroups);
                    } else {
                        quickOrderLandscapeFragment = ((QuickOrderLandscapeFragment)
                                getSupportFragmentManager().findFragmentById(R.id
                                        .quickOrderFragment));

                        if (quickOrderLandscapeFragment != null)
                            quickOrderLandscapeFragment.setModifierGroups(modifierGroups);
                    }
                }

                @Override
                public void onLoaderReset(
                        Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader) {
                    modifierGroups = null;
                }
            };

    private SessionAdapter addToSessionAdapter;
    private AlertDialog addToSessionDialog;

    public void addToSession() {
        final ArrayList<EpicuriOrderItem> orders = pendingOrderFragment != null ? pendingOrderFragment
                .getOrders() : ((QuickOrderLandscapeFragment) getSupportFragmentManager().findFragmentById
                (R.id.quickOrderFragment)).getOrderedItems();
        Money runningTotal = Money.zero(LocalSettings.getCurrencyUnit());
        for (EpicuriOrderItem o : orders) {
            runningTotal = runningTotal.plus(o.getCalculatedPriceIncludingQuantity());
        }
        final Money total = runningTotal;
        final int itemCount = orders.size();
        addToSessionAdapter = new SessionAdapter(this);
        getSupportLoaderManager().restartLoader(LOADER_SESSIONS, null, sessionsLoadedCallback);
        addToSessionDialog = new AlertDialog.Builder(this)
                .setTitle("Add to session")
                .setAdapter(addToSessionAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EpicuriSessionDetail session = addToSessionAdapter.getItem(which);
                        new AlertDialog.Builder(QuickOrderActivity.this)
                                .setTitle(String.format("Add to %s", session.getName()))
                                .setMessage(String.format(Locale.UK,
                                        "Please confirm you want to add %d items totalling %s to "
                                                + "\"%s\"",
                                        itemCount,
                                        LocalSettings.formatMoneyAmount(total, true),
                                        session.getName()))
                                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        addToSession(session, orders);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .show();
    }

    private LoaderManager.LoaderCallbacks<? extends Object> sessionsLoadedCallback =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {
                @Override
                public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id,
                                                                                             Bundle args) {
                    return new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(
                            QuickOrderActivity.this, new SessionsLoaderTemplate());
                }

                @Override
                public void onLoadFinished(
                        Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
                        LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
                    if (null == data) { // nothing returned, ignore
                        return;
                    } else if (data.isError()) {
                        Toast.makeText(QuickOrderActivity.this,
                                "QuickOrderACtivity error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ArrayList<EpicuriSessionDetail> sessions = new ArrayList<>(
                            data.getPayload().size());
                    for (EpicuriSessionDetail s : data.getPayload()) {
                        if (s.getType() == EpicuriSessionDetail.SessionType.DINE && !s.isAdHoc()) {
                            sessions.add(s);
                        }
                    }
                    addToSessionAdapter.setState(sessions);
                    if (sessions.size() == 0
                            && null != addToSessionDialog) {
                        addToSessionDialog.dismiss();
                        addToSessionDialog = null;
                        new AlertDialog.Builder(QuickOrderActivity.this)
                                .setTitle("No sessions found")
                                .setMessage(
                                        "Sorry, cannot find sessions to which this this order can"
                                                + " be added")
                                .setNegativeButton("Dismiss", null)
                                .show();

                    }
                }

                @Override
                public void onLoaderReset(
                        Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader) {

                }
            };

    private void addToSession(final EpicuriSessionDetail session, final ArrayList<EpicuriOrderItem> orders) {

        // assign all orders to the table
        EpicuriSessionDetail.Diner theTable = session.getTableDiner();
        for (EpicuriOrderItem o : orders) {
            o.setDinerId(theTable.getId());
        }
        WebServiceTask task = new WebServiceTask(this, new SubmitOrderWebServiceCall(session, orders), true);

        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                if (pendingOrderFragment != null) pendingOrderFragment.clearOrders();
                else orders.clear();

                if (quickOrderLandscapeFragment != null) quickOrderLandscapeFragment.clear();

                Toast.makeText(QuickOrderActivity.this, "Added to session", Toast.LENGTH_SHORT).show();
                ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);

                boolean supportsImmediatePrinting = EpicuriApplication.getInstance(QuickOrderActivity.this).getApiVersion() >= GlobalSettings.API_VERSION_6;
                if (supportsImmediatePrinting) { //todo USE PrintUtil.printFromJsonResponse instead -- && search for similar codes in the app
                    try {
                        JSONObject responseJson = new JSONObject(response);

                        if (responseJson.has("batches")) {
                            JSONArray array = responseJson.getJSONArray("batches");
                            if (array != null) {
                                for (int i = 0; i < array.length(); ++i) {
                                    EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
                                    objBatches.add(batch);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(QuickOrderActivity.this, PrintDirectlyService.class);
                    intent.putExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
                    startService(intent);
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    public void addToTab(boolean preSelectPartyName) {
        NewPartyFragment.newInstance(null, new String[0], "Create new tab", "Create", preSelectPartyName)
                .show(getSupportFragmentManager(), FRAGMENT_NEW_PARTY);

    }

    @Override
    public void onCreateNewParty(CharSequence partyName, int numberInParty,
                                 EpicuriCustomer customer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCreateNewSession(CharSequence partyName, int numberInParty,
                                   EpicuriCustomer customer, String[] tables, String serviceId) {

        final ArrayList<EpicuriOrderItem> orderItems = pendingOrderFragment != null ?
                pendingOrderFragment.getOrders() :
                ((QuickOrderLandscapeFragment) getSupportFragmentManager().findFragmentById
                        (R.id.quickOrderFragment)).getOrderedItems();
        QuickOrderWebServiceCall call =
                new QuickOrderWebServiceCall(partyName.toString(), numberInParty, customer, false,
                        tables, serviceId, orderItems, true, null, false);
        WebServiceTask task = new WebServiceTask(this, call);

        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                String sessionId = "0";
                ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);
                boolean supportsImmediatePrinting = EpicuriApplication.getInstance(QuickOrderActivity.this).getApiVersion() >= GlobalSettings.API_VERSION_6;
                try {
                    if (supportsImmediatePrinting) {
                        JSONObject responseJson = new JSONObject(response);
                        sessionId = responseJson.getString("SessionId");

                        if (responseJson.has("batches")) {
                            JSONArray array = responseJson.getJSONArray("batches");
                            if (array != null) {
                                for (int i = 0; i < array.length(); ++i) {
                                    EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
                                    objBatches.add(batch);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (supportsImmediatePrinting) {
                    Intent intent = new Intent(QuickOrderActivity.this, PrintDirectlyService.class);
                    intent.putExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
                    startService(intent);
                }
                if (sessionId != null && !sessionId.equals("0")) {
                    HubActivity.setNewParty(true);
                    Toast.makeText(QuickOrderActivity.this, "Session is created", Toast.LENGTH_SHORT).show();
                    if (pendingOrderFragment != null)
                        pendingOrderFragment.clearOrders();
                    else
                        orderItems.clear();

                    if (quickOrderLandscapeFragment != null) quickOrderLandscapeFragment.clear();
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        if (call.isValid()) {
            task.execute();
        }
    }

    private void createAdHoc() {
        QuickOrderWebServiceCall call = new QuickOrderWebServiceCall
                ("QuickOrder", 0, null, true, new String[0], pendingOrderFragment.getOrders(), orderPrint, null, false);
        WebServiceTask task = new WebServiceTask(this, call, true);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Log.d("QuickOrder", response);

                String sessionId = "0";
                ArrayList<EpicuriPrintBatch> objBatches = new ArrayList<>(1);

                boolean supportsImmediatePrinting = EpicuriApplication.getInstance(QuickOrderActivity.this).getApiVersion() >= GlobalSettings.API_VERSION_6;
                try {
                    JSONObject responseJson = new JSONObject(response);
                    sessionId = responseJson.getString("SessionId");

                    if (supportsImmediatePrinting && responseJson.has("batches")) {
                        JSONArray array = responseJson.getJSONArray("batches");
                        if (array != null) {
                            for (int i = 0; i < array.length(); ++i) {
                                EpicuriPrintBatch batch = new EpicuriPrintBatch(array.getJSONObject(i), new Date());
                                objBatches.add(batch);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (sessionId == null || sessionId.equals("0")) {
                    Toast.makeText(QuickOrderActivity.this, "Something went wrong",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent sessionIntent = new Intent(QuickOrderActivity.this,
                            SeatedSessionActivity.class);
                    sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
                    sessionIntent.putExtra(GlobalSettings.EXTRA_BILL_PRINT, billPrint);
                    sessionIntent.putExtra(SeatedSessionActivity.EXTRA_IMMEDIATE_PAYMENT, true);
                    startActivity(sessionIntent);
                    pendingOrderFragment.clearOrders();
                }

                if (supportsImmediatePrinting) {
                    Intent intent = new Intent(QuickOrderActivity.this, PrintDirectlyService.class);
                    intent.putParcelableArrayListExtra(PrintDirectlyService.BATCH_EXTRA, objBatches);
                    startService(intent);
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        if (call.isValid()) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void kickDrawer(CashDrawerSelectDialog.OnDrawerKicked listener) {
        conditionalDrawerKick(this, printers, listener);
    }

    private void showSessionHistory() {
        Intent intent = new Intent(this, SessionHistoryActivity.class);
        startActivity(intent);
    }

    public void lockScreen() {
        if (restrictedOrientation) return;

        setRequestedOrientation(orientation == Configuration.ORIENTATION_PORTRAIT ? ActivityInfo
                .SCREEN_ORIENTATION_SENSOR_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    public void unlockScreen() {
        if (restrictedOrientation) return;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    protected void onDestroy() {
        Utils.enableLock(mWakeLock);
        super.onDestroy();
    }

    @Override
    public void addPayment(double amount, EpicuriAdjustmentType type, boolean payPrintAndClose, EpicuriSessionDetail session, String reference) {
        quickOrderLandscapeFragment = (QuickOrderLandscapeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quickOrderFragment);

        if (quickOrderLandscapeFragment != null)
            quickOrderLandscapeFragment.addPayment(amount, type, payPrintAndClose, session, reference);
    }

    @Override
    public void addMewsPayment(double amount, EpicuriAdjustmentType paymentMethod,
                               EpicuriMewsCustomer customer, boolean payPrintAndClose, boolean quickOrder) {

    }

    @Override
    public void addPaymentSensePayment(EpicuriSessionDetail sessionId, double amount, EpicuriAdjustmentType type,
                                       boolean payPrintAndClose, String reference, Integer amountGratuity,
                                       EpicuriAdjustmentType gratuity) {
        addPaymentSenseAdjustment(sessionId, amount, type, payPrintAndClose, reference, amountGratuity, gratuity);
    }

    void addPaymentSenseAdjustment(final EpicuriSessionDetail sessionId, double amount, EpicuriAdjustmentType type,
                                   final boolean payPrintAndClose, final String reference, final Integer amountGratuity,
                                   final EpicuriAdjustmentType gratuity) {
        Money moneyAmount = Money.of(LocalSettings.getCurrencyUnit(), amount);
        if (amountGratuity != null && amountGratuity > 0 && gratuity != null) {
            addPayment(amountGratuity / 100.0, gratuity,
                    moneyAmount.equals(sessionId.getRemainingTotal()) || moneyAmount.isGreaterThan(sessionId.getRemainingTotal()),
                    sessionId, reference);
        }

        addPayment(amount, type, moneyAmount.equals(sessionId.getRemainingTotal()) || moneyAmount.isGreaterThan(sessionId.getRemainingTotal()), sessionId, reference);
    }

    @Override
    public void onLocation(String location) {
        if(quickOrderLandscapeFragment != null) {
            quickOrderLandscapeFragment.onLocation(location);
        }
    }

    @Override
    public void onBackPressed() {

        if (onBackPressedListener != null && !doBack) {
            onBackPressedListener.doBack();
        } else {
            if (isTaskRoot()) {
                Intent i = new Intent(this, HubActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                super.onBackPressed();
            }
        }
    }

    private boolean doBack;

    public void setDoBack() {
        this.doBack = true;
    }

    private OnBackPressedListener onBackPressedListener;

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    public void printReceipt() {
        quickOrderLandscapeFragment = (QuickOrderLandscapeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quickOrderFragment);

        if (quickOrderLandscapeFragment != null) {
            quickOrderLandscapeFragment.onBillPrintPressed();
        }
    }

    public interface OnBackPressedListener {
        void doBack();
    }
}
