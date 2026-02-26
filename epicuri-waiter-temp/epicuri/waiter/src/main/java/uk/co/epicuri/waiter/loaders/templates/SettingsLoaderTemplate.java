package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriSetting;

public class SettingsLoaderTemplate implements LoadTemplate<EpicuriSetting> {

	@Override
	public Uri getUri() {
		return EpicuriContent.SETTING_URI;
	}

	@Override
	public EpicuriSetting parseJson(String jsonString) throws JSONException {
		return new EpicuriSetting(new JSONObject(jsonString));
	}

}
