package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class ClearTakeawayWebServiceCall implements WebServiceCall {
	private final String path;

	public ClearTakeawayWebServiceCall(String sessionId){
		path = String.format("/Order/RemoveAllOrdersFromSession/%s", sessionId);
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
		return new Uri[]{};
	}

}
