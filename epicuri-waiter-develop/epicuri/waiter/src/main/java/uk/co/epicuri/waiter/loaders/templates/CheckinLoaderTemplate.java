package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;

public class CheckinLoaderTemplate implements LoadTemplate<ArrayList<EpicuriCustomer.Checkin>> {
	@Override
	public Uri getUri() {
		return EpicuriContent.CHECKIN_URI;
	}
	
	@Override
	public ArrayList<EpicuriCustomer.Checkin> parseJson(String jsonString) throws JSONException {
		JSONArray checkinArrayJson = new JSONArray(jsonString);
		
		ArrayList<EpicuriCustomer.Checkin> response = new ArrayList<EpicuriCustomer.Checkin>(checkinArrayJson.length());
		for(int i=0; i<checkinArrayJson.length(); i++){
			response.add(new EpicuriCustomer.Checkin(checkinArrayJson.getJSONObject(i)));
		}
		return response;
	}


}
