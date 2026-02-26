package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class DeleteReservationWebServiceCall implements WebServiceCall {
	final String path;
	
	public DeleteReservationWebServiceCall(String partyId, boolean withBlackMark){
		
		path = String.format("/Reservation/%s?withPrejudice=%s", partyId, withBlackMark ? "true":"false");
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
		return new Uri[]{EpicuriContent.PARTIES_URI, EpicuriContent.RESERVATIONS_URI};
	}
}
