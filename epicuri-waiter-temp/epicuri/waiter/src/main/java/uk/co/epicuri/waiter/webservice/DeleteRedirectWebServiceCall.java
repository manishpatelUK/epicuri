package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class DeleteRedirectWebServiceCall implements WebServiceCall {
	final String path;

	public DeleteRedirectWebServiceCall(String id) {
		path = String.format("/Printer/Redirect/%s", id);
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
		return new Uri[]{EpicuriContent.PRINTER_REDIRECT_URI};
	}
}
