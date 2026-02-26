package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.NumericalAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.NewAdjustmentRequest;

public class NewAdjustmentWebServiceCall implements WebServiceCall {
	private final boolean multiple;
	private final String body;
	private final String sessionId;

	public NewAdjustmentWebServiceCall(NewAdjustmentRequest request) {
		this(request.getSessionId(), request.getAdjustmentType(), request.getType(), request.getValue(), request.getReference(), request.getItemType());
	}

	public NewAdjustmentWebServiceCall(List<NewAdjustmentRequest> requests) {
		if(requests.size() == 0) {
			throw new IllegalArgumentException("No adjustments to upload");
		}
		JSONArray jsonArray = new JSONArray();
		for(NewAdjustmentRequest request : requests) {
			try {
				jsonArray.put(adjustmentToJson(request.getSessionId(), request.getAdjustmentType(), request.getType(), request.getValue(), request.getReference(), request.getItemType()));
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException("cannot continue");
			}
		}
		this.sessionId = requests.get(0).getSessionId();
		this.body = jsonArray.toString();
		this.multiple = true;
	}

	public NewAdjustmentWebServiceCall(String sessionId, EpicuriAdjustmentType adjustmentType, NumericalAdjustmentType type, double value, String reference, String itemType){
		JSONObject o;
		try {
            o = adjustmentToJson(sessionId, adjustmentType, type, value, reference, itemType);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		this.sessionId = sessionId;
		this.body = o.toString();
		this.multiple = false;
	}

    public static JSONObject adjustmentToJson(String sessionId, EpicuriAdjustmentType adjustmentType, NumericalAdjustmentType type, double value, String reference, String itemType) throws JSONException {
        JSONObject o = new JSONObject();
	    o.put("SessionId", sessionId);
        o.put("TypeId", adjustmentType.getId());
        o.put("NumericalTypeId", type.getId());
        o.put("Value", value);
        o.put("itemType", itemType);
        if(null != reference){
            o.put("Reference", reference);
        }
        return o;
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
		return multiple ? "/Adjustment/multiple" : "/Adjustment";
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
