package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class MarkReservationArrivedWebServiceCall implements WebServiceCall {
	private final String path;
	
	public MarkReservationArrivedWebServiceCall(String sessionId) {
		path = String.format("/Reservation/Arrived/%s", sessionId);
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
		return new Uri[]{EpicuriContent.RESERVATIONS_URI};
	}

}
