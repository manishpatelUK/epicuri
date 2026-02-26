package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class EditPartySessionWebServiceCall implements WebServiceCall {
	final String body;
	final String partyId;
	final String sessionId;
	final boolean refreshSession;

	public EditPartySessionWebServiceCall(String sessionId, String partyId, int numberOfDiners,
			String name, boolean refreshSession){
		this.sessionId = sessionId;
		this.partyId = partyId;
		this.refreshSession = refreshSession;

		JSONObject o = new JSONObject();
		try {
			o.put("numberOfDiners", numberOfDiners);
			o.put("name", name);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
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
		return (sessionId != null && !sessionId.equals("0") && !sessionId.equals("-1")) ? String
				.format("/Session/%s", sessionId) : String.format("/Party/%s", partyId);
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return !refreshSession ? new Uri[]{EpicuriContent.SESSION_URI, EpicuriContent.PARTIES_URI}
		: new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
	
	
}
