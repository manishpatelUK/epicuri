package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class MenuLoaderTemplate implements LoadTemplate<EpicuriMenu> {

	private final Uri uri;
	
	/*
	"options": ["potato","veg"]
	"options" : { "potato": {"type":"choice","values":["Mash","Chips","Gratin"]}};
	 */

	public MenuLoaderTemplate(String menuId) {
		uri = Uri.withAppendedPath(EpicuriContent.MENU_URI, menuId);
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public EpicuriMenu parseJson(String jsonString) throws JSONException {
		return new EpicuriMenu(new JSONObject(jsonString));
	}

}
