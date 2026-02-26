package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class GetPrintersWebServiceCall implements WebServiceCall {

	@Override
	public String getMethod() {
		return "GET";
	}

	@Override
	public String getPath() {
		return "/Printer";
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
