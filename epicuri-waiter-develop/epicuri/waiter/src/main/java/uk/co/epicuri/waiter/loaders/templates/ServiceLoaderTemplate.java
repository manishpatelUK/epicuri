package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriService;

public class ServiceLoaderTemplate implements LoadTemplate<ArrayList<EpicuriService>> {
	private final boolean getSingle;
	private final Uri uri;
	
	public ServiceLoaderTemplate(long id) {
		uri = Uri.withAppendedPath(EpicuriContent.SERVICE_URI, String.valueOf(id));
		getSingle = true;
	}
	
	public ServiceLoaderTemplate() {
		uri = EpicuriContent.SERVICE_URI;
		getSingle = false;
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public ArrayList<EpicuriService> parseJson(String jsonString) throws JSONException {
		if(getSingle){
			JSONObject input = new JSONObject(jsonString);
			ArrayList<EpicuriService> response = new ArrayList<EpicuriService>(1);
			response.add(new EpicuriService(input));
			return response;
		} else {
			JSONArray serviceArrayJson = new JSONArray(jsonString);
			ArrayList<EpicuriService> response = new ArrayList<EpicuriService>(serviceArrayJson.length());
			for(int i=0; i<serviceArrayJson.length(); i++){
				response.add(new EpicuriService(serviceArrayJson.getJSONObject(i)));
			}
			return response;
		}
	}
}
