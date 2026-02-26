package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class DetachCustomerFromDinerWebServiceCall implements WebServiceCall {

	final String path;
	final String sessionId;
	public DetachCustomerFromDinerWebServiceCall(EpicuriSessionDetail.Diner diner, String sessionId){
		path = String.format("/Diner/DisassociateCheckIn/%s", diner.getId());
		this.sessionId = sessionId;
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
	
}
