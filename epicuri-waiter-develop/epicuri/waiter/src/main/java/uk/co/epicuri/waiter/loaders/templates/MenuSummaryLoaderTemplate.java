package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;

public class MenuSummaryLoaderTemplate implements LoadTemplate<List<EpicuriMenuSummary>> {
	private final Uri uri;
	public MenuSummaryLoaderTemplate(boolean includeInactiveMenus) {
		uri = includeInactiveMenus ? Uri.withAppendedPath(EpicuriContent.MENU_URI, "All") : EpicuriContent.MENU_URI;
	}

	@Override
	public Uri getUri() {
		return uri;
	}
	
	@Override
	public List<EpicuriMenuSummary> parseJson(String jsonString) throws JSONException {
		
		JSONArray menusJson = new JSONArray(jsonString);
		List<EpicuriMenuSummary> menus = new ArrayList<EpicuriMenuSummary>(menusJson.length());
		for(int i=0; i<menusJson.length(); i++){
			menus.add(new EpicuriMenuSummary(menusJson.getJSONObject(i)));
		}
		return menus;
	}

}
