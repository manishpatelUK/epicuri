package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;

public class PrinterRedirectLoaderTemplate implements LoadTemplate<ArrayList<EpicuriPrintRedirect>> {
	@Override
	public Uri getUri() {
		return EpicuriContent.PRINTER_REDIRECT_URI;
	}

	@Override
	public ArrayList<EpicuriPrintRedirect> parseJson(String jsonString) throws JSONException {
		JSONArray printerArrayJson = new JSONArray(jsonString);
		
		ArrayList<EpicuriPrintRedirect> response = new ArrayList<EpicuriPrintRedirect>(printerArrayJson.length());
		for(int i=0; i<printerArrayJson.length(); i++){
			response.add(new EpicuriPrintRedirect(printerArrayJson.getJSONObject(i)));
		}
		return response;
	}

}
