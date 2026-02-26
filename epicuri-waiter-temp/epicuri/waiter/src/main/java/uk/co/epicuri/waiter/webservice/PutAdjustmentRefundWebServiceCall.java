package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class PutAdjustmentRefundWebServiceCall implements WebServiceCall {
	final String path;
	final String body;
	private final String sessionId;

	public PutAdjustmentRefundWebServiceCall(EpicuriSessionDetail session, EpicuriAdjustment
			adjustment, String transactionId, String location){
		path = String.format("/Adjustment/refund/%s", adjustment.getId());
		sessionId = session.getId();

		JSONObject o = new JSONObject();
		JSONObject paymentSense = new JSONObject();
		try {
			paymentSense.put("transactionId", transactionId);
			paymentSense.put("location", location);
			o.put("paymentSense", paymentSense);
		} catch (Exception e) {
			// Ignore exception
		}

		body = o.toString();
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
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
	}
	
	
}
