package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class SaveTableSeatingWebServiceCall implements WebServiceCall {
	private final String body;
	private final String sessionId;
	
	public SaveTableSeatingWebServiceCall(String layout, String sessionId){
		JSONObject o = new JSONObject();	
		/*
		put Session/Chairs/{id}

		{“ChairData”: ” {\”something\”: 2}”} */
		try {
			o.put("ChairData", layout);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
		this.sessionId = sessionId;
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
		return String.format("/Session/Chairs/%s", sessionId);
	}

	@Override
	public String getBody() {
		return body;
	}
	
	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
}
