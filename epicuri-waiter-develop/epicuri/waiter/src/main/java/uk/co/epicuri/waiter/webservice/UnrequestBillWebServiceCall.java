package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;


public class UnrequestBillWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String sessionId;
	/*
	 * PUT api/Session/Close/1

Send:

{"GiveBlackMark":false}

Returns: 400, 403, 200. No response on success

	 */
	public UnrequestBillWebServiceCall(String sessionId){
		path = String.format("/Session/UnrequestBill/%s", sessionId);
		body = "";
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, String.valueOf(sessionId))};
	}

}
