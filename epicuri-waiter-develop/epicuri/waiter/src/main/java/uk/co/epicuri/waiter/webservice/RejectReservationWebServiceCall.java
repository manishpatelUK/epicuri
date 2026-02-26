package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class RejectReservationWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	
	public RejectReservationWebServiceCall(String reservationId, CharSequence message) {
		path = String.format("/Reservation/Reject/%s", reservationId);
		
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody.put("Notice", message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		body = jsonBody.toString();
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
		return body;
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
