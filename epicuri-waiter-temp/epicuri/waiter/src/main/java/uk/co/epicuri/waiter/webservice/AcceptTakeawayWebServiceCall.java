package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class AcceptTakeawayWebServiceCall implements WebServiceCall {
	private final String path;
	private final String sessionId;
	
	public AcceptTakeawayWebServiceCall(String takeawayId) {
		path = String.format("/Session/Accept/%s", takeawayId);
		this.sessionId = takeawayId;
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
		return null;
	}

	@Override
	public boolean requiresToken() {
		return true;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}

}
