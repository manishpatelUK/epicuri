package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class MewsPaymentWebServiceCall implements WebServiceCall{
	public enum Type {
		MONETARY(0), PERCENTAGE(1);
		private final int id;
		Type(int id) { this.id=  id; }
	}

	final String body;
	final String sessionId;

	public MewsPaymentWebServiceCall(EpicuriSessionDetail session, EpicuriMewsCustomer customer, double value, String reference){
		JSONObject o = new JSONObject();
		try {
			o.put("SessionId", session.getId());
			o.put("PaymentAmount", value);
			o.put("Customer", customer.toJson());
			if(null != reference){
				o.put("Reference", reference);
			}
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		sessionId = session.getId();
		body = o.toString();
	}

	@Override
	public String getMethod() {
		return "POST";
	}

	@Override
	public boolean requiresToken() {
		return true;
	}

	@Override
	public String getPath() {
		return "/Mews/Adjustment";
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, String.valueOf(sessionId))};
	}



}
