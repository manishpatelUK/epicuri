package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCashUp;

public class CashUpLoaderTemplate implements LoadTemplate<ArrayList<EpicuriCashUp>> {

	@Override
	public Uri getUri() {
		return EpicuriContent.CASHUP_URI;
	}

	@Override
	public ArrayList<EpicuriCashUp> parseJson(String jsonString) throws JSONException {

		JSONArray itemsJson = new JSONArray(jsonString);
		ArrayList<EpicuriCashUp> items = new ArrayList<EpicuriCashUp>(itemsJson.length());

		for(int i=0; i<itemsJson.length(); i++){
			EpicuriCashUp item = new EpicuriCashUp(itemsJson.getJSONObject(i));
			items.add(item);
		}
		return items;
	}

}
