package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriCustomer.Address;

public class CreateEditTakeawayWebServiceCall implements WebServiceCall {
	private final String method;
	private final String body;
	private final String path;
	private final String sessionId;

	public CreateEditTakeawayWebServiceCall(String id, boolean delivery, String name, String telephone, String message, Date expectedTime, Address address, EpicuriCustomer customer){
		method = "PUT";
		path = String.format("/Takeaway/%s", id);
		body = setupBody(id, delivery, name, telephone, message, expectedTime, address, customer, true);
		sessionId = id;
	}
	
	public CreateEditTakeawayWebServiceCall(String id, boolean delivery, String name, String telephone, String message, Date expectedTime, Address address, EpicuriCustomer customer, boolean submit){
 		method = submit ? "POST" : "PUT";
		path = "/Takeaway";
		body = setupBody(id, delivery, name, telephone, message, expectedTime, address, customer, false);
		sessionId = "-1";
	}
	
	private String setupBody(String id, boolean delivery, String name, String telephone, String message, Date expectedTime, Address address, EpicuriCustomer customer, boolean updating){
		JSONObject bodyJson = new JSONObject();
		
		try {
			if(id != null && !id.equals("0") && !id.equals("-1")){
				bodyJson.put("Id", id);
			}
			bodyJson.put("Delivery", delivery);
			bodyJson.put("Name", name);
			bodyJson.put("Telephone", telephone);
			bodyJson.put("Message", message);
			bodyJson.put("Address", address.toJson());
			if(null != expectedTime){
				bodyJson.put(updating ? "ExpectedTime" : "RequestedTime", expectedTime.getTime() / 1000L);
			}
			if(null != customer){
				bodyJson.put("LeadCustomerId", customer.getId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot recover");
		}
		return bodyJson.toString();
	}
	
	@Override
	public String getMethod() {
		return method;
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
		if(sessionId != null && !sessionId.equals("0") && !sessionId.equals("-1")){
			return new Uri[]{EpicuriContent.TAKEAWAY_URI, Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
		} else {
			return new Uri[]{EpicuriContent.TAKEAWAY_URI};
		}
	}
}
