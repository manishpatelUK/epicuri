package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class VoidSessionWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String sessionId;

	public VoidSessionWebServiceCall(String sessionId, String reason, boolean forceClose){
		path = String.format("/Session/Void/%s?forceClose=%s", sessionId, forceClose);

		JSONObject json = new JSONObject();
		try{
			json.put("Reason", reason);
		} catch(JSONException e){
			throw new RuntimeException("Unexpected JSON error");
		}
		body = json.toString();
		this.sessionId = sessionId;
	}

	public VoidSessionWebServiceCall(String sessionId, String reason){
		this(sessionId, reason, false);
	}

	@Override
	public String getMethod() {
		return "PUT";
	}

	@Override
	public boolean requiresToken() {
		return true;
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
	public Uri[] getUrisToRefresh() {
		return new Uri[]{
				Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId),
				EpicuriContent.SESSION_URI,
				EpicuriContent.CLOSED_SESSION_URI
			};
	}

}
