package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriFloor;

public class FloorLoaderTemplate implements LoadTemplate<List<EpicuriFloor>> {
	
	@Override
	public Uri getUri() {
		return EpicuriContent.FLOOR_URI;
	}
	
	@Override
	public List<EpicuriFloor> parseJson(String jsonString) throws JSONException {
		JSONArray floorsJson = new JSONArray(jsonString);
		List<EpicuriFloor> floors = new ArrayList<EpicuriFloor>(floorsJson.length());
		for(int i=0; i<floorsJson.length(); i++){
			JSONObject floorJson = floorsJson.getJSONObject(i);
			floors.add(new EpicuriFloor(floorJson));
		}
		return floors;
	}


}
