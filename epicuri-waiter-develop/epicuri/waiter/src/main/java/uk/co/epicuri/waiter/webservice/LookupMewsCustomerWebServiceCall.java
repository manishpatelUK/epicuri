package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class LookupMewsCustomerWebServiceCall implements WebServiceCall {
	private final String path;

	public LookupMewsCustomerWebServiceCall(String name, String roomNumber){
		StringBuilder pathBuilder = new StringBuilder("/Mews/Customers?");
		if(name != null && name.trim().length() > 0){
			pathBuilder.append("name=").append(Uri.encode(name)).append("&");
		} else {
			pathBuilder.append("name=&");
		}
		if(roomNumber != null && roomNumber.trim().length() > 0){
			pathBuilder.append("room=").append(Uri.encode(roomNumber));
		} else {
			pathBuilder.append("room=");
		}
		path = pathBuilder.toString();
	}
	
	@Override
	public String getMethod() {
		return "GET";
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
