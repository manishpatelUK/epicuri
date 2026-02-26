package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.Preferences;

public class PreferencesLoaderTemplate implements LoadTemplate<Preferences> {
    @Override public Uri getUri() {
        return EpicuriContent.PREFERENCES_URI;
    }

    @Override public Preferences parseJson(String jsonString) throws JSONException {
        Preferences prefs = new Preferences(new JSONObject(jsonString));
        return prefs;
    }
}
