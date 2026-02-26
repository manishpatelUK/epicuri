package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EpicuriPreferenceActivity extends AppCompatActivity {

    static boolean manuallySet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_frame);

        if (null == savedInstanceState) {
            manuallySet = true;
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new EpicuriPreferenceFragment(), null)
                    .commit();
        }
    }

    public static class EpicuriPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.network);
        }
    }

    public static String getUrlPrefix(Context context) {
        String[] environments = context.getResources().getStringArray(R.array.environments);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String environment = sharedPref.getString(GlobalSettings.KEY_PREF_ENVIRONMENT, environments[0]);

        if (context.getString(R.string.custom).equals(environment)) {
            return sharedPref.getString(GlobalSettings.KEY_PREF_URL_PREFIX, "");
        }

        int environmentIndex = findEnvironment(environments, environment);
        return context.getResources().getStringArray(R.array.environmentPrefixes)[environmentIndex];
    }

    public static String getWebPrefix(Context context) {
        String[] environments = context.getResources().getStringArray(R.array.environments);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String environment = sharedPref.getString(GlobalSettings.KEY_PREF_ENVIRONMENT, environments[0]);

        if (context.getString(R.string.custom).equals(environment)) {
            return sharedPref.getString(GlobalSettings.KEY_PREF_KITCHEN_VIEW_PREFIX, "");
        }

        int environmentIndex = findEnvironment(environments, environment);
        return context.getResources().getStringArray(R.array.environmentWebPrefixes)[environmentIndex];
    }

    private static int findEnvironment(String[] environments, String environment) {
        int environmentIndex = 0;
        for (int i = 0; i < environments.length; i++) {
            String e = environments[i];
            if (e.equals(environment)) {
                environmentIndex = i;
                break;
            }
        }
        return environmentIndex;
    }
}
