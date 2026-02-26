package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class SetTakeawayMenuWebServiceCall implements WebServiceCall {
	private final String path;

	public SetTakeawayMenuWebServiceCall(String menuId) {
		path = String.format("/Menu/ChangeTakeawayMenu/%s", menuId);
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
		return new Uri[]{EpicuriContent.RESTAURANT_URI};
	}
}
