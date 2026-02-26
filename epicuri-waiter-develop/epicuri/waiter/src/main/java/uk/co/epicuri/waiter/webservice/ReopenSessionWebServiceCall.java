package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class ReopenSessionWebServiceCall implements WebServiceCall {
	private final String path;
	private final String sessionId;

	public ReopenSessionWebServiceCall(String sessionId){
		path = String.format("/Session/Open/%s", sessionId);
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
		return null;
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
