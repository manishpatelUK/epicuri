package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriEvent.Notification;

public class DelaySessionWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String sessionId;
	
	public DelaySessionWebServiceCall(Notification notification, String sessionId, long sessionLag){

		long dueDate = new Date().getTime() + 10 * 60 * 1000; // add ten minutes on to 'now' to get new due date
//		notification.getDue().getTime()
		long sessionOffsetRequired = (sessionLag + dueDate - notification.getDue().getTime()) / 1000;
		this.sessionId = sessionId;
		path = String.format("/Session/Delay/%s", sessionId);
	
		JSONObject responseJson = new JSONObject();
		try {
			responseJson.put("Delay", sessionOffsetRequired);
		} catch (JSONException e){
			throw new RuntimeException(e);
		}
		body = responseJson.toString();
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
		return new Uri[]{EpicuriContent.EVENT_URI, Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}

}
