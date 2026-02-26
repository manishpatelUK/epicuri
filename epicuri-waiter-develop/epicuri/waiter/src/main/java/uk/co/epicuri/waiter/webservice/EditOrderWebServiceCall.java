package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;

public class EditOrderWebServiceCall implements WebServiceCall {

	private final String method;
	private final String path;
	private final String body;
	
	public EditOrderWebServiceCall(EpicuriOrderItem item, EpicuriAdjustmentType reason) {
		method = "PUT";
		path = String.format("/Order/RemoveOrderFromBill/%s", item.getId());

		JSONObject bodyObject = new JSONObject();
		try {
			bodyObject.put("AdjustmentType", reason.getId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		body = bodyObject.toString();
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
		return new Uri[]{};
	}
}
