package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class SplitSessionWebServiceCall implements WebServiceCall {
	final String body;
	final String sessionId;

	public SplitSessionWebServiceCall(String sessionId, ArrayList<String> orderItems){
		this.sessionId = sessionId;
		JSONObject o = new JSONObject();
		try {
			o.put("sessionType", "TAB");
			o.put("orderIds", new JSONArray(orderItems));
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
	}

	@Override
	public String getMethod() {
		return "POST";
	}

	@Override
	public boolean requiresToken() {
		return true;
	}
	
	@Override
	public String getPath() {
		return String.format("/Session/split/%s", sessionId);
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, String.valueOf(sessionId))};
	}
	
	
}
