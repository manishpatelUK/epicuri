package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class LookupCustomerWebServiceCall implements WebServiceCall {
	private final String path;
	
	public LookupCustomerWebServiceCall(String phoneNumber, String email){
		StringBuilder pathBuilder = new StringBuilder("/Customer?");
		if(phoneNumber != null && phoneNumber.trim().length() > 0){
			pathBuilder.append("phoneNumber=").append(Uri.encode(phoneNumber)).append("&");
		} else {
			pathBuilder.append("phoneNumber=&");
		}
		if(email != null && email.trim().length() > 0){
			pathBuilder.append("email=").append(Uri.encode(email));
		} else {
			pathBuilder.append("email=");
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
