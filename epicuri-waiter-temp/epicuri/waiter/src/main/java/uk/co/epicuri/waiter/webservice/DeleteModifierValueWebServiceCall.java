package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;


public class DeleteModifierValueWebServiceCall implements WebServiceCall {
	final String path;
	
	public DeleteModifierValueWebServiceCall(String id){
		path = String.format("/Modifier/%s", id);
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
		return new Uri[]{EpicuriContent.MENUMODIFIER_URI};
	}
}
