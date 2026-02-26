package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class EditSessionPercentageOffsetWebServiceCall implements WebServiceCall {
	private final String body;
	private final String path;
	private final int sessionId;
	public EditSessionPercentageOffsetWebServiceCall (int sessionId, int percentageOffset){
		path = String.format("/Session/PercentageOffset/%s", sessionId);
		
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("Offset", -percentageOffset);
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
