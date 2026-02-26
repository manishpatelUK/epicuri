package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.LoginSessionService;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CustomDialogAdapter;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.CheckinLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.HubWaitingListLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.BadgesSingleton;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriParty;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.QuickOrderLandscapeState;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.SendDeviceInformationWebServiceCall;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.VoidSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.ui.LoginActivity.KEY_LOGIN_NAME;
import static uk.co.epicuri.waiter.ui.LoginActivity.KEY_LOGIN_USERNAME;
import static uk.co.epicuri.waiter.ui.LoginActivity.KEY_RESTAURANT_ID;
import static uk.co.epicuri.waiter.ui.LoginActivity.RESTAURANT_PREFS;

public class LockActivity extends AppCompatActivity {

    @InjectView(R.id.logout_button)
    Button logoutButton;
    @InjectView(R.id.first_star)
    ImageView firstStar;
    @InjectView(R.id.stars)
    LinearLayout stars;
    @InjectView(R.id.second_star)
    ImageView secondStar;
    @InjectView(R.id.third_star)
    ImageView thirdStar;
    @InjectView(R.id.fourth_star)
    ImageView fourthStar;

    @InjectView(R.id.check_ins)
    TextView checkIns;
    @InjectView(R.id.new_orders)
    TextView seatedNoOrder;
    @InjectView(R.id.bill_requests)
    TextView seatedBillRequest;

    @InjectView(R.id.check_in_icon)
    ImageView checkInsIcon;
    @InjectView(R.id.no_orders_icon)
    ImageView seatedNoOrderIcon;
    @InjectView(R.id.bill_request_icon)
    ImageView seatedBillRequestIcon;

    @InjectView(R.id.currently_logged_in)
    TextView currentUser;
    @InjectView(R.id.quick_switch)
    Button quickSwitchBtn;

    private int numberOfRetries = 3;
    private String pin = "";

    private int billRequestsCount = 0;
    private int ordersCount = 0;

    public static final int LOADER_SESSIONS = 1;
    public static final int LOADER_PARTIES = 3;
    public static final int LOADER_CHECKINS = 6;
    public static final String FILE_ORDERS_FORMAT = "%s/orders_%s.ser";
    public static final String FILE_SESSION_FORMAT = "%s/session_%s.ser";
    public static final String EXTRA_POPUP_SWITCH = "uk.co.epicuri.waiter.popupSwitch";

    private List<EpicuriSessionDetail> sessions = null;

