package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class EditDinerOrderWebServiceCall implements WebServiceCall {
	private final String body;
	private final String path;
	private final String sessionId;

	public EditDinerOrderWebServiceCall(String sessionId, String dinerId, ArrayList<String>
			orderIds, ArrayList<String> unassignedOrderIds){
		path = String.format("/Order/allocate/%s", sessionId);
		
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("dinerId", dinerId);
			bodyJson.put("orderIds", new JSONArray(orderIds));
			bodyJson.put("unassignedOrderIds", new JSONArray(unassignedOrderIds));
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
