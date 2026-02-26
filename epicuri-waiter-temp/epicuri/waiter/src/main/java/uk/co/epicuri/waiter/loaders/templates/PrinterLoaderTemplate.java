package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class PrinterLoaderTemplate implements LoadTemplate<ArrayList<EpicuriMenu.Printer>> {
	@Override
	public Uri getUri() {
		return EpicuriContent.PRINTER_URI;
	}

	@Override
	public ArrayList<EpicuriMenu.Printer> parseJson(String jsonString) throws JSONException {
		JSONArray printerArrayJson = new JSONArray(jsonString);
		
		ArrayList<EpicuriMenu.Printer> response = new ArrayList<EpicuriMenu.Printer>(printerArrayJson.length());
		for(int i=0; i<printerArrayJson.length(); i++){
			response.add(new EpicuriMenu.Printer(printerArrayJson.getJSONObject(i)));
		}
		return response;
	}


}
