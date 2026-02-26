package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class MenuItemLoaderTemplate implements LoadTemplate<ArrayList<EpicuriMenu.Item>> {
	
	private final Uri uri;
	
	public MenuItemLoaderTemplate(boolean showOrphans) {
		uri = EpicuriContent.MENUITEM_URI.buildUpon().appendQueryParameter("orphaned", showOrphans ? "true":"false").build();
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public ArrayList<EpicuriMenu.Item> parseJson(String jsonString) throws JSONException {
		
		JSONArray itemsJson = new JSONArray(jsonString);
		ArrayList<EpicuriMenu.Item> items = new ArrayList<EpicuriMenu.Item>(itemsJson.length());
		
		for(int i=0; i<itemsJson.length(); i++){
			EpicuriMenu.Item item = new EpicuriMenu.Item(itemsJson.getJSONObject(i));
			items.add(item);
		}
		return items;
	}

}
