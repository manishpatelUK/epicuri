package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriEvent.Notification;

public class AcknowledgeNotificationWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String sessionId;
	
	public AcknowledgeNotificationWebServiceCall(Notification notification, String sessionId){
		switch(notification.getType()){
		case TYPE_ADHOC:{
			path = String.format("/AdhocAcknowledgement/%s", notification.getId());
			break;
		}
		case TYPE_RECURRING:
		case TYPE_SCHEDULED:{
			path = String.format("/Acknowledgement/%s", notification.getId());
			break;
		}
		default:
			throw new IllegalStateException("Notification type not recognised");
		}
		this.sessionId = sessionId;
		
		JSONObject responseJson = new JSONObject();
		try {
			responseJson.put("SessionId", sessionId);
		} catch (JSONException e){
			throw new RuntimeException(e);
		}
		body = responseJson.toString();
	}
	
	@Override
	public String getMethod() {
		return "POST";
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
		return new Uri[]{EpicuriContent.EVENT_URI,Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
}