    private LoaderManager lm;
    LoaderManager.LoaderCallbacks<LoaderWrapper<List<EpicuriParty>>> partyCallback;
    LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> sessionCallback;
    LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> checkInsCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lockscreen);
        ButterKnife.inject(this);

        // init loaders
        lm = getSupportLoaderManager();
        sessionCallback = new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int arg0, Bundle arg1) {
                EpicuriLoader<ArrayList<EpicuriSessionDetail>> loader = new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(LockActivity.this, new SessionsLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
                if (null == data) { // nothing returned, ignore
                    return;
                } else if (data.isError()) {
                    Toast.makeText(LockActivity.this, "error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessions = data.getPayload();

                billRequestsCount = 0;
                ordersCount = 0;

                //get seated sessions but exclude "at the bar" sessions(in progress sessions)
                for (EpicuriSessionDetail s : data.getPayload()) {
                    if (s.getStatusString().equals("Seated")) {
                        ordersCount++;
                    } else if (s.getType() == EpicuriSessionDetail.SessionType.DINE && !s.getName().equals("QuickOrder")&& s.isBillRequested()) {
                        billRequestsCount++;
                    }
                }
                seatedBillRequest.setText(billRequestsCount + "");
                seatedNoOrder.setText(ordersCount + "");

                //Toast.makeText(LockActivity.this, "Session Data Refreshed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> arg0) {
            }

        };
        lm.initLoader(LOADER_SESSIONS, null, sessionCallback);

        partyCallback = new LoaderManager.LoaderCallbacks<LoaderWrapper<List<EpicuriParty>>>() {

            @Override
            public Loader<LoaderWrapper<List<EpicuriParty>>> onCreateLoader(int id, Bundle arguments) {
                EpicuriLoader<List<EpicuriParty>> loader = new EpicuriLoader<List<EpicuriParty>>(LockActivity.this, new HubWaitingListLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<List<EpicuriParty>>> loader,
                                       LoaderWrapper<List<EpicuriParty>> data) {
                if (null == data) { // nothing returned, set "loading" state
                    return;
                } else if (data.isError()) {
                    Toast.makeText(LockActivity.this, "error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                int walkInCount = 0;
                for (EpicuriParty party : data.getPayload()) {
                    if (party.getSessionId() == null || party.getSessionId().equals("-1") || party.getSessionId().equals("0")) {
                        walkInCount++;
                    }
                }
                BadgesSingleton.setWalkInCount(walkInCount);
                checkIns.setText(String.valueOf(BadgesSingleton.getTabsBadgeNumber()));
                //Toast.makeText(LockActivity.this, "People Checked In Refreshed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<List<EpicuriParty>>> data) {
            }

        };
        lm.initLoader(LOADER_PARTIES, null, partyCallback);

        checkInsCallback = new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> onCreateLoader(int id, Bundle arguments) {
                EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>> loader = new EpicuriLoader<>(LockActivity.this, new CheckinLoaderTemplate());
                loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader,
                                       LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>> data) {
                if (null == data) { // nothing returned, set "loading" state
                    return;
                } else if (data.isError()) {
                    Toast.makeText(LockActivity.this, "error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                BadgesSingleton.setCheckInsCount(data.getPayload().size());
                checkIns.setText(String.valueOf(BadgesSingleton.getTabsBadgeNumber()));

                //Toast.makeText(LockActivity.this, "People Checked In Refreshed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> data) {
            }

        };
        lm.initLoader(LOADER_CHECKINS, null, checkInsCallback);

        quickSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupQuickSwitchDialog();
            }
        });
    }

    private void popupQuickSwitchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LockActivity.this);
        builder.setTitle("Select user to switch");

        final SharedPreferences sharedPrefers = getSharedPreferences(GlobalSettings.PREF_APP_QUICK_SWITCH, Context.MODE_PRIVATE);
        final Set<String> users = sharedPrefers.getStringSet(GlobalSettings.PREF_KEY_USER_LIST, null);

        if (users != null && boundService != null && users.size() > 1) {
            String currentUser = String.format(getString(R.string.name_username_format), boundService.getLoggedInUser().getName(), boundService.getLoggedInUser().getUsername());
            int size = 0;
            for(String user : users){
                if(!user.equals(currentUser)) {
                    size++;
                }
            }

            final String[] usersArray = new String[size];
            int i = 0;
            for (String user : users) {
                if(!user.equals(currentUser)){
                    usersArray[i] = user;
                    i++;
                }
            }
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = getLayoutInflater().inflate(R.layout.dialog_custom_select, null);
            ListView listView = view.findViewById(R.id.customDialogList);
            CustomDialogAdapter adapter = new CustomDialogAdapter(this, usersArray);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int which, long itemId) {
                    String user = usersArray[which];
                    int start = user.indexOf('(')+1;
                    String username = user.substring(start, user.length()-1);
                    SharedPreferences sp = getSharedPreferences(
                            GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
                    SharedPreferences prefs = getSharedPreferences(RESTAURANT_PREFS,
                            Context.MODE_PRIVATE);
                    String token = sharedPrefers.getString(GlobalSettings.PREF_KEY_TOKEN + username, null);
                    String pin = sharedPrefers.getString(GlobalSettings.PREF_KEY_PIN + username, null);
                    String name = sharedPrefers.getString(GlobalSettings.PREF_KEY_NAME + username, null);
                    String id = sharedPrefers.getString(GlobalSettings.PREF_KEY_ID + username, null);
                    Boolean isManager = sharedPrefers.getBoolean(GlobalSettings.PREF_KEY_MANAGER + username, false);
                    String restaurantId = sharedPrefers.getString(KEY_RESTAURANT_ID + username, "0");

                    //saving for currentUser
                    String currentUser = prefs.getString(KEY_LOGIN_USERNAME, "");
                    if ((PendingOrderFragment.orders != null && PendingOrderFragment.orders.size() != 0)) {
                        saveOrders(currentUser, PendingOrderFragment.orders);
                    } else if (QuickOrderLandscapeFragment.quickOrderLandscapeState != null) {
                        saveOrders(currentUser, QuickOrderLandscapeFragment.quickOrderLandscapeState.getOrders());
                        saveSession(currentUser, QuickOrderLandscapeFragment.quickOrderLandscapeState);
                    } else if (SeatedSessionActivity.sessionId != null && !SeatedSessionActivity.sessionId.isEmpty()){
                        sharedPrefers.edit().putString(GlobalSettings.KEY_SESSION_ID + currentUser, SeatedSessionActivity.sessionId).apply();
                    }
                    TokenManager.logout(LockActivity.this);

                    prefs.edit()
                            .putString(KEY_RESTAURANT_ID, restaurantId)
                            .putString(KEY_LOGIN_NAME, name)
                            .putString(KEY_LOGIN_USERNAME, username)
                            .apply();

                    sp.edit()
                            .putString(GlobalSettings.PREF_KEY_TOKEN, token)
                            .putString(GlobalSettings.PREF_KEY_PIN, pin)
                            .putString(GlobalSettings.PREF_KEY_NAME, name)
                            .putString(GlobalSettings.PREF_KEY_USERNAME, username)
                            .putBoolean(GlobalSettings.PREF_KEY_MANAGER, isManager)
                            .putString(GlobalSettings.PREF_KEY_ID, id)
                            .apply();

                    boundService.clearLogin();
                    String sessionId = sharedPrefers.getString(GlobalSettings.KEY_SESSION_ID + username, "");
                    if (!sessionId.equals("")){
                        sharedPrefers.edit().putString(GlobalSettings.KEY_SESSION_ID + currentUser, "").apply();
                        Intent sessionIntent = new Intent(LockActivity.this,
                                SeatedSessionActivity.class);
                        sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, sessionId);
                        sessionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(sessionIntent);
                    }else {
                        try {
                            Intent qoActivity = QuickOrderActivity.newInstance(LockActivity.this, restoreOrders(username), restoreSession(username));
                            qoActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(qoActivity);
                        } catch (Exception e) {
                            Intent startEpicuriIntent = new Intent(LockActivity.this,
                                    HubActivity.class);

                            startEpicuriIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startEpicuriIntent);
                        }
                    }

                    dialog.dismiss();
                }
            });

            dialog.setContentView(view);
            dialog.show();

        } else {
            Toast.makeText(LockActivity.this, R.string.quick_switch_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSession(String username, QuickOrderLandscapeState quickOrderLandscapeState) {
        try{
            FileOutputStream fileSession = new FileOutputStream(String.format(FILE_SESSION_FORMAT, getFilesDir(), username));
            ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(fileSession));
            outputStream.writeObject(quickOrderLandscapeState);
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private QuickOrderLandscapeState restoreSession(String username){
        try {
            FileInputStream file = new FileInputStream(String.format(FILE_SESSION_FORMAT, getFilesDir(), username));
            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(file));
            QuickOrderLandscapeState sessionDetail = (QuickOrderLandscapeState) objectInputStream.readObject();
            objectInputStream.close();
            File fileRef = new File(String.format(FILE_SESSION_FORMAT, getFilesDir(), username));
            if (fileRef.exists()) fileRef.delete();
            return sessionDetail;
        }catch (Exception e){
            return null;
        }
    }

    private void saveOrders(String username, ArrayList<EpicuriOrderItem> orders) {
        try {
            FileOutputStream file = new FileOutputStream(String.format(FILE_ORDERS_FORMAT, getFilesDir(), username));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(file));
            objectOutputStream.writeObject(orders);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<EpicuriOrderItem> restoreOrders(String username) throws Exception {
        FileInputStream file = new FileInputStream(String.format(FILE_ORDERS_FORMAT, getFilesDir(), username));
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(file));
        ArrayList<EpicuriOrderItem> orders = (ArrayList<EpicuriOrderItem>) objectInputStream.readObject();
        objectInputStream.close();
        File fileRef = new File(String.format(FILE_ORDERS_FORMAT, getFilesDir(), username));
        if (fileRef.exists()) fileRef.delete();
        return orders;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, LoginSessionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    private LoginSessionService boundService;
    private boolean bound = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LoginSessionService.LocalBinder binder = (LoginSessionService.LocalBinder) service;
            boundService = binder.getService();

            //  		loggedInAsText.setText(String.format(getString(R.string.lockscreen_loggedInAs), boundService.getLoggedInUser().getName()));
            currentUser.setText(String.format(getString(R.string.lockscreen_loggedInAs), boundService.getLoggedInUser().getName()));
            bound = true;
            if(getIntent().hasExtra(EXTRA_POPUP_SWITCH)) {
                if(getIntent().getBooleanExtra(EXTRA_POPUP_SWITCH, false)) {
                    getIntent().removeExtra(EXTRA_POPUP_SWITCH);
                    popupQuickSwitchDialog();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundService = null;
            bound = false;
        }
    };

    @Override
    public void onBackPressed() {
        // Do nothing - cannot cancel this dialog
    }

    public void checkPin(CharSequence pinNumber) {
        if (pinNumber.toString().equals(boundService.getLoggedInUser().getPin())) {
            correctPin();
        } else {
            incorrectPin();
        }
    }

    public void correctPin() {
        Toast.makeText(this, "Correct pin. Resuming app", Toast.LENGTH_SHORT).show();
        boundService.unlock();
        WebServiceTask task = new WebServiceTask(this, new SendDeviceInformationWebServiceCall(this));
        task.execute();

        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void incorrectPin() {

        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shakeanimation);
        numberOfRetries -= 1;
        pin = "";

        if (numberOfRetries < 1) {
            logout();
        } else {
            //Toast.makeText(this, String.format("Sorry, that PIN is not correct. %d tries remaining", numberOfRetries), Toast.LENGTH_SHORT).show();
            stars.startAnimation(shake);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    firstStar.setVisibility(View.INVISIBLE);
                    secondStar.setVisibility(View.INVISIBLE);
                    thirdStar.setVisibility(View.INVISIBLE);
                    fourthStar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void logout(View v) {
        logout();
    }

    @OnClick(R.id.check_in_icon)
    void refreshCheckIns() {
        lm.restartLoader(LOADER_PARTIES, null, partyCallback);
        lm.restartLoader(LOADER_CHECKINS, null, checkInsCallback);
    }

    @OnClick(R.id.no_orders_icon)
    void refreshSeatedNoOrders() {
        lm.restartLoader(LOADER_SESSIONS, null, sessionCallback);
    }

    @OnClick(R.id.bill_request_icon)
    void refreshSeatedBillRequest() {
        lm.restartLoader(LOADER_SESSIONS, null, sessionCallback);
    }

    @OnClick(R.id.logout_button)
    void logout() {
        Toast.makeText(this, "Logging you out.", Toast.LENGTH_SHORT).show();
        voidSessions();
        TokenManager.logout(this);
        Utils.savePreferences(LockActivity.this,
                GlobalSettings.IS_NOT_FIRST_TIME_LOGGED_IN, true);

    }

    private void voidSessions() {
        String sessionId = null;
        if (QuickOrderLandscapeFragment.quickOrderLandscapeState != null) {
            EpicuriSessionDetail session = QuickOrderLandscapeFragment.quickOrderLandscapeState.getCurrentSession();
            if(session != null) {
                sessionId = session.getId();
            }
        }else if (SeatedSessionActivity.sessionId != null && !SeatedSessionActivity.sessionId.isEmpty() && SeatedSessionActivity.isAdHoc){
            sessionId = SeatedSessionActivity.sessionId;
        }

        if(sessionId != null) {
            WebServiceTask task = new WebServiceTask(this, new VoidSessionWebServiceCall(sessionId, "VOIDED BY USER", true));
            task.execute();
        }
    }

    public void handleKeyboardClick(View v) {
        TextView number = (TextView) ((RelativeLayout) v).getChildAt(0);
        pin += number.getText();
        setStar(pin.length());
        if (pin.length() > 3) checkPin(pin);
    }

    public void onKeyboardBack(View v) {
        if (pin.length() > 0) {
            removeStar(pin.length());
            pin = pin.substring(0, pin.length() - 1);
        }
    }

    public void onKeyboardClear(View v) {
        pin = "";
        firstStar.setVisibility(View.INVISIBLE);
        secondStar.setVisibility(View.INVISIBLE);
        thirdStar.setVisibility(View.INVISIBLE);
        fourthStar.setVisibility(View.INVISIBLE);
    }

    public void setStar(int number) {
        if (number == 1) {
            firstStar.setVisibility(View.VISIBLE);
        } else if (number == 2) {
            secondStar.setVisibility(View.VISIBLE);
        } else if (number == 3) {
            thirdStar.setVisibility(View.VISIBLE);
        } else if (number == 4) {
            fourthStar.setVisibility(View.VISIBLE);
        }
    }

    public void removeStar(int number) {
        if (number == 1) {
            firstStar.setVisibility(View.INVISIBLE);
        } else if (number == 2) {
            secondStar.setVisibility(View.INVISIBLE);
        } else if (number == 3) {
            thirdStar.setVisibility(View.INVISIBLE);
        } else if (number == 4) {
            fourthStar.setVisibility(View.INVISIBLE);
        }
    }
}
