package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.DinerActionHandler;
import uk.co.epicuri.waiter.interfaces.NewPartyDialogListener;
import uk.co.epicuri.waiter.interfaces.OnDinerClickListener;
import uk.co.epicuri.waiter.interfaces.RemoveOrderListener;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.ui.dialog.CashDrawerSelectDialog;
import uk.co.epicuri.waiter.ui.dialog.ForceCloseInfoDialog;
import uk.co.epicuri.waiter.ui.menueditor.PartyDetailsFragment;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.ConvertToTabWebServiceCall;
import uk.co.epicuri.waiter.webservice.EditOrderWebServiceCall;
import uk.co.epicuri.waiter.webservice.RequestBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.UnrequestBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.ui.MenuFragment.EXTRA_QUICK_ORDER_MENU;

public class SeatedSessionActivity extends SessionActivity implements OnDinerClickListener,
        RemoveOrderListener, DinerActionHandler, NewPartyDialogListener,
        DinerChooserFragment.IBillSplitHandler, ForceCloseInfoDialog.OnForceCloseListener {

    /**
     * pass as true to start the "Add items" process immediately
     */
    public static final String EXTRA_ADD_ITEMS = "uk.co.epicuri.extra.ADD_ITEMS";

    public static final String EXTRA_IMMEDIATE_PAYMENT = "uk.co.epicuri.extra.IMMEDIATE_PAYMENT";

    private static final String TAB_ORDERS = "ORDERS";
    private static final String TAB_EVENTS = "ACTIONS";
    private static final String TAB_PAYMENTS = "PAYMENTS";

    private static final String KEY_RESHOW_TAB = "reshowTab";
    private static final String FRAGMENT_CONVERT_TO_TAB = "convertToTab";
    private static final String FRAGMENT_DINER_CHOOSER = "dinerChooser";
    private static final String FRAGMENT_QUICK_ORDER_HEADER = "quickOrderFragment";
    @InjectView(R.id.session_view_pager) CustomViewPager pager;
    @InjectView(R.id.header) View header;
    int reshowTab = -1;
    String requestTab = null;
    boolean initialShow = true;
    TabsPagerAdapter adapter;
    @InjectView(R.id.tabs) TabLayout tabLayout;
    private EpicuriSessionDetail session;
    private DinerChooserFragment dinerChooserFragment;
    private boolean showAddItemsOnceLoaded;
    private boolean showPaymentOnceLoaded;
    private boolean renderFailure;
    private boolean activitiesIncluded;
    private EpicuriSessionDetail.Diner selectedDiner;
    private boolean flag = true;
    private boolean isSessionHistory;
    private boolean eventsTabVisible = false;
    private boolean paymentsTabVisible = false;
    private boolean ordersTabVisible = false;
    public static String sessionId;
    public static  boolean isAdHoc;
    private boolean billPrint = true;

    public EpicuriSessionDetail.Diner getSelectedDiner() {
        return selectedDiner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) isSessionHistory = extras.getBoolean("history");

        setContentView(R.layout.activity_session);
        ButterKnife.inject(this);
        final boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        header.setVisibility(isLandscape? View.GONE : View.VISIBLE);
        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(EXTRA_ADD_ITEMS, false)) {
                // show the "add items" dialog straight away
                showAddItemsOnceLoaded = true;

            } else if (getIntent().getBooleanExtra(EXTRA_IMMEDIATE_PAYMENT, false)) {
                // show the "add items" dialog straight away
                showPaymentOnceLoaded = true;
            }
            if (!getIntent().getBooleanExtra(GlobalSettings.EXTRA_BILL_PRINT, true)){
                billPrint = false;
            }
            requestTab = TAB_ORDERS;
        } else {
            if (savedInstanceState != null) reshowTab = savedInstanceState.getInt(KEY_RESHOW_TAB);
        }
        refreshTabs();
    }

    @Override protected void onResume() {
        super.onResume();
        sessionId = null;
        if (renderFailure) {
            refreshTabs();
            renderFailure = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getSession() != null) {
            sessionId = getSession().getId();
            isAdHoc = session.isAdHoc() && !session.isClosed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionId = null;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int id = 0;
        if (pager != null) id = pager.getCurrentItem();
        if (outState != null) outState.putInt(KEY_RESHOW_TAB, id);
        super.onSaveInstanceState(outState);
    }

    private void refreshTabs() {
        boolean showEventsTab = ((null != session && session.getEvents().size() > 0) || flag);
        boolean showPaymentsTab = (null != session && (session.getAdjustments().size() > 0 || session.isBillRequested()) || flag);
        int currentTab = pager.getCurrentItem();
        if (reshowTab >= 0) {
            currentTab = reshowTab;
        }

        if (reshowTab >= 0 || !ordersTabVisible || (showEventsTab != eventsTabVisible) || (showPaymentsTab != paymentsTabVisible)) {
            adapter.resetTabs();
            adapter.notifyDataSetChanged();
            adapter = new TabsPagerAdapter(getSupportFragmentManager());
            pager.removeAllViews();
            pager.setAdapter(adapter);
            ordersTabVisible = eventsTabVisible = paymentsTabVisible = false;
            if (showEventsTab) {
                adapter.addPage(TAB_EVENTS, SessionEventsFragment.newInstance());
                eventsTabVisible = true;
            }

            adapter.addPage(TAB_ORDERS, SessionOrdersFragment.newInstance());
            ordersTabVisible = true;

            if (showPaymentsTab) {
                adapter.addPage(TAB_PAYMENTS, SessionPaymentsFragment.newInstance());

                if (paymentsTabVisible != showPaymentsTab && initialShow) {
                    requestTab = TAB_PAYMENTS;
                }
                paymentsTabVisible = true;
            }

            if (null != requestTab) {
                pager.setCurrentItem(adapter.getPositionOfFragmentByTag(requestTab));
                requestTab = null;
            } else {
                pager.setCurrentItem(currentTab);
            }

            if (session != null) {
                reshowTab = -1;
            }

            flag = false;
            initialShow = false;
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void addPayment(double amount, EpicuriAdjustmentType type, boolean payPrintAndClose, EpicuriSessionDetail session, String reference) {
        super.addPayment(amount, type, payPrintAndClose, session, reference);

        if (payPrintAndClose && session.isAdHoc()) {
            printReceipt(true, billPrint);
        }
        pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS));
    }

    @Override
    public void addPaymentSensePayment(EpicuriSessionDetail sessionId, double amount, EpicuriAdjustmentType type,
                                       boolean payPrintAndClose, String reference, Integer amountGratuity,
                                       EpicuriAdjustmentType gratuity) {
        super.addPaymentSensePayment(session, amount, type, payPrintAndClose, reference, amountGratuity,
                gratuity);
        if (payPrintAndClose) {
            printReceipt(false);
        }
        pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS));
    }

    @Override
    public void addMewsPayment(double amount, EpicuriAdjustmentType paymentMethod, EpicuriMewsCustomer customer, boolean payPrintAndClose, boolean quickOrder) {
        super.addMewsPayment(amount, paymentMethod, customer, payPrintAndClose, quickOrder);
        pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS));
    }

    @Override
    void addAdjustment(double amount, boolean percentage, EpicuriAdjustmentType type, boolean autoClose, String itemType, boolean quickOrder) {
        super.addAdjustment(amount, percentage, type, autoClose, itemType, quickOrder);
        pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS));
    }

    @Override void addPaymentSenseAdjustment(double amount, EpicuriAdjustmentType type,
                                             boolean payPrintAndClose, String reference, Integer amountGratuity,
                                             EpicuriAdjustmentType gratuity) {
        super.addPaymentSenseAdjustment(amount, type, payPrintAndClose, reference,
                amountGratuity, gratuity);
        pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS));
    }

    @Override
    void onSessionLoaded(EpicuriSessionDetail newSession) {
        if (session != null) newSession.setBillSplitMode(session.isBillSplitMode());
        session = newSession;

        if (selectedDiner != null) {
            selectedDiner = newSession.getDinerFromId(selectedDiner.getId());
        }

        FragmentManager fm = getSupportFragmentManager();
        if (session.isAdHoc()) {
            QuickOrderHeaderFragment frag = (QuickOrderHeaderFragment) fm.findFragmentByTag(FRAGMENT_QUICK_ORDER_HEADER);
            if (frag == null) {
                frag = new QuickOrderHeaderFragment();
                fm.beginTransaction()
                        .replace(R.id.header, frag, FRAGMENT_QUICK_ORDER_HEADER)
                        .commitAllowingStateLoss();
            }
            View content = findViewById(R.id.header);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition((ViewGroup) content.getParent());
            }
            ViewGroup.LayoutParams lp = content.getLayoutParams();
            lp.height = (int) getResources().getDimension(R.dimen.quick_order_height);
            content.setLayoutParams(lp);
            frag.onSessionChanged(session);
        } else {
            dinerChooserFragment = (DinerChooserFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DINER_CHOOSER);
            if (null == dinerChooserFragment) {
                dinerChooserFragment = new DinerChooserFragment();
                if(session == null) {
                    Crashlytics.log("Session is null in SeatedSessionActivity - Line ~294");
                }
                dinerChooserFragment.onSessionChanged(session);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.header, dinerChooserFragment, FRAGMENT_DINER_CHOOSER)
                        .commitAllowingStateLoss();
            } else {
                dinerChooserFragment.onSessionChanged(session);
            }

            View content = findViewById(R.id.header);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition((ViewGroup) content.getParent());
            }

            ViewGroup.LayoutParams lp = content.getLayoutParams();
            lp.height = (int) getResources().getDimension(R.dimen.session_header_height);
            content.setLayoutParams(lp);
            dinerChooserFragment.setOnDinerChangeListener(this);
        }

        try {
            refreshTabs();
        } catch (IllegalStateException exception) {
            // This exception will be thrown when session is loaded in background
            // tabHost.addTab will throw the exception because app is in background
            // save flag and retry render onResume
            renderFailure = true;
        }

        invalidateOptionsMenu();

        if (showAddItemsOnceLoaded) {
            showAddItemsOnceLoaded = false;
            launchMenu();
        } else if (showPaymentOnceLoaded) {
            showPaymentOnceLoaded = false;
            if(session.getLinkedTo() == null) {
                showPaymentDialog();
            } else {
                showPaymentDialogOnDefer();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (null == session) return super.onCreateOptionsMenu(menu);

        if (!session.isClosed() && !session.isVoided() &&
                LocalSettings.getInstance(this).isAllowed(WaiterAppFeature.MANUAL_DRAWER_KICK))
            getMenuInflater().inflate(R.menu.kickdrawer, menu);

        if (menu.findItem(R.id.menu_reseatSession) == null &&
                !session.isClosed() &&
                !session.isVoided() &&
                session.isBillRequested() &&
                !session.paymentsExceedBill()) {
            getMenuInflater().inflate(R.menu.action_additem, menu);
        }

        if (session.isClosed()) {
            if (getLoggedInUser().isManager()) {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_closed, menu);
                menu.findItem(R.id.menu_void).setVisible(!session.isVoided());
                menu.findItem(R.id.menu_unvoid).setVisible(session.isVoided());
                menu.findItem(R.id.menu_print).setVisible(true);
                if (session.isAdHoc()) {
                    menu.findItem(R.id.menu_reopen).setVisible(false);
                } else {
                    menu.findItem(R.id.menu_reopen).setVisible(!session.isVoided());
                }
            }
        } else if (!session.isBillRequested()) {
            if (session.getOrders().isEmpty()) {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_insession_noorders, menu);
            } else {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_insession_withorders, menu);
                if (menu.findItem(R.id.menu_forceCloseSession) != null)
                    menu.findItem(R.id.menu_forceCloseSession)
                            .setVisible(LocalSettings.getInstance(this)
                                    .isAllowed(WaiterAppFeature.FORCE_CLOSE));
            }

            if (menu.findItem(R.id.menu_addPayment) != null) {
                menu.findItem(R.id.menu_addPayment)
                        .setVisible(LocalSettings.getInstance(this)
                                .isAllowed(WaiterAppFeature.ADD_DELETE_PAYMENT));
            }

            if (menu.findItem(R.id.menu_addDiscount) != null) {
                menu.findItem(R.id.menu_addDiscount)
                        .setVisible(LocalSettings.getInstance(this)
                                .isAllowed(WaiterAppFeature.ADD_DELETE_DISCOUNT));
            }
        } else if (!session.isPaid()) {
            if (!session.paymentsExceedBill()) {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_billrequested_unpaid, menu);
            } else {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_billrequested_paid, menu);
            }
            if (menu.findItem(R.id.menu_addPayment) != null) {
                menu.findItem(R.id.menu_addPayment)
                        .setVisible(LocalSettings.getInstance(this)
                                .isAllowed(WaiterAppFeature.ADD_DELETE_PAYMENT));
            }

            if (menu.findItem(R.id.menu_addDiscount) != null) {
                menu.findItem(R.id.menu_addDiscount)
                        .setVisible(LocalSettings.getInstance(this)
                                .isAllowed(WaiterAppFeature.ADD_DELETE_DISCOUNT));
            }
            if (menu.findItem(R.id.menu_forceCloseSession) != null)
                menu.findItem(R.id.menu_forceCloseSession).setVisible(LocalSettings.getInstance(this).isAllowed(WaiterAppFeature.FORCE_CLOSE));

            if (EpicuriApplication.getInstance(this).getApiVersion() > 5) {
                getMenuInflater().inflate(R.menu.billsplit_menu, menu);
                menu.findItem(R.id.menu_enter_billsplit).setVisible(!session.isBillSplitMode());
                menu.findItem(R.id.menu_exit_billsplit).setVisible(session.isBillSplitMode());
            }
        } else {
            getMenuInflater().inflate(R.menu.activity_sessiondetail_paid, menu);
        }

        if (session.isAdHoc()) {
            if (!session.isClosed()) {
                getMenuInflater().inflate(R.menu.activity_sessiondetail_adhoc, menu);
            }
        } else if (menu.findItem(R.id.menu_reseatSession) == null && !session.isClosed() && !session.isVoided()) {
            if (!session.isTab()) {
                getMenuInflater().inflate(R.menu.action_reseatsession, menu);
            }
        }
        if (!session.isAdHoc() && !session.isBillRequested()) {
            getMenuInflater().inflate(R.menu.menu_edit_session, menu);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_kick_drawer: {
                kickDrawer(null);
                return true;
            }
            case R.id.action_edit_session: {
                if (session == null) return false;

                PartyDetailsFragment fragment = PartyDetailsFragment.newInstance(session.getId(),
                        "", session.getName(), session.getNumberInParty(), true);
                fragment.show(getSupportFragmentManager(), "DetailsTag");
                return true;
            }
            case R.id.menu_additems:
                if (session.isBillRequested()) {
                    new AlertDialog.Builder(this).setTitle("Unlock session?")
                            .setMessage("This will reopen the session for editing")
                            .setPositiveButton("Unlock session", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unlockSession(true);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    launchMenu();
                }
                return true;
            case R.id.menu_paybill:
                if (session.isTab()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Mark tab as paid")
                            .setMessage("This will mark as paid and close the tab")
                            .setPositiveButton("Mark as paid", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    kickDrawer(new CashDrawerSelectDialog.OnDrawerKicked() {
                                        @Override
                                        public void onDrawerKicked(boolean success) {
                                            payBill(true, session.isAdHoc());
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Do nothing", null)
                            .show();
                } else {
                    showPayBillDialog();
                }
                return true;
            case R.id.menu_requestbill:
                requestBill();
                return true;
            case R.id.menu_print: {
                printReceipt(true);
                return true;
            }
            case R.id.menu_enter_billsplit:
            case R.id.menu_exit_billsplit:
                session.setBillSplitMode(!session.isBillSplitMode());
                if (session.isBillSplitMode() && dinerChooserFragment != null)
                    dinerChooserFragment.onDinerClick(session.getTableDiner());
                Toast.makeText(this, session.isBillSplitMode() ? "Bill splitting started. Choose"
                        + " a guest to assign items." : "Bill splitting finished.", Toast
                        .LENGTH_SHORT).show();
                flag = eventsTabVisible;
                reshowTab = eventsTabVisible ? 1 : 0;
                Log.d("TAG_ORDER", "Bill split Reshow tab: " + reshowTab);
                initialShow = false;
                onSessionLoaded(session);
                return true;
            case R.id.menu_convert_to_tab: {
                new AlertDialog.Builder(this)
                        .setTitle("Convert to tab")
                        .setMessage("This will change the session from an ad-hoc session to a tab")
                        .setPositiveButton("Convert", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                convertToTab();
                            }
                        })
                        .setNegativeButton("Do nothing", null)
                        .show();
                return true;
            }
            case R.id.menu_closeSession:
            case R.id.menu_forceCloseSession: {
                boolean showInfo = false;
                final Map<String,EpicuriAdjustmentType> adjustmentTypes = LocalSettings.getInstance(this).getCachedRestaurant().getAdjustmentTypesLookup();
                for (EpicuriAdjustment adjustment : session.getAdjustments()){
                    if(adjustmentTypes.containsKey(adjustment.getTypeId()) &&
                            adjustmentTypes.get(adjustment.getTypeId()).getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
                        showInfo = true;
                        break;
                    }
                }

                if (showInfo) {
                    ForceCloseInfoDialog forceCloseInfoDialog = ForceCloseInfoDialog.newInstance(session);
                    forceCloseInfoDialog.setListener(this);
                    forceCloseInfoDialog.show(getSupportFragmentManager(), "force_close_info");
                } else {
                    forceClose();
                }
                return true;
            }
            case android.R.id.home: {
                if (handleUpOrBack()) {
                    return true;
                }

                onBackPressed();
                return true;

            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void forceClose() {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        final boolean forced = !session.isCloseable();
        final int leave;
        if (session.isTab()) {
            b.setTitle(R.string.closeTab_title)
                    .setMessage(forced ? R.string.forceCloseTab_message : R.string.closeTab_message);
            leave = R.string.closeTab_leave;
            if (forced) {
                b.setPositiveButton(R.string.closeTab_forceClose, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeSession(true);
                    }
                });
            } else {
                b.setPositiveButton(R.string.closeTab_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeSession(false);
                    }
                });
            }
        } else {
            b.setTitle(R.string.closeSession_title)
                    .setMessage(forced ? R.string.forceCloseSession_message : R.string.closeSession_message);
            leave = R.string.closeSession_leave;

            if (forced) {
                // only prompt for black marks if items were ordered
                b.setPositiveButton(R.string.forceCloseSession_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeSession(true);
                    }
                });
            } else {
                b.setPositiveButton(R.string.closeSession_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeSession(false);
                    }
                });
            }
        }

        b.setNegativeButton(leave, null);
        b.show();
    }

    @Override public void onApplyDiscount() {
        EpicuriRestaurant restaurant = LocalSettings.getInstance(this).getCachedRestaurant();
        ArrayList<EpicuriAdjustmentType> adjustmentTypes = restaurant.getDiscountTypes();
        addAdjustment(session.getRemainingTotal().getAmount().doubleValue(), false, getUnpaidType(adjustmentTypes), false, TYPE_ALL, false);
        closeSession(false);
    }

    private EpicuriAdjustmentType getUnpaidType(ArrayList<EpicuriAdjustmentType> adjustmentTypes) {
        for(EpicuriAdjustmentType epicuriAdjustmentType : adjustmentTypes) {
            if(epicuriAdjustmentType.getName().equalsIgnoreCase("UNPAID")) {
                return epicuriAdjustmentType;
            }
        }
        return adjustmentTypes.get(0);
    }

    @Override public void onRemovePayments() {
        forceClose();
    }

    public EpicuriSessionDetail getSession() {
        return session;
    }

    public void launchMenu() {
        Intent intentToOrder = new Intent(SeatedSessionActivity.this, OrderingActivity.class);
        intentToOrder.putExtras(getIntent().getExtras());
        if (session.getType() != EpicuriSessionDetail.SessionType.DINE) {
            String menuId = session.getTakeawayMenuId();
            intentToOrder.putExtra(GlobalSettings.EXTRA_MENU_ID, menuId);
        } else if (session.getServiceDefaultMenuId() != null && !session.getServiceDefaultMenuId().equals("0") && !session.getServiceDefaultMenuId().equals("-1")) {
            intentToOrder.putExtra(GlobalSettings.EXTRA_MENU_ID, String.valueOf(session.getServiceDefaultMenuId()));
        }

        if (session.getName().equals(EpicuriSessionDetail.SessionType.AD_HOC.toString())){
            intentToOrder.putExtra(EXTRA_QUICK_ORDER_MENU, true);
        }

        intentToOrder.putExtra(GlobalSettings.EXTRA_DINER, (Parcelable) selectedDiner);
        intentToOrder.putExtra(GlobalSettings.EXTRA_SESSION, (Parcelable) session);
        startActivity(intentToOrder);
    }

    @Override
    public void removeOrderItems(ArrayList<EpicuriOrderItem> items, EpicuriAdjustmentType reason) {
        removeOrderItem(items, 0, reason);
    }

    private void removeOrderItem(final ArrayList<EpicuriOrderItem> items, final int index, final EpicuriAdjustmentType reason) {
        if (reason == null) {
            new AlertDialog.Builder(SeatedSessionActivity.this)
                    .setTitle("Remove Order")
                    .setMessage("Reason code is required for order removal.")
                    .setPositiveButton("Dismiss", null)
                    .show();
            return;
        }
        if (index >= items.size()) {
            UpdateService.requestUpdate(this, sessionUri);
            int message = R.string.session_orderChanged;
            try {
                EpicuriRestaurant restaurant = LocalSettings.getStaticCachedRestaurant();
                if(restaurant.stockCountdownEnabled()) {
                    message = R.string.session_orderChanged_stockControl;
                }
            } catch (Exception ex){}
            if (!session.isBillRequested()) {
                new AlertDialog.Builder(SeatedSessionActivity.this)
                        .setTitle(getString(R.string.session_orderChangedTitle))
                        .setMessage(getString(message))
                        .setPositiveButton("Dismiss", null)
                        .show();
            }
            return;
        }
        EditOrderWebServiceCall call = new EditOrderWebServiceCall(items.get(index), reason);
        WebServiceTask task = new WebServiceTask(this, call, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                removeOrderItem(items, index + 1, reason);
            }
        });
        task.execute();
    }

    @Override public void printDinerBill() {
        printDinerReceipt(selectedDiner);
    }

    private void requestBill() {
        if (session.isBillRequested()) {
            throw new IllegalStateException("Bill already requested");
        }
        WebServiceTask task = new WebServiceTask(SeatedSessionActivity.this, new RequestBillWebServiceCall(session.getId()));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {

                invalidateOptionsMenu();
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(SeatedSessionActivity.this, R.string.toast_billRequested, Toast.LENGTH_SHORT).show();

                pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_PAYMENTS)); // try to switch to tab
                requestTab = TAB_PAYMENTS; // in case it's not visible yet
            }
        });
        task.execute();
    }

    private void unlockSession(boolean showAddOrders) {
        WebServiceTask task = new WebServiceTask(this, new UnrequestBillWebServiceCall(session.getId()));
        if (showAddOrders) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

                @Override
                public void onSuccess(int code, String response) {
                    session.setBillSplitMode(false);
                    launchMenu();
                }
            });
        }
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    private void convertToTab() {
        NewPartyFragment frag = NewPartyFragment.newInstance(null, new String[0], "Convert to tab", "Convert", true);
        frag.show(getSupportFragmentManager(), FRAGMENT_CONVERT_TO_TAB);
    }

    @Override
    public void onCreateNewParty(CharSequence partyName, int numberInParty, EpicuriCustomer customer) {
        throw new UnsupportedOperationException();
    }

    // called to convert the quickorder into a tab
    @Override
    public void onCreateNewSession(CharSequence partyName, int numberInParty, EpicuriCustomer customer, String[] tables, String serviceId) {
        WebServiceTask task = new WebServiceTask(this, new ConvertToTabWebServiceCall(session.getId(), partyName.toString(),
                numberInParty, customer, tables, serviceId));
        task.setIndicatorText("Converting to tab");
        task.execute();
    }

    /**
     * mark a diner as highlighted.  This should highlight them on the table view, show them on the top left
     * and either show their pending orders (if in detail view) or allow new orders if in menu mode
     *
     * @param diner The diner which is selected, or null to deselect
     */
    @Override
    public void onDinerClick(EpicuriSessionDetail.Diner diner) {
        selectedDiner = diner;
//		orderAdapter.selectDiner(diner);
        Fragment fragmentByTag = adapter.findFragmentByTag(TAB_ORDERS);
        if (fragmentByTag != null && fragmentByTag instanceof SessionOrdersFragment) {
            ((SessionOrdersFragment) fragmentByTag).selectDiner(diner);
            if (null != diner) {
                pager.setCurrentItem(adapter.getPositionOfFragmentByTag(TAB_ORDERS));
            }
        }
    }

    @Override public void onConfirmChanges() {
        Fragment fragmentByTag = adapter.findFragmentByTag(TAB_ORDERS);

        if (fragmentByTag != null && fragmentByTag instanceof SessionOrdersFragment) {
            reshowTab = flag ? 1 : 0;
            reshowTab = 1;
            ((SessionOrdersFragment) fragmentByTag).confirmAssignChanges();
        }
    }

    @Override public void onDeselectAll() {
        Fragment fragmentByTag = adapter.findFragmentByTag(TAB_ORDERS);

        if (fragmentByTag != null && fragmentByTag instanceof SessionOrdersFragment) {
            ((SessionOrdersFragment) fragmentByTag).deselectAll();
        }
    }

    @Override public void ordersToTab() {
        Fragment fragmentByTag = adapter.findFragmentByTag(TAB_ORDERS);

        if (fragmentByTag != null && fragmentByTag instanceof SessionOrdersFragment) {
            ((SessionOrdersFragment) fragmentByTag).ordersToTab();
        }
    }

    boolean handleUpOrBack() {
        if (session != null && session.isAdHoc() && !session.isClosed()) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard this ad-hoc session?")
                    .setMessage("This will discard the order and delete any queued items")
                    .setNegativeButton("Do nothing", null)
                    .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeSession(false);
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        if (handleUpOrBack()) return;
        if (isTaskRoot()) {
            Intent i = new Intent(this, HubActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } else {
            super.onBackPressed();
        }
    }
}
