package uk.co.epicuri.waiter.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.BuildConfig;
import uk.co.epicuri.waiter.LoginSessionService;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.PreferencesLoaderTemplate;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.Preferences;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.LoginWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGGER = "LoginActivity";

    public static final String RESTAURANT_PREFS = "restaurant";
    public static final String KEY_RESTAURANT_ID = "restaurantId";
    public static final String KEY_LOGIN_NAME = "loginName";
    public static final String KEY_LOGIN_USERNAME = "loginUsername";

    @InjectView(R.id.usernameText)
    EditText usernameText;
    @InjectView(R.id.passwordText)
    EditText passwordText;
    @InjectView(R.id.restaurantIdText)
    EditText restaurantText;
    @InjectView(R.id.findOutMoreText)
    TextView findOutMore;

    private String restaurantId = "0";
    public void deleteCache() {
        try {
            File dir = this.getCacheDir();
            deleteDir(dir);
        } catch (Exception ignored) {}
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ImageView logoImage = findViewById(R.id.logo);
        if (BuildConfig.NETWORK_SWITCH) {
            logoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LoginActivity.this, EpicuriPreferenceActivity.class);
                    startActivity(intent);
                }
            });
        }

        SharedPreferences prefs = getSharedPreferences(RESTAURANT_PREFS, Context.MODE_PRIVATE);
        try {
            restaurantId = prefs.getString(KEY_RESTAURANT_ID, "");
        } catch (ClassCastException ex) {
            Log.d(LOGGER, "Could not parse restaurant id (restaurant has migrated?)");
            restaurantId = "";
        }

        logoImage.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onLongClick(View v) {
                StringBuilder messageBuilder = new StringBuilder();
                if(restaurantId != null && !restaurantId.isEmpty()){
                    messageBuilder.append("Your restaurant ID is [").append(restaurantId).append("]\n");
                }
                messageBuilder.append("This will reset your epicuri app on this device.\n" +"Do you wish to continue");

                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Reset data")
                        .setMessage(messageBuilder.toString())
                        .setPositiveButton("Reset data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCache();
                                ((ActivityManager)LoginActivity.this.getSystemService(ACTIVITY_SERVICE))
                                        .clearApplicationUserData();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                return true;
            }
        });

        String previousLogin = prefs.getString(KEY_LOGIN_USERNAME, "");
        ButterKnife.inject(this);

        String findOutMoreText = getString(R.string.findOutMoreText);
        Spannable span = Spannable.Factory.getInstance().newSpannable(findOutMoreText);
        ClickableSpan cs = new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Uri tAndCLink = Uri.parse(getString(R.string.findOutMoreLink));
                Intent findOutMore = new Intent(Intent.ACTION_VIEW, tAndCLink);
                startActivity(Intent.createChooser(findOutMore, "Find out more"));
            }
        };
        span.setSpan(cs, 0, findOutMoreText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        findOutMore.setText(findOutMoreText);
        findOutMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.findOutMoreLink)));
                startActivity(intent);
            }
        });

        final EditText lastTextbox;
        if (restaurantId != null && !restaurantId.equals("0") && restaurantId.trim().length() > 0) {
            restaurantText.setText(String.format("Restaurant ID %s", restaurantId));
            restaurantText.setEnabled(false);
            lastTextbox = passwordText;
        } else {
            lastTextbox = restaurantText;
        }

        lastTextbox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        lastTextbox.setImeActionLabel(getString(R.string.login), 1);
        lastTextbox.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                attemptLogin();
                return true;
            }
        });
    }

    @OnClick(R.id.loginButton)
    void attemptLogin() {
        final String username = usernameText.getText().toString();
        final String password = passwordText.getText().toString();
        if (restaurantText.isEnabled()) {
            try {
                restaurantId = restaurantText.getText().toString().trim();
            } catch (NumberFormatException e) {
                new AlertDialog.Builder(LoginActivity.this).setTitle(
                        "Invalid ID").setMessage(
                        "Restaurant ID not valid: \"" + restaurantId + "\".").show();
                return;
            }
        }
        if (username.length() == 0) {
            usernameText.setError("Cannot be empty");
            return;
        } else {
            usernameText.setError(null);
        }
        if (password.length() == 0) {
            passwordText.setError("Cannot be empty");
            return;
        } else {
            passwordText.setError(null);
        }
        if (restaurantId == null || restaurantId.equals("0") || restaurantId.equals("")) {
            restaurantText.setError("Not recognised");
            return;
        } else {
            restaurantText.setError(null);
        }

        final WebServiceTask task = new WebServiceTask(LoginActivity.this,
                new LoginWebServiceCall(username, password, restaurantId), true);
        task.setIndicatorText("Logging in");
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {

            @Override
            public void onError(int code, String response) {
                if (LoginActivity.this.isFinishing()) return;
                if (code == 403) {
                    new AlertDialog.Builder(LoginActivity.this).setTitle(
                            "Incorrect Credentials").setMessage(
                            "Credentials not recognised").show();
                    return;
                } else if (code == 404) {
                    new AlertDialog.Builder(LoginActivity.this).setTitle(
                            "Restaurant Not Found").setMessage(
                            "Restaurant ID not valid: \"" + restaurantId + "\". Please contact Epicuri").show();
                    return;
                } else if (response == null || response.equals(WebServiceTask.CONNECTION_ERROR)) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("An error occurred")
                            .setMessage(
                                    response == null ? "No response from server or no internet connection" : response).show();
                    return;
                } else if (code == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Upgrade required")
                            .setMessage(R.string.upgrade_required)
                            .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent upgradeIntent = new Intent(Intent.ACTION_VIEW);
                                    upgradeIntent.setData(
                                            Uri.parse("market://details?id=uk.co.epicuri.waiter"));
                                    upgradeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    LoginActivity.this.startActivity(upgradeIntent);
                                }
                            })
                            .setNegativeButton("Dismiss", null)
                            .show();
                    return;
                }
                new AlertDialog.Builder(LoginActivity.this).setTitle(
                        "No connection").setMessage(
                        "Please check your internet/WiFi connection - could not connect to Epicuri").show();
            }
        });
        task.setOnCompleteListener(
                new WebServiceTask.OnSuccessListener() {

                    @Override
                    public void onSuccess(int code, String response) {
                        if (LoginActivity.this.isFinishing()) return;
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            final String token = responseJson.getString("AuthKey");
                            final String pin = responseJson.getString("Pin");
                            final String name = responseJson.getString("Name");
                            final boolean isManager = responseJson.getBoolean("Manager");
                            final String role = responseJson.getString("Role");
                            final String id = responseJson.getString("Id");

                            // save restaurant ID for next use
                            SharedPreferences prefs = getSharedPreferences(RESTAURANT_PREFS,
                                    Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putString(KEY_RESTAURANT_ID, restaurantId)
                                    .putString(KEY_LOGIN_NAME, name)
                                    .putString(KEY_LOGIN_USERNAME, username)
                                    .commit();

                            SharedPreferences sp = getSharedPreferences(
                                    GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
                            sp.edit()
                                    .putString(GlobalSettings.PREF_KEY_TOKEN, token)
                                    .putString(GlobalSettings.PREF_KEY_PIN, pin)
                                    .putString(GlobalSettings.PREF_KEY_NAME, name)
                                    .putString(GlobalSettings.PREF_KEY_USERNAME, username)
                                    .putBoolean(GlobalSettings.PREF_KEY_MANAGER, isManager)
                                    .putString(GlobalSettings.PREF_KEY_ROLE, role)
                                    .putString(GlobalSettings.PREF_KEY_ID, id)
                                    .commit();

                            SharedPreferences quickSwitchSharedPrefs = getSharedPreferences(GlobalSettings.PREF_APP_QUICK_SWITCH, Context.MODE_PRIVATE);

                            Set<String> quickSwitchUsers = quickSwitchSharedPrefs.getStringSet(GlobalSettings.PREF_KEY_USER_LIST, new HashSet<String>());

                            String user = String.format(getString(R.string.name_username_format), name, username);
                            if (!quickSwitchUsers.contains(user)) {
                                quickSwitchUsers.add(user);
                                quickSwitchSharedPrefs.edit()
                                        .putStringSet(GlobalSettings.PREF_KEY_USER_LIST, quickSwitchUsers)
                                        .apply();

                                quickSwitchSharedPrefs.edit()
                                        .putString(KEY_RESTAURANT_ID + username, restaurantId)
                                        .commit();

                                quickSwitchSharedPrefs.edit()
                                        .putString(GlobalSettings.PREF_KEY_TOKEN + username, token)
                                        .putString(GlobalSettings.PREF_KEY_PIN + username, pin)
                                        .putString(GlobalSettings.PREF_KEY_NAME + username, name)
                                        .putString(GlobalSettings.PREF_KEY_USERNAME + username, username)
                                        .putBoolean(GlobalSettings.PREF_KEY_MANAGER + username, isManager)
                                        .putString(GlobalSettings.PREF_KEY_ROLE + username, role)
                                        .putString(GlobalSettings.PREF_KEY_ID + username, id)
                                        .apply();
                            }

                            boundService.unlock();

                            getSupportLoaderManager().initLoader(GlobalSettings.LOADER_PREFERENCES, null,
                                    preferencesLoaderCallbacks);
                            return;
                        } catch (JSONException e) {
                            Log.e(LOGGER, "Broke on response", e);
                        }
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            Toast.makeText(LoginActivity.this, responseJson.getString("Message"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } catch (JSONException e) {
                            Log.e(LOGGER, "Broke creating responseJson", e);
                        }
                        // something went wrong
                        Toast.makeText(LoginActivity.this, "Something went wrong, code " + code,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        task.execute();
    }

    private void finishLogin() {
        Intent startEpicuriIntent = new Intent(LoginActivity.this,
                HubActivity.class);
        startEpicuriIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startEpicuriIntent);
    }

    private final LoaderManager.LoaderCallbacks<LoaderWrapper<Preferences>> preferencesLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<Preferences>>() {
                @NonNull
                @Override
                public Loader<LoaderWrapper<Preferences>> onCreateLoader(int id, @Nullable Bundle args) {
                    return new EpicuriLoader<>(LoginActivity.this, new PreferencesLoaderTemplate());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<LoaderWrapper<Preferences>> loader, LoaderWrapper<Preferences> data) {
                    if (data == null || data.getPayload() == null) return;
                    LocalSettings.getInstance(LoginActivity.this).cachePreferences(data.getPayload());
                    finishLogin();
                }

                @Override
                public void onLoaderReset(@NonNull Loader<LoaderWrapper<Preferences>> loader) {

                }
            };

    LoginSessionService boundService = null;
    boolean bound = false;

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LoginSessionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LoginSessionService.LocalBinder binder = (LoginSessionService.LocalBinder) service;
            boundService = binder.getService();
            bound = true;
            boundService.clearLogin();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundService = null;
            bound = false;
        }
    };
}
