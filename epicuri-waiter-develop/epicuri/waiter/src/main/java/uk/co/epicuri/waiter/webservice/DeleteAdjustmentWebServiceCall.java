package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class DeleteAdjustmentWebServiceCall implements WebServiceCall {
	final String path;
	private final String sessionId;

	public DeleteAdjustmentWebServiceCall(EpicuriSessionDetail session, EpicuriAdjustment adjustment){
		path = String.format("/Adjustment/%s", adjustment.getId());
		sessionId = session.getId();
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
	
	
}
