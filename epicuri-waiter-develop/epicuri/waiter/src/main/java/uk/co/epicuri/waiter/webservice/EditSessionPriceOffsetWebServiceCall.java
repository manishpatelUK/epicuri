package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.LocalSettings;

public class EditSessionPriceOffsetWebServiceCall implements WebServiceCall {
	private final String body;
	private final String path;
	private final String sessionId;
	
	public EditSessionPriceOffsetWebServiceCall (String sessionId, Money priceOffset){
		path = String.format("/Session/PriceOffset/%s", sessionId);
		
		JSONObject bodyJson = new JSONObject();
		try {
			Money correctedOffset = Money.zero(LocalSettings.getCurrencyUnit()).minus(priceOffset);
			bodyJson.put("Offset", LocalSettings.formatMoneyAmount(correctedOffset, false));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		body = bodyJson.toString();
		this.sessionId = sessionId;
	}
	@Override
	public String getMethod() {
		return "PUT";
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public boolean requiresToken() {
		return true;
	}
	
	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, String.valueOf(sessionId))};
	}
}
