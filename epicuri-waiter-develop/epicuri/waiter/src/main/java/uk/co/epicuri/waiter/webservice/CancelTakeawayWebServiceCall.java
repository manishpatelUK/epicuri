package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CancelTakeawayWebServiceCall implements WebServiceCall {
	private final String path;
	private final String sessionId;
	/*
	 * PUT api/Session/Close/1

Send:

{"GiveBlackMark":false}

Returns: 400, 403, 200. No response on success

	 */
	public CancelTakeawayWebServiceCall(String sessionId){
		path = String.format("/Takeaway/%s", sessionId);
		this.sessionId = sessionId;
	}

	@Override
	public String getMethod() {
		return "DELETE";
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
				EpicuriContent.SESSION_URI
			};
	}

}
