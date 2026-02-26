package uk.co.epicuri.waiter.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.BuildConfig;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.FloorPlanFragmentAdapter;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.HubListener;
import uk.co.epicuri.waiter.interfaces.NewPartyDialogListener;
import uk.co.epicuri.waiter.interfaces.OnEventsChangedListener;
import uk.co.epicuri.waiter.interfaces.OnSessionChangedListener;
import uk.co.epicuri.waiter.interfaces.OnTableChangeListener;
import uk.co.epicuri.waiter.interfaces.OnUnseatedChangedListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.CheckinLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.FloorLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.HubEventsLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.HubWaitingListLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ReservationsLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.RestaurantLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ServiceLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.TakeawaysLoaderTemplate;
import uk.co.epicuri.waiter.model.BadgesSingleton;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriParty;
import uk.co.epicuri.waiter.model.EpicuriReservation;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriService;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.printing.PowerConnectionReceiver;
import uk.co.epicuri.waiter.printing.PrintQueueActivity;
import uk.co.epicuri.waiter.printing.PrintQueueService;
import uk.co.epicuri.waiter.ui.menueditor.EditMenuActivity;
import uk.co.epicuri.waiter.ui.menueditor.PartyDetailsFragment;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.AcknowledgeNotificationWebServiceCall;
import uk.co.epicuri.waiter.webservice.DelaySessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeletePartyWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.NewPartyWebServiceCall;
import uk.co.epicuri.waiter.webservice.RejectReservationWebServiceCall;
import uk.co.epicuri.waiter.webservice.SeatPartyWebServiceCall;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class HubActivity extends EpicuriBaseActivity implements NewPartyDialogListener, OnTableChangeListener, HubListener {

    private static final String LOGGER = "HUBACTIVITY";
    public static final String TAB_EVENTS = "Actions";
    public static final String TAB_SESSION = "Sessions";
    public static final String TAB_UNSEATED = "Parties & Tabs";
    private static final String INITIAL_TAB = "initialTab";
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.left_drawer)
    ListView drawerList;
    @InjectView(R.id.floorPager)
    CustomViewPager floorAnimator;
    @InjectView(R.id.tabStrip)
    PagerTabStrip floorTabStrip;
    @InjectView(R.id.timezoneMismatch)
    TextView timezoneMismatch;
    @InjectView(R.id.progressBar)
    @Optional
    ProgressBar progressBar;

    @InjectView(R.id.tabs_view)
    @Optional
    View tabsView;

    @InjectView(R.id.tabs)
    TabLayout tabLayout;

    @InjectView(R.id.viewPager)
    ViewPager pager;
    private FloorPlanFragmentAdapter floorplanAdapter = null;
    private Spinner serviceSpinner;

    private List<EpicuriSessionDetail> sessions = null;
    private List<EpicuriFloor> floors = null;
    private List<EpicuriService> services = null;
    protected Map<String, EpicuriMenu.Printer> printers;
    private EpicuriRestaurant restaurant = null;

    private String mUrlPrefix;
    private Animation anim;

    private boolean tableHighlighted = false;
    private ActionBarDrawerToggle drawerToggle;

    private ActionMode partyActionMode;
    private EpicuriParty partyToSeat = null;
    private EpicuriSessionDetail sessionToReseat = null;
    private boolean showFloor;
    private PowerConnectionReceiver powerConnectionReceiver;
    private ArrayList<EpicuriSessionDetail> takeaways;
    private ArrayList<EpicuriReservation> reservations;
    private ActionMode reseatSessionActionMode;
    private TextView takeawayActionViewText;
    private TextView reservationActionViewText;
    String versionName;
    private OnEventsChangedListener eventsListener;
    private OnSessionChangedListener sessionListener;
    private OnUnseatedChangedListener unseatedListener;
    public static boolean updatedParty;
    private EpicuriCustomer.Checkin selectedCheckin = null;
    private HubPagerAdapter pagerAdapter;
    private String initialTab;
    public static void setNewParty(boolean updatedParty) {
        HubActivity.updatedParty = updatedParty;
    }

    public EpicuriCustomer.Checkin getSelectedCheckin() {
        return selectedCheckin;
    }

    public void setSelectedCheckin(EpicuriCustomer.Checkin selectedCheckin) {
        this.selectedCheckin = selectedCheckin;
    }

    public boolean isShowFloor() {
        return showFloor;
    }

    public void setShowFloor(boolean showFloor) {
        this.showFloor = showFloor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        showFloor = false;
        floorSwitch();
        pagerAdapter = new HubPagerAdapter(getSupportFragmentManager());
        setupTabs();
        progressBar.bringToFront();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            floorTabStrip.setVisibility(View.GONE);
            floorAnimator.setVisibility(View.GONE);
            tabsView.setVisibility(View.VISIBLE);
        }

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (progressBar != null) progressBar.setVisibility(View.GONE);

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            Log.e(LOGGER, "Error getting version name", e);
            Log.i(LOGGER, "Default version name to " + BuildConfig.VERSION_NAME);
            versionName = BuildConfig.VERSION_NAME;
        }

        final Intent serviceIntent = new Intent(this, PrintQueueService.class);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        Log.i(LOGGER, "Version code " + versionName);

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(10);

        //
        // set up navigation drawer
        //
        drawerList.setAdapter(new NavAdapter(this));
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                onNavItemSelected((NavItem) adapter.getItemAtPosition(position));
                drawerLayout.closeDrawer(drawerList);
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        mUrlPrefix = EpicuriPreferenceActivity.getUrlPrefix(this);

        serviceSpinner = new Spinner(new ContextThemeWrapper(HubActivity.this, R.style.EpicuriDark));
        serviceSpinner.setMinimumHeight((int) (48 * getResources().getDisplayMetrics().density));

        floorplanAdapter = new FloorPlanFragmentAdapter(getSupportFragmentManager(), null);
        floorAnimator.setAdapter(floorplanAdapter);
        floorTabStrip.setTabIndicatorColorResource(R.color.orange);

        if (null != savedInstanceState) {
            startPrintQueue = savedInstanceState.getBoolean(GlobalSettings.EXTRA_START_PRINT_QUEUE);
        }

        for (Uri contentUri : new Uri[]{EpicuriContent.EVENT_URI, EpicuriContent.PARTIES_URI, EpicuriContent.CHECKIN_URI, EpicuriContent.SESSION_URI, EpicuriContent.FLOOR_URI}) {
            UpdateService.requestUpdate(this, contentUri);
        }

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                finishActionModes();
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        // if starting up for the first time,
        if (null == savedInstanceState) {
            initialTab = TAB_SESSION;

            if (getIntent().hasExtra(GlobalSettings.EXTRA_TAB)) {
                initialTab = getIntent().getStringExtra(GlobalSettings.EXTRA_TAB);
            }
        }else {
            initialTab = savedInstanceState.getString(INITIAL_TAB);
            if (initialTab == null){
                initialTab = TAB_SESSION;
            }
        }

        LoaderManager lm = getSupportLoaderManager();
        restartEventLoader();
        restartPartiesLoader();
        restartCheckinsLoader();

        lm.initLoader(GlobalSettings.LOADER_SERVICES, null, new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriService>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriService>>> onCreateLoader(int id, Bundle arguments) {
                return new EpicuriLoader<>(HubActivity.this, new ServiceLoaderTemplate());
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriService>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriService>> data) {
                if (null == data) {
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_SERVICES HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                services = data.getPayload();
                for (EpicuriService service : services) {
                    if (service.sessionType != null && service.sessionType.equals("ADHOC")) {
                        LocalSettings.getInstance(HubActivity.this).cacheQuickOrderServiceMenuId(service.menuId);
                    }
                }
                ArrayAdapter<EpicuriService> serviceAdapter
                        = new ArrayAdapter<>(serviceSpinner.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1, services);
                serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                serviceSpinner.setAdapter(serviceAdapter);
                if (null != partyActionMode) {
                    partyActionMode.invalidate();
                }
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriService>>> data) {
            }

        });

        lm.initLoader(GlobalSettings.LOADER_SESSIONS, null, new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int arg0, Bundle arg1) {
                EpicuriLoader<ArrayList<EpicuriSessionDetail>> loader = new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(HubActivity.this, new SessionsLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
                if (null == data) {
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_SESSIONS HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessions = data.getPayload();
                if (null != pagerAdapter.findFragmentByTag(TAB_SESSION)) {

                    List<EpicuriSessionDetail> sessionsWithoutTabs = new LinkedList<>();
                    int nonTabbedSessions = 0;
                    for (EpicuriSessionDetail s : data.getPayload()) {
                        // exclude "at the bar" sessions
                        if (s.getType() != EpicuriSessionDetail.SessionType.DINE || (null != s.getTables() && !s.isTab())) {
                            sessionsWithoutTabs.add(s);
                        } else {
                            if ((s.getType() != EpicuriSessionDetail.SessionType.COLLECTION
                                    || s.getType() != EpicuriSessionDetail.SessionType.DELIVERY
                                    || s.getType() != EpicuriSessionDetail.SessionType.DINE)
                                    && !s.isTab()) {
                                nonTabbedSessions++;
                            }
                        }
                    }
                    BadgesSingleton.setNonTabbedSessions(nonTabbedSessions);
                    sessionListener.addSessions(sessionsWithoutTabs);
                }

                if (pagerAdapter.findFragmentByTag(TAB_EVENTS) != null)
                    eventsListener.onEventsChangedListener(data.getPayload());
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> arg0) {
            }

        });

        lm.initLoader(GlobalSettings.LOADER_RESTAURANT, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriRestaurant>>() {

            @Override
            public Loader<LoaderWrapper<EpicuriRestaurant>> onCreateLoader(int id, Bundle args) {
                EpicuriLoader<EpicuriRestaurant> loader = new EpicuriLoader<EpicuriRestaurant>(HubActivity.this, new RestaurantLoaderTemplate());
                loader.setAutoRefreshPeriod(1000 * 60 * 10); // refresh restaurant every 10 mins
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<EpicuriRestaurant>> loader,
                                       LoaderWrapper<EpicuriRestaurant> data) {
                if (null == data) { // nothing returned, ignore
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this,
                            "LOADER_RESTAURANT HubActivity error loading data",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                restaurant = data.getPayload();
                LocalSettings.getInstance(HubActivity.this).cacheRestaurant(restaurant);
                drawerList.setAdapter(new NavAdapter(HubActivity.this));
                Utils.checkTimezone(HubActivity.this, restaurant, timezoneMismatch);
                invalidateOptionsMenu();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<EpicuriRestaurant>> loader) {
            }
        });

        lm.initLoader(GlobalSettings.LOADER_RESERVATIONS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriReservation>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> onCreateLoader(int id, Bundle args) {
                EpicuriLoader<ArrayList<EpicuriReservation>> loader = new EpicuriLoader<ArrayList<EpicuriReservation>>(HubActivity.this, new ReservationsLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> loader, LoaderWrapper<ArrayList<EpicuriReservation>> data) {
                if (null == data) { // nothing returned, ignore
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_RESERVATIONS HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                reservations = data.getPayload();

                invalidateOptionsMenu();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriReservation>>> loader) {
            }
        });

        lm.initLoader(GlobalSettings.LOADER_TAKEAWAYS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id, Bundle args) {
                EpicuriLoader<ArrayList<EpicuriSessionDetail>> loader = new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(HubActivity.this, new TakeawaysLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader, LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
                if (null == data) {
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_TAKEAWAYS HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                takeaways = data.getPayload();
                invalidateOptionsMenu();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader) {
            }
        });

        lm.initLoader(GlobalSettings.LOADER_PRINTERS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
            @Override
            public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
                return new OneOffLoader<>(HubActivity.this, new PrinterLoaderTemplate());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> result) {
                if (result != null) {
                    printers = new HashMap<>(result.size());
                    for (EpicuriMenu.Printer p : result) {
                        printers.put(p.getId(), p);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
                // don't care
            }
        });

    }

    public void restartEventLoader() {
        getSupportLoaderManager().restartLoader(GlobalSettings.LOADER_EVENTS, null, new LoaderCallbacks<LoaderWrapper<List<EpicuriEvent.HubNotification>>>() {

            @Override
            public Loader<LoaderWrapper<List<EpicuriEvent.HubNotification>>> onCreateLoader(int id, Bundle arguments) {
                EpicuriLoader<List<EpicuriEvent.HubNotification>> loader = new EpicuriLoader<List<EpicuriEvent.HubNotification>>(HubActivity.this, new HubEventsLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriEvent.HubNotification>>> loader,
                                       LoaderWrapper<List<EpicuriEvent.HubNotification>> data) {
                if (null == data) { // nothing returned, set loading state
                    if (pagerAdapter.findFragmentByTag(TAB_EVENTS) != null)
                        eventsListener.onEventsAddedListener(null);
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_EVENTS HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pagerAdapter.findFragmentByTag(TAB_EVENTS) != null)
                    eventsListener.onEventsAddedListener(data.getPayload());
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriEvent.HubNotification>>> arg0) { }
        });

    }

    private void restartPartiesLoader() {
        getSupportLoaderManager().initLoader(GlobalSettings.LOADER_PARTIES, null, new LoaderCallbacks<LoaderWrapper<List<EpicuriParty>>>() {

            @Override
            public Loader<LoaderWrapper<List<EpicuriParty>>> onCreateLoader(int id, Bundle arguments) {
                EpicuriLoader<List<EpicuriParty>> loader = new EpicuriLoader<List<EpicuriParty>>(HubActivity.this, new HubWaitingListLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriParty>>> loader,
                                       LoaderWrapper<List<EpicuriParty>> data) {
                if (null == data) { // nothing returned, set "loading" state
                    if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null)
                        unseatedListener.setParties(null);
                    return;
                } else if (data.isError()) {
                    Toast.makeText(HubActivity.this, "LOADER_PARTIES HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clear existing selection (if any)
                if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null) {
                    int checkedItem = unseatedListener.getCheckedItemPosition();
                    if (unseatedListener.getCheckedItemPosition() != AdapterView.INVALID_POSITION) {
                        unseatedListener.setItemChecked(checkedItem, false);
                    }
                }

                List<EpicuriParty> allParties = new ArrayList<>();
                for (EpicuriParty party : data.getPayload()) {
                    if (party.getSessionType() == null || !party.getSessionType().equals(EpicuriParty.TYPE_ADHOC)) {
                        allParties.add(party);
                    }
                }
                if ( pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null)
                    unseatedListener.setParties(allParties);

                int walkInCount = 0;
                for (EpicuriParty party : allParties) {
                    if (party.getSessionId() == null || party.getSessionId().equals("-1") || party.getSessionId().equals("0")) {
                        walkInCount++;
                    }
                }
                BadgesSingleton.setWalkInCount(walkInCount);
                int unseatedTabPos = pagerAdapter.getPositionOfFragmentByTag(TAB_UNSEATED);
                tabLayout.getTabAt(unseatedTabPos).setCustomView(getTabView(unseatedTabPos, BadgesSingleton.getTabsBadgeNumber()));
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriParty>>> data) {
            }

        });
    }

    private void restartCheckinsLoader() {
        getSupportLoaderManager().initLoader(GlobalSettings.LOADER_CHECKINS, null, new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> onCreateLoader(int id, Bundle arguments) {
                EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>> loader = new EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>>(HubActivity.this, new CheckinLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>> data) {
                if (null == data && pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null) { // nothing returned, set "loading" state
                    unseatedListener.setCheckins(null);
                    return;
                } else if (data.isError()) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(HubActivity.this, "LOADER_CHECKINS HubActivity error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clear existing selection (if any)
                if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null) {
                    int checkedItem = unseatedListener.getCheckedItemPosition();
                    if (unseatedListener.getCheckedItemPosition() != AdapterView.INVALID_POSITION) {
                        unseatedListener.setItemChecked(checkedItem, false);
                    }
                }

                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null)
                    unseatedListener.setCheckins(data.getPayload());

                BadgesSingleton.setCheckInsCount(data.getPayload().size());

                int unseatedTabPos = pagerAdapter.getPositionOfFragmentByTag(TAB_UNSEATED);
                tabLayout.getTabAt(unseatedTabPos).setCustomView(getTabView(unseatedTabPos, BadgesSingleton.getTabsBadgeNumber()));
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> data) {
            }

        });
    }

    private void setupTabs() {
        pagerAdapter = new HubPagerAdapter(getSupportFragmentManager());
        pager.removeAllViews();
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(pager);
        HubEventsFragment event = HubEventsFragment.newInstance(this);
        HubSessionsFragment session = HubSessionsFragment.newInstance(this);
        HubUnseatedFragment unseated = HubUnseatedFragment.newInstance(this);
        pagerAdapter.addPage(TAB_EVENTS, event);
        pagerAdapter.addPage(TAB_SESSION, session);
        pagerAdapter.addPage(TAB_UNSEATED, unseated);
        pagerAdapter.notifyDataSetChanged();
        eventsListener = event;
        sessionListener = session;
        unseatedListener = unseated;
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) tab.setCustomView(getTabView(i, 0));
        }

        if(initialTab != null){
            pager.setCurrentItem(pagerAdapter.getPositionOfFragmentByTag(initialTab));
            initialTab = TAB_SESSION;
        }
    }


    public View getTabView(int position, int notifications) {
        View v = tabLayout.getTabAt(position).getCustomView();
        if (v == null) {
            v = LayoutInflater.from(this).inflate(R.layout.tab_indicator, null);
        }
        v.setPadding(0,0,0,0);
        TextView title = v.findViewById(R.id.title);
        title.setText(pagerAdapter.getTagByPosition(position));

        View badgeContainer = v.findViewById(R.id.badge_container);
        if (notifications <= 0) {
            badgeContainer.setVisibility(View.GONE);
        } else {
            badgeContainer.setVisibility(View.VISIBLE);
            TextView badgeView = v.findViewById(R.id.seat_number);
            badgeView.setText(String.valueOf(notifications));
            badgeView.setAnimation(anim);
        }

        return v;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(GlobalSettings.EXTRA_START_PRINT_QUEUE, startPrintQueue);
        outState.putString(INITIAL_TAB, pagerAdapter.getTagByPosition(pager.getCurrentItem()));
        super.onSaveInstanceState(outState);
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // called to move a session between tables
        if (intent.getBooleanExtra(GlobalSettings.EXTRA_RESEAT_SESSION, false)) {
            String sessionId = intent.getStringExtra(GlobalSettings.EXTRA_SESSION_ID);
            if (sessionId != null && !sessionId.equals("-1")) {
                for (EpicuriSessionDetail sessionDetail : sessions) {
                    if (sessionDetail.getId().equals(sessionId)) {
                        showReseatUi(sessionDetail);
                        return;
                    }
                }
            }
        }
        if (intent.hasExtra(GlobalSettings.EXTRA_TAB)) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            String switchTab = intent.getStringExtra(GlobalSettings.EXTRA_TAB);
            pager.setCurrentItem(pagerAdapter.getPositionOfFragmentByTag(switchTab));
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finishActionModes();
        if (floorAnimator != null) {
            getSupportLoaderManager().restartLoader(GlobalSettings.LOADER_FLOORS, null, new LoaderCallbacks<LoaderWrapper<List<EpicuriFloor>>>() {
                @Override
                public Loader<LoaderWrapper<List<EpicuriFloor>>> onCreateLoader(int arg0,
                                                                                Bundle arg1) {
                    EpicuriLoader<List<EpicuriFloor>> loader = new EpicuriLoader<>(HubActivity.this, new FloorLoaderTemplate());
                    loader.setAutoRefreshPeriod(60000);
                    return loader;
                }

                @Override
                public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriFloor>>> arg0,
                                           LoaderWrapper<List<EpicuriFloor>> result) {
                    if (result == null || result.getPayload() == null) return;
                    floors = result.getPayload();
                    floorplanAdapter.setFloors(floors, floorAnimator.getId());
                }

                @Override
                public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriFloor>>> arg0) {
                }

            });
        }
        if (null != restaurant) Utils.checkTimezone(HubActivity.this, restaurant, timezoneMismatch);

        for (Uri contentUri : new Uri[]{EpicuriContent.EVENT_URI, EpicuriContent.SESSION_URI}) {
            UpdateService.requestUpdate(this, contentUri);
        }

        if (updatedParty) {
            UpdateService.expireData(this, new Uri[]{EpicuriContent.PARTIES_URI});
            updatedParty = false;
        }
        setupTabs();
    }

    private void finishActionModes() {
        eventsListener.finishActionMode();
        sessionListener.finishActionMode();
        unseatedListener.finishActionMode();
        if (null != partyActionMode)
            partyActionMode.finish();
    }

    @Override
    public void onTableSelected(String tableId) {
        try {
            EpicuriSessionDetail session = EpicuriSessionDetail.getSessionForTable(sessions, tableId);
            if (null != session) {
                launchSession(session.getId());
            } else {
                showNewPartyDialog(new String[]{tableId});
            }
        } catch (IllegalArgumentException e) {
            //nothing
        }
    }

    @Override
    public void onNoTableSelected() {
        sessionListener.showSession(null);
    }

    @Override
    public void onHighlightedTablesChanged(boolean selected) {
        if (reseatSessionActionMode != null) {
            this.tableHighlighted = selected;
            reseatSessionActionMode.invalidate();
        }
    }

    private long lastTap = 0;
    private boolean isDoubleTap() {
        if(lastTap != 0 && System.currentTimeMillis() - lastTap < 500){
            return true;
        } else {
            lastTap = System.currentTimeMillis();
            return false;
        }
    }

    public class SeatPartyActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (null == partyToSeat) return false;

            if (null == floors) {
                partyToSeat = null;
                if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null) {
                    int currentItem = unseatedListener.getCheckedItemPosition();
                    if (AdapterView.INVALID_POSITION != currentItem) {
                        unseatedListener.setItemChecked(currentItem, false);
                    }
                }
                return false;
            }

            getMenuInflater().inflate(R.menu.action_seatparty, menu);
            floorSelection(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.menu_partyLeft).setVisible(partyToSeat.getSessionId() == null || partyToSeat.getSessionId().equals("0") || partyToSeat.getSessionId().equals("-1"));
            menu.findItem(R.id.menu_viewSession).setVisible(partyToSeat.getSessionId() != null && partyToSeat.getSessionId().length() > 2);
            menu.findItem(R.id.menu_createSession).setVisible(partyToSeat.getSessionId() == null || partyToSeat.getSessionId().equals("0") || partyToSeat.getSessionId().equals("-1"));
            menu.findItem(R.id.menu_edit).setVisible(partyToSeat.getReservationTime() != null);

            // session not already started AND more than one service to choose from => show spinner
            if ((partyToSeat.getSessionId() == null || partyToSeat.getSessionId().equals("0") || partyToSeat.getSessionId().equals("-1")) && serviceSpinner.getCount() > 1) {
                mode.setCustomView(serviceSpinner);
            } else {
                mode.setCustomView(null);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(isDoubleTap()) return false;
            switch (item.getItemId()) {
                case R.id.menu_seatParty: {
                    int currentFloor = floorAnimator.getCurrentItem();
                    FloorplanFragment floorplan = (FloorplanFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + floorAnimator.getId() + ":" + currentFloor);
                    Map<String, Boolean> selectedTables = floorplan.getSelectedTables();
                    if (null == selectedTables || selectedTables.size() == 0) {
                        new AlertDialog.Builder(HubActivity.this)
                                .setTitle("Choose at least one table")
                                .setMessage("You need to choose a table before seating diners")
                                .setPositiveButton("Dismiss", null)
                                .show();
                        return true;
                    }
                    ArrayList<String> tables = new ArrayList<>(selectedTables.size());
                    for (Map.Entry<String, Boolean> entry : selectedTables.entrySet()) {
                        if (entry.getValue()) {
                            tables.add(entry.getKey());
                        }
                    }
                    if (tables.size() == 0) {
                        new AlertDialog.Builder(HubActivity.this)
                                .setTitle("Choose at least one table")
                                .setMessage("You need to choose a table before seating diners")
                                .setPositiveButton("Dismiss", null)
                                .show();
                        return true;
                    }
                    EpicuriService service = (EpicuriService) serviceSpinner.getItemAtPosition(serviceSpinner.getSelectedItemPosition());
                    if (null == service) {
                        Toast.makeText(HubActivity.this, "Service not loaded", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    final EpicuriParty persistParty = partyToSeat;
                    if (partyToSeat == null) return false;
                    WebServiceTask task = new WebServiceTask(HubActivity.this, new SeatPartyWebServiceCall(partyToSeat, tables, service.id));
                    task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                        @Override
                        public void onSuccess(int code, String response) {
                            UpdateService.expireData(HubActivity.this, new Uri[]{EpicuriContent.PARTIES_URI});
                            if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null)
                                unseatedListener.setParties(null);
                            try {
                                String sessionId;
                                if (persistParty.getSessionId() != null && !persistParty.getSessionId().equals("0") && !persistParty.getSessionId().equals("-1")) {
                                    sessionId = persistParty.getSessionId();
                                } else {
                                    JSONObject responseJSON = new JSONObject(response);
                                    sessionId = responseJSON.getString("Id");
                                }
                                Intent sessionIntent = new Intent(HubActivity.this, SeatedSessionActivity.class);
                                sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
                                startActivity(sessionIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    task.setIndicatorText(getString(R.string.webservicetask_alertbody));
                    task.execute();

                    mode.finish();
                    return true;
                }
                case R.id.menu_edit: {
                    editReservation(partyToSeat.getReservationId());
                    mode.finish();
                    return true;
                }
                case R.id.menu_partyLeft: {
                    final EpicuriParty persistParty = partyToSeat;
                    new AlertDialog.Builder(HubActivity.this)
                            .setTitle(R.string.partyLeft_title)
                            .setMessage(R.string.partyLeft_prompt)
                            .setPositiveButton(R.string.partyLeft_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeParty(persistParty, false);
                                }
                            })
                            .setNegativeButton(R.string.partyLeft_negative, null)
                            .show();
                    mode.finish();
                    return true;
                }
                case R.id.menu_viewSession: {
                    launchSession(partyToSeat.getSessionId());
                    mode.finish();
                    return true;
                }
                case R.id.menu_createSession: {
                    EpicuriService service = (EpicuriService) serviceSpinner.getItemAtPosition(serviceSpinner.getSelectedItemPosition());
                    if (null == service) {
                        Toast.makeText(HubActivity.this, "Cannot determine service", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    final EpicuriParty persistParty = partyToSeat;
                    if (partyToSeat == null) return false;
                    WebServiceTask task = new WebServiceTask(HubActivity.this, new SeatPartyWebServiceCall(partyToSeat, new ArrayList<String>(0), service.id));
                    task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

                        @Override
                        public void onSuccess(int code, String response) {
                            try {
                                String sessionId;
                                if (persistParty.getSessionId() != null && !persistParty.getSessionId().equals("0") && !persistParty.getSessionId().equals("-1")) {
                                    sessionId = persistParty.getSessionId();
                                } else {
                                    JSONObject responseJSON = new JSONObject(response);
                                    sessionId = responseJSON.getString("Id");
                                }

                                Intent sessionIntent = new Intent(HubActivity.this, SeatedSessionActivity.class);
                                sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
                                sessionIntent.putExtra(SeatedSessionActivity.EXTRA_ADD_ITEMS, true);
                                startActivity(sessionIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    task.setIndicatorText(getString(R.string.webservicetask_alertbody));
                    task.execute();
                    mode.finish();
                    return true;
                }
                case R.id.menu_detailsParty:
                    return partyDetails();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            partyToSeat = null;
            floorSelection(false);
            if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null) {
                int currentItem = unseatedListener.getCheckedItemPosition();
                if (AdapterView.INVALID_POSITION != currentItem) {
                    unseatedListener.setItemChecked(currentItem, false);
                }
            }
            partyActionMode = null;

        }
    }

    public boolean partyDetails() {
        PartyDetailsFragment frag = PartyDetailsFragment.newInstance(partyToSeat
                            .getSessionId(), partyToSeat.getId(), partyToSeat.getPartyName(),
                    partyToSeat.getNumberInParty(), false);
        frag.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_PARTY);
        return true;
    }

    public void floorSelection(boolean b) {
        floorplanAdapter.setTableSelectionMode(b, floorAnimator.getId());
    }

    private class ReseatSessionActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (null == sessionToReseat) {
                Log.e("HubActivity", "Null party! HubActivity.ReseatSessionActionMode.onCreateActionMode");
                return false;
            }

            if (null == floors) {
                sessionToReseat = null;

                int currentItem = sessionListener.getCheckedItemPosition();
                if (AdapterView.INVALID_POSITION != currentItem) {
                    sessionListener.setItemChecked(currentItem, false);
                }
                return false;
            }

            getMenuInflater().inflate(R.menu.action_reseatsession, menu);
            floorplanAdapter.setTableSelectionMode(floorAnimator.getId(), sessionToReseat);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            MenuItem item = menu.findItem(R.id.menu_reseatSession);
            if (item != null) {
                item.setTitle(tableHighlighted ? "Reseat Party" : "Convert to Tab");
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_reseatSession: {
                    int currentFloor = floorAnimator.getCurrentItem();
                    FloorplanFragment floorplan = (FloorplanFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + floorAnimator.getId() + ":" + currentFloor);
                    Map<String, Boolean> selectedTables = floorplan.getSelectedTables();
                    if (null == selectedTables || selectedTables.size() == 0) {
                        // prevent 'seat party' until tables are selected
                        new AlertDialog.Builder(HubActivity.this)
                                .setTitle("Convert to tab")
                                .setMessage("This will convert table to tab and will lose all schedule data. Continue?")
                                .setNegativeButton("Do nothing", null)
                                .setPositiveButton("Convert to tab", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        reseatSession(sessionToReseat, null);
                                    }
                                })
                                .show();
                        return true;
                    }

                    ArrayList<String> tables = new ArrayList<>(selectedTables.size());
                    for (Map.Entry<String, Boolean> entry : selectedTables.entrySet()) {
                        if (entry.getValue()) {
                            tables.add(entry.getKey());
                        }
                    }
                    reseatSession(sessionToReseat, tables);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            tableHighlighted = false;
            partyToSeat = null;
            floorSelection(false);
            if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) == null)
                return;

            int currentItem = unseatedListener.getCheckedItemPosition();
            if (AdapterView.INVALID_POSITION != currentItem) {
                // deselect current item
                unseatedListener.setItemChecked(currentItem, false);
            }
            reseatSessionActionMode = null;

        }
    }

    private void reseatSession(EpicuriSessionDetail session, ArrayList<String> tables) {
        if (null == tables) tables = new ArrayList<>(0);

        WebServiceTask task = new WebServiceTask(HubActivity.this, new SeatPartyWebServiceCall(session, tables));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                UpdateService.expireData(HubActivity.this, new Uri[]{EpicuriContent.SESSION_URI});
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(HubActivity.this, response, Toast.LENGTH_LONG).show();
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();

        if (null != reseatSessionActionMode) reseatSessionActionMode.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_hub, menu);
        LocalSettings localSettings = LocalSettings.getInstance(this);

        menu.findItem(R.id.menu_addTakeaway).setVisible(restaurant != null && !restaurant.getTakeawayTypes().isEmpty());
        menu.findItem(R.id.floor_toggle).setVisible(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if(localSettings.getCachedRestaurant() != null) {
            menu.findItem(R.id.long_tab).setVisible(localSettings.getCachedRestaurant().deferredSessionsEnabled());
        }

        if (menu.findItem(R.id.kick_drawer) != null) {
            menu.findItem(R.id.kick_drawer).setVisible(localSettings.isAllowed(WaiterAppFeature.MANUAL_DRAWER_KICK));
        }
        View reservationAction = menu.findItem(R.id.menu_reservations).getActionView();
        ((ImageView) reservationAction.findViewById(R.id.icon)).setImageResource(R.drawable.ic_action_reservations);
        reservationActionViewText = ((TextView) reservationAction.findViewById(R.id.count_text));
        reservationAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HubActivity.this, ReservationsActivity.class);
                startActivity(intent);
            }
        });

        List<EpicuriReservation> pendingReservations = new ArrayList<>(1);
        Date now = new Date();

        if (reservations != null)
            for (EpicuriReservation reservation : reservations) {
                if ((reservation.getArrivedTime() != null && reservation.getArrivedTime().before
                        (now)) || reservation.isDeleted())
                    continue;

                pendingReservations.add(reservation);
            }

        if (!pendingReservations.isEmpty()) {
            reservationActionViewText.setText(String.valueOf(pendingReservations.size()));
            reservationActionViewText.setBackgroundResource(R.drawable.white_circle_bg);
            reservationActionViewText.setVisibility(View.VISIBLE);
        } else {
            reservationActionViewText.setVisibility(View.GONE);
        }

        View takeawayAction = menu.findItem(R.id.menu_addTakeaway).getActionView();
        ((ImageView) takeawayAction.findViewById(R.id.icon)).setImageResource(R.drawable.ic_action_takeaways);
        takeawayActionViewText = (TextView) takeawayAction.findViewById(R.id.count_text);
        takeawayAction.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takeawayManagerIntent = new Intent(HubActivity.this, TakeawaysActivity.class);
                startActivity(takeawayManagerIntent);
            }
        });

        List<EpicuriSessionDetail> pending = new ArrayList<>(1);

        if (takeaways != null)
            for (EpicuriSessionDetail session : takeaways) {
                if (session.getExpectedTime() != null && session.getExpectedTime().before(now))
                    continue;

                if (session.getType() != EpicuriSessionDetail.SessionType.DINE
                        && !session.isDeleted()) {
                    pending.add(session);
                }
            }

        if (!pending.isEmpty()) {
            takeawayActionViewText.setText(String.valueOf(pending.size()));
            takeawayActionViewText.setVisibility(View.VISIBLE);
        } else {
            takeawayActionViewText.setVisibility(View.GONE);
        }

        MenuItem quickOrder = menu.findItem(R.id.menu_quick);
        if (null != quickOrder) quickOrder.setVisible(true);

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (null == restaurant) {
            Toast.makeText(this, "Data not loaded yet", Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_quick: {
                showQuickOrder();
                return true;
            }
            case R.id.menu_addDiner: {
                showNewPartyDialog(null);
                return true;
            }
            case R.id.menu_addTakeaway: {
                Intent takeawayManagerIntent = new Intent(this, TakeawaysActivity.class);
                startActivity(takeawayManagerIntent);
                return true;
            }
            case R.id.menu_reservations: {
                Intent intent = new Intent(this, ReservationsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menu_refresh: {
                refresh();
                return true;
            }
            case R.id.floor_toggle: {
                showFloor = !showFloor;
                floorSwitch();
                return true;
            }
            case R.id.kick_drawer: {
                conditionalDrawerKick(this, printers, null);
                return true;
            }
            case R.id.lock_screen: {
                Intent lockScreenIntent = new Intent(this, LockActivity.class);
                lockScreenIntent.putExtra(LockActivity.EXTRA_POPUP_SWITCH, true);
                startActivity(lockScreenIntent);
                return true;
            }
            case R.id.long_tab: {
                Intent intent = new Intent(this, DeferredSessionsActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh(){
        for (Uri contentUri : new Uri[]{EpicuriContent.EVENT_URI, EpicuriContent.PARTIES_URI, EpicuriContent.CHECKIN_URI, EpicuriContent.SESSION_URI, EpicuriContent.FLOOR_URI}) {
            UpdateService.requestUpdate(this, contentUri);
        }
    }

    public void floorSwitch() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (showFloor) {
                floorTabStrip.setVisibility(View.VISIBLE);
                floorAnimator.setVisibility(View.VISIBLE);
                tabsView.setVisibility(View.GONE);
            } else {
                floorTabStrip.setVisibility(View.GONE);
                floorAnimator.setVisibility(View.GONE);
                tabsView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void launchSession(String sessionId) {
        launchSession(sessionId, false);
    }

    public void launchSession(String sessionId, boolean addItems) {
        EpicuriSessionDetail session = EpicuriSessionDetail.getSessionForId(sessions, sessionId);
        Intent intent;
        switch (session.getType()) {
            case DINE: {
                intent = new Intent(getApplicationContext(), SeatedSessionActivity.class);
                if (addItems) {
                    intent.putExtra(SeatedSessionActivity.EXTRA_ADD_ITEMS, true);
                }
                break;
            }
            case COLLECTION:
            case DELIVERY: {
                intent = new Intent(getApplicationContext(), TakeawayActivity.class);
                break;
            }
            default:
                throw new IllegalStateException(String.format(Locale.UK, "session type %s is not recognised", session.getType()));
        }
        intent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
        startActivity(intent);
    }

    @Override
    public void showReseatUi(EpicuriSessionDetail session) {
        sessionToReseat = session;
        reseatSessionActionMode = startSupportActionMode(new ReseatSessionActionMode());
    }

    @Override
    public void editReservation(String reservationId) {
        Intent editReservationIntent = new Intent(this, ReservationEditActivity.class);
        editReservationIntent.putExtra(GlobalSettings.EXTRA_RESERVATION_ID, reservationId);
        editReservationIntent.setAction(ReservationEditActivity.ACTION_EDIT);
        startActivity(editReservationIntent);
    }

    @Override
    public void showNewPartyDialog(String[] tableIds) {
        NewPartyFragment frag = NewPartyFragment.newInstance(selectedCheckin, tableIds, null, null, true);
        frag.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_PARTY);
    }

    private void showQuickOrder() {
        Intent intent = new Intent(this, QuickOrderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreateNewParty(CharSequence partyName, int numberInParty, EpicuriCustomer customer) {
        WebServiceTask task = new WebServiceTask(this, new NewPartyWebServiceCall(partyName.toString(), numberInParty, customer), true);
        task.setIndicatorText("Creating party");
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                UpdateService.expireData(HubActivity.this, new Uri[]{EpicuriContent.PARTIES_URI, EpicuriContent.CHECKIN_URI});
                if (null != unseatedListener) {
                    // not available in landscape
                    unseatedListener.setCheckins(null);
                    unseatedListener.setParties(null);
                }
                /*
                 show unseated lists tab; select newly created party once refresh has happened
				 */
                pager.setCurrentItem(pagerAdapter.getPositionOfFragmentByTag(TAB_UNSEATED));
                try {
                    JSONObject responseJson = new JSONObject(response);
                    GlobalSettings.autoselectPartyId = responseJson.getString("Id");
                } catch (JSONException e) {
                    // don't care, just carry on as normal
                }
            }
        });
        task.execute();
    }

    @Override
    public void onCreateNewSession(CharSequence partyName, int numberInParty, EpicuriCustomer customer, String[] tables, final String serviceId) {
        final WebServiceTask task = new WebServiceTask(this, new NewPartyWebServiceCall(partyName.toString(), numberInParty, customer, tables, serviceId), true);
        task.setIndicatorText("Creating session");
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONObject responseJSON = new JSONObject(response);
                    String sessionId = responseJSON.getString("SessionId");
                    if (sessionId != null && !sessionId.equals("0") && !sessionId.equals("-1")) {
                        Intent sessionIntent = new Intent(HubActivity.this, SeatedSessionActivity.class);
                        sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
                        sessionIntent.putExtra(SeatedSessionActivity.EXTRA_ADD_ITEMS, true);
                        startActivity(sessionIntent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(HubActivity.this, "Cannot parse response", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        task.execute();
    }

    private void removeParty(EpicuriParty party, boolean blackMark) {
        WebServiceCall webServiceCall;
        boolean normalParty = party.getReservationTime() == null;
        boolean unacceptedReservation = !party.isAccepted();
        if (normalParty) {
            webServiceCall = new DeletePartyWebServiceCall(party.getId(), blackMark);
        } else if (unacceptedReservation) {
            webServiceCall = new RejectReservationWebServiceCall(party.getId(), "Reservation was not accepted");
        } else {
            // accepted reservation -> delete reservation
            webServiceCall = new DeleteReservationWebServiceCall(party.getId(), blackMark);
        }
        WebServiceTask task = new WebServiceTask(this, webServiceCall);
        task.setIndicatorText("Removing Party");
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                UpdateService.expireData(HubActivity.this, new Uri[]{EpicuriContent.PARTIES_URI});
                if (pagerAdapter.findFragmentByTag(TAB_UNSEATED) != null)
                    unseatedListener.setParties(null);
            }
        });
        task.execute();
    }

    public void postpone(EpicuriEvent.Notification notification) {
        WebServiceTask task = new WebServiceTask(
                this,
                new DelaySessionWebServiceCall(
                        notification,
                        ((EpicuriEvent.HubNotification) notification).getSessionId(),
                        ((EpicuriEvent.HubNotification) notification).getSessionDelay()
                ));
        task.setIndicatorText("Postponing");
        task.execute();
    }

    @Override
    public void acknowledge(EpicuriEvent.Notification notification) {
        WebServiceTask task = new WebServiceTask(this, new AcknowledgeNotificationWebServiceCall(notification, ((EpicuriEvent.HubNotification) notification).getSessionId()), true);
        task.setIndicatorText("Acknowledging");
        task.execute();
    }

    private void showAboutDialog() {
        StringBuilder aboutMessage = new StringBuilder();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.RESTAURANT_PREFS, Context.MODE_PRIVATE);
        String restaurantId = prefs.getString(LoginActivity.KEY_RESTAURANT_ID, "0");

        EpicuriRestaurant r = LocalSettings.getInstance(this).getCachedRestaurant();

        aboutMessage.append(String.format("Restaurant Id %s\n", restaurantId));
        aboutMessage.append(String.format("Server URL: %s\n", mUrlPrefix));
        aboutMessage.append("\n").append(String.format("This Epicuri device belongs to:\n%s\n%s\n%s", r.getName(), r.getAddress().toString(), r.getPhoneNumber()));
        aboutMessage.append("\n\nEpicuri version ").append(versionName);
        aboutMessage.append("\n\n").append(getString(R.string.aboutLibraries));

        String loginName = prefs.getString(LoginActivity.KEY_LOGIN_USERNAME, null);
        aboutMessage.append("\n\n").append(String.format("You are logged in as %s", null == loginName ? "Unknown" : loginName));

        View view = getLayoutInflater().inflate(R.layout.dialog_about, null, false);
        TextView tv = view.findViewById(android.R.id.text1);
        tv.setAutoLinkMask(Linkify.WEB_URLS);
        tv.setText(aboutMessage, BufferType.SPANNABLE);

        new AlertDialog.Builder(this)
                .setTitle("About")
                .setView(view)
                .setPositiveButton("Dismiss", null)
                .show();
    }

    public void highlightNoTables() {
        floorplanAdapter.highlightTables(null, floorAnimator.getId());
    }

    public void highlightTablesForSession(String sessionId) {
        if (null == sessions) return;
        for (EpicuriSessionDetail s : sessions) {
            if (s.getId().equals(sessionId)) {
                EpicuriTable[] tableObjects = s.getTables();
                if (null == tableObjects) {
                    highlightNoTables();
                    return;
                }
                String[] tableIds = new String[tableObjects.length];
                for (int i = 0; i < tableIds.length; i++) {
                    tableIds[i] = tableObjects[i].getId();
                }
                List<Integer> floorsContainingTable = floorplanAdapter.highlightTables(tableIds, floorAnimator.getId());
                if (floorsContainingTable.isEmpty()) {
                    return;
                }
                int currentFloor = floorAnimator.getCurrentItem();
                if (!floorsContainingTable.contains(currentFloor)) {
                    floorAnimator.setCurrentItem(floorsContainingTable.get(0), true);
                }
                return;
            }
        }
    }

    private boolean startPrintQueue = true;
    private boolean mBound = false;
    private PrintQueueService mPrintQueue = null;
    private AlertDialog startPrintDialog = null;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PrintQueueService.PrintQueueBinder binder = (PrintQueueService.PrintQueueBinder) service;
            mPrintQueue = binder.getService();
            mBound = true;

            SharedPreferences prefs = getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, MODE_PRIVATE);

            if (!mPrintQueue.isRunning() && mBound && prefs.getBoolean(GlobalSettings.PREF_KEY_PRINT_QUEUE, false)) {
                mPrintQueue.startProcessingPrintQueue();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onPause() {
        PrintQueueService.mPrintQueue = mPrintQueue;
        PrintQueueService.mBound = mBound;
        try {
            if (startPrintDialog != null && startPrintDialog.isShowing()) {
                startPrintDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        try {
            if (startPrintDialog != null && startPrintDialog.isShowing()) {
                startPrintDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private long backPressedTime = 0;
    private static final long BACK_DOUBLE_PRESS_PERIOD = 10 * 1000; // 10 seconds

    @Override
    public void onBackPressed() {
        long now = new Date().getTime();
        if (now - backPressedTime < BACK_DOUBLE_PRESS_PERIOD) {
            super.onBackPressed();
        } else {
            backPressedTime = now;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onBind() {
        super.onBind();
        drawerList.setAdapter(new NavAdapter(this));
    }

    private enum NavItem {
        MENU_MANAGER(R.string.title_activity_menumanager),
        FLOORPLAN_MANAGER(R.string.title_activity_floorplanmanager),
        QUICK_ORDER(R.string.menu_quickorder),
        ORDER_SCHEDULE(R.string.menu_kitchen),
        BUSINESS_INTELLIGENCE(R.string.menu_businessintelligence),
        LOGIN_MANAGER(R.string.menu_loginmanager),
        CLOSE_SERVICE(R.string.menu_cash_up),
        PAYMENT_SENSE(R.string.payment_sense),
        PRINT_QUEUE(R.string.menu_printqueue),
        SESSION_HISTORY(R.string.menu_session_history),
        LOGOUT(R.string.logout),
        SUPPORT(R.string.menu_support),
        ABOUT(R.string.menu_about);

        final int name_resource;

        NavItem(int name) {
            this.name_resource = name;
        }
    }

    private class NavAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final ArrayList<NavItem> items;

        public NavAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            items = new ArrayList<>();

            LocalSettings localSettings = LocalSettings.getInstance(HubActivity.this);

            if (localSettings.isAllowed(WaiterAppFeature.CASH_UP) || localSettings.isAllowed(WaiterAppFeature.CASH_UP_SIMULATION)) {
                items.add(NavItem.CLOSE_SERVICE);
            }

            if (localSettings.isAllowed(WaiterAppFeature.MENU_MANAGER))
                items.add(NavItem.MENU_MANAGER);

            if (localSettings.isAllowed(WaiterAppFeature.LOGIN_MANAGER)) {
                items.add(NavItem.LOGIN_MANAGER);
            }

            if (localSettings.isAllowed(WaiterAppFeature.FLOOR_PLAN_MANAGER)) {
                items.add(NavItem.FLOORPLAN_MANAGER);
            }

            if (localSettings.isAllowed(WaiterAppFeature.PORTAL)) {
                items.add(NavItem.ORDER_SCHEDULE);
            }

            if (localSettings.isAllowed(WaiterAppFeature.SESSION_HISTORY)) {
                items.add(NavItem.SESSION_HISTORY);
            }
            if (restaurant != null && restaurant.hasPaymentSenseAdjustmentType() && !TextUtils.isEmpty(restaurant.getPaymentsenseHost())) {
                items.add(NavItem.PAYMENT_SENSE);
            }

            items.add(NavItem.PRINT_QUEUE);
            if (localSettings.isAllowed(WaiterAppFeature.PORTAL)) {
                items.add(NavItem.BUSINESS_INTELLIGENCE);
            }
            items.add(NavItem.SUPPORT);
            items.add(NavItem.ABOUT);
            items.add(NavItem.LOGOUT);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public NavItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(getString(getItem(position).name_resource));
            return convertView;
        }
    }

    private void onNavItemSelected(NavItem item) {
        switch (item) {
            case MENU_MANAGER: {
                Intent intent = new Intent(this, EditMenuActivity.class);
                startActivity(intent);
                break;
            }
            case PRINT_QUEUE: {
                Intent intent = new Intent(this, PrintQueueActivity.class);
                startActivity(intent);
                break;
            }
            case FLOORPLAN_MANAGER: {
                if(floors == null) { //could happen on slow load
                    Toast.makeText(this, "Data is still loading. Please wait & try again in a few seconds.", Toast.LENGTH_LONG).show();
                    break;
                }
                int floorIndex = floorAnimator.getCurrentItem();
                EpicuriFloor floor = floors.get(floorIndex);

                Intent startFloorplanIntent = new Intent(this, FloorplanManagerActivity.class);
                startFloorplanIntent.putExtra(GlobalSettings.EXTRA_FLOOR, floor.getId());
                startActivity(startFloorplanIntent);
                break;
            }
            case CLOSE_SERVICE: {
                Intent intent = new Intent(this, CashUpActivity.class);
                startActivity(intent);
                break;
            }
            case SESSION_HISTORY: {
                Intent intent = new Intent(this, SessionHistoryActivity.class);
                startActivity(intent);
                break;
            }
            case ORDER_SCHEDULE: {
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_TYPE, WebViewActivity.Type.KITCHEN);
                startActivity(intent);
                break;
            }
            case BUSINESS_INTELLIGENCE: {
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_TYPE, WebViewActivity.Type.BI);
                startActivity(intent);
                break;
            }
            case LOGIN_MANAGER: {
                Intent intent = new Intent(this, LoginManagerActivity.class);
                startActivity(intent);
                break;
            }
            case SUPPORT: {
                Intent intent = new Intent(this, SupportActivity.class);
                startActivity(intent);
                break;
            }
            case PAYMENT_SENSE: {
                Intent intent = new Intent(this, PaymentSenseActivity.class);
                startActivity(intent);
                break;
            }
            case LOGOUT: {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.logout_title)
                        .setMessage(R.string.logout_prompt)
                        .setPositiveButton(R.string.logout_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PrintQueueService.mPrintQueue = mPrintQueue;
                                PrintQueueService.mBound = mBound;
                                TokenManager.logout(HubActivity.this);
                                Utils.savePreferences(HubActivity.this,
                                        GlobalSettings.IS_NOT_FIRST_TIME_LOGGED_IN, true);
                            }
                        })
                        .setNegativeButton(R.string.logout_negative, null)
                        .show();
                break;
            }
            case ABOUT: {
                showAboutDialog();
                break;
            }
        }
    }

    public EpicuriParty getPartyToSeat() {
        return partyToSeat;
    }

    public void setPartyToSeat(EpicuriParty partyToSeat) {
        this.partyToSeat = partyToSeat;
    }

    public ActionMode getPartyActionMode() {
        return partyActionMode;
    }

    public void setPartyActionMode(ActionMode partyActionMode) {
        this.partyActionMode = partyActionMode;
    }
}