package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class DeleteLoginWebServiceCall implements WebServiceCall {
	final String path;
	
	public DeleteLoginWebServiceCall(String id) {
		path = String.format("/Staff/%s", id);
	}

	@Override
	public String getMethod() {
		return "DELETE";
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
		return new Uri[]{EpicuriContent.LOGIN_URI};
	}
}
