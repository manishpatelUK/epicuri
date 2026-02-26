package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CloseSessionWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String sessionId;
	/*
	 * PUT api/Session/Close/1

Send:

{"GiveBlackMark":false}

Returns: 400, 403, 200. No response on success

	 */
	public CloseSessionWebServiceCall(String sessionId, boolean blackMark){
		path = String.format("/Session/Close/%s", sessionId);
		JSONObject bodyJson = new JSONObject();
		try{
			bodyJson.put("GiveBlackMark", blackMark);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("Unexpected jsone error");
		}
		body = bodyJson.toString();
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
				EpicuriContent.PARTIES_URI,
				EpicuriContent.CLOSED_SESSION_URI
			};
	}

}
