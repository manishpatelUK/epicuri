package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriVatRate;

public class VatRateLoaderTemplate implements LoadTemplate<ArrayList<EpicuriVatRate>> {

	@Override
	public Uri getUri() {
		return EpicuriContent.VATRATES_URI;
	}

	@Override
	public ArrayList<EpicuriVatRate> parseJson(String jsonString) throws JSONException {
		JSONArray ratesArrayJson = new JSONArray(jsonString);
		
		ArrayList<EpicuriVatRate> response = new ArrayList<EpicuriVatRate>(ratesArrayJson.length());
		for(int i=0; i<ratesArrayJson.length(); i++){
			response.add(new EpicuriVatRate(ratesArrayJson.getJSONObject(i)));
		}
		return response;
	}


}
