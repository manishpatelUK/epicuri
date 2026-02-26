package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;

public class RestaurantLoaderTemplate implements LoadTemplate<EpicuriRestaurant> {
	
	@Override
	public Uri getUri() {
		return EpicuriContent.RESTAURANT_URI;
	}

	@Override
	public EpicuriRestaurant parseJson(String jsonString) throws JSONException {
		JSONObject rJson = new JSONObject(jsonString);
		return new EpicuriRestaurant(rJson);
	}
}
