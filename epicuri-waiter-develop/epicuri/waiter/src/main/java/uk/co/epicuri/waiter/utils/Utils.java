package uk.co.epicuri.waiter.utils;

import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.TimeZone;

import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.PartyAdapter;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriParty;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;

/**
 * Created by Home on 7/11/16.
 */
public class Utils {

    public static PowerManager.WakeLock keepUnlocked(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "INFO");
        wl.acquire(600000);
        return wl;
    }

    public static void enableLock(PowerManager.WakeLock wl) {
        try {
            if(wl.isHeld()) {
                wl.release();
            }
        } catch (RuntimeException ex){
            ex.printStackTrace();
        } catch (Throwable e){
            //catch wakelock exception
        }
    }

    public static void checkTimezone(final Context context, EpicuriRestaurant restaurant, TextView timezoneMismatch) {
        if (restaurant.getTimezone().hasSameRules(TimeZone.getDefault())) {
            timezoneMismatch.setVisibility(View.GONE);
        } else {
            timezoneMismatch.setVisibility(View.VISIBLE);
            timezoneMismatch.setText(context.getString(R.string.timezone_mismatch_message, restaurant.getTimezone().getDisplayName()));
            timezoneMismatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                    context.startActivity(intent);
                }
            });
        }
    }

    public static void reloadWaitingListSelection(PartyAdapter waitingListAdapter, ListView waitingListView,
                                            AdapterView.OnItemClickListener waitingListClickListener,
                                            EpicuriParty partyToSeat, EpicuriCustomer.Checkin selectedCheckin,
                                            ActionMode checkinActionMode, ActionMode partyActionMode) {
        if (waitingListAdapter == null) return;
        for (int i = 0; i < waitingListAdapter.getCount(); i++) {
            Object o = waitingListAdapter.getItem(i);
            if (o instanceof EpicuriParty) {
                EpicuriParty p = (EpicuriParty) o;
                if (p.getId().equals(GlobalSettings.autoselectPartyId)) {
                    waitingListView.setItemChecked(i, true);
                    waitingListClickListener.onItemClick(null, null, i, GlobalSettings.autoselectPartyId.hashCode());
                    GlobalSettings.autoselectPartyId = "-1";
                    break;
                } else if (p.equals(partyToSeat)) {
                    waitingListView.setItemChecked(i, true);
                    break;
                }
            } else if (o instanceof EpicuriCustomer.Checkin) {
                if (null != selectedCheckin && selectedCheckin.equals(o)) {
                    waitingListView.setItemChecked(i, true);
                    break;
                }
            }
        }
        if (waitingListView != null && waitingListView.getCheckedItemPosition() == AdapterView.INVALID_POSITION) {
            // for whatever reason the selected row is no longer there, stop action mode
            if (null != checkinActionMode) checkinActionMode.finish();
            if (null != partyActionMode) partyActionMode.finish();
        }
    }


    public static void initActionBar(AppCompatActivity activity){
        final ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void savePreferences(Context context, String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean readPreferences(Context context, String key) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sp.getBoolean(key, false);
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    public boolean isNewApiVersion(Context context){
        return EpicuriApplication.getInstance(context).getApiVersion() > 5;
    }


}
