package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class SendPaymentWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final int sessionId; 
	/*
	 * 
POST api/Payment

Send:

{"SessionId":1, "Amount":2.50}
	 */
	public SendPaymentWebServiceCall(int sessionId, CharSequence amountString) {
		path = String.format("/Payment", sessionId);
		JSONObject bodyJSON = new JSONObject();
		try {
			bodyJSON.put("SessionId", sessionId);
			bodyJSON.put("Amount", amountString);
		} catch (JSONException e) {
			throw new RuntimeException("really shouldn't happen");
		}
		this.sessionId = sessionId;
		body = bodyJSON.toString();
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
		return path;
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
