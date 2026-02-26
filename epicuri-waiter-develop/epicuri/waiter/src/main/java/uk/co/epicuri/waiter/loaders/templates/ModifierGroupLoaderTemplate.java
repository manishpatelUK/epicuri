package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class ModifierGroupLoaderTemplate implements LoadTemplate<ArrayList<EpicuriMenu.ModifierGroup>> {
	
	@Override
	public Uri getUri() {
		return EpicuriContent.MENUMODIFIER_URI;
	}

	@Override
	public ArrayList<EpicuriMenu.ModifierGroup> parseJson(String jsonString) throws JSONException {
		
		JSONArray modifiersJson = new JSONArray(jsonString);
		ArrayList<EpicuriMenu.ModifierGroup> returnValue = new ArrayList<EpicuriMenu.ModifierGroup>(modifiersJson.length());
		for(int i=0; i<modifiersJson.length(); i++){
			returnValue.add(new EpicuriMenu.ModifierGroup(modifiersJson.getJSONObject(i)));
		}
		return returnValue;
	}

}
