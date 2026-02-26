package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriLogin;

public class LoginLoaderTemplate implements LoadTemplate<ArrayList<EpicuriLogin>> {

	@Override
	public Uri getUri() {
		return EpicuriContent.LOGIN_URI;
	}

	@Override
	public ArrayList<EpicuriLogin> parseJson(String jsonString) throws JSONException {
		JSONArray loginArrayJson = new JSONArray(jsonString);
		
		ArrayList<EpicuriLogin> response = new ArrayList<EpicuriLogin>(loginArrayJson.length());
		for(int i=0; i<loginArrayJson.length(); i++){
			response.add(new EpicuriLogin(loginArrayJson.getJSONObject(i)));
		}
		return response;
	}


}
