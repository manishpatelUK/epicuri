package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriTable;

public class TableLoaderTemplate implements LoadTemplate<ArrayList<EpicuriTable>> {
	
	@Override
	public Uri getUri() {
		return EpicuriContent.TABLE_URI;
	}

	@Override
	public ArrayList<EpicuriTable> parseJson(String jsonString) throws JSONException {
		JSONArray tablesJson = new JSONArray(jsonString);
		ArrayList<EpicuriTable> tables = new ArrayList<EpicuriTable>(tablesJson.length());
		for(int i=0; i<tablesJson.length(); i++){
			JSONObject tableJson = tablesJson.getJSONObject(i);
			tables.add(new EpicuriTable(tableJson));
		}
		return tables;
	}

}
