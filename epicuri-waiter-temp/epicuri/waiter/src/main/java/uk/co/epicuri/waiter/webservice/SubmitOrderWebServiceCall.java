package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class SubmitOrderWebServiceCall implements WebServiceCall {
	String body;
	/*
POST api/Order

Creates a list of orders, “Note”(string) and “PriceOverride”(decimal) are optional parameters.

Payload

[
{
"Quantity":1,
"DinerId":9,
"MenuItemId":2,
"Modifiers":[12],
"CourseId":1,

}
]


]
	 */
	String sessionId;
	public SubmitOrderWebServiceCall(EpicuriSessionDetail session, Collection<EpicuriOrderItem> orders){
		try{
			JSONArray request = new JSONArray();
			for(EpicuriOrderItem order: orders){
				JSONObject orderJSON = new JSONObject();
				orderJSON.put("Quantity", order.getQuantity());
				orderJSON.put("DinerId", order.getDinerId());
				orderJSON.put("MenuItemId", order.getItem().getId());
				orderJSON.put("InstantiatedFromId", 0); // hardcoded value for Waiter App
				
				JSONArray modifiers = new JSONArray();
				for(EpicuriMenu.ModifierValue value: order.getChosenModifiers()){
					modifiers.put(value.getId());
				}
				orderJSON.put("Modifiers", modifiers);
				orderJSON.put("CourseId", order.getCourse().getId());
				if(order.isPriceOverridden()){
					orderJSON.put("PriceOverride", order.getPriceOverride().getAmount().doubleValue());
				}
				if(order.getNote() != null){
					orderJSON.put("Note", order.getNote());
				}
				request.put(orderJSON);
			}
			body = request.toString();
			sessionId = session.getId();
		} catch(JSONException e){
			throw new RuntimeException(e);
		}
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
		return "/Order?willAttemptImmediatePrint=true";
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
