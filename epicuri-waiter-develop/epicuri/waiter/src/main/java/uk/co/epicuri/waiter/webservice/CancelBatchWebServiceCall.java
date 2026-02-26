package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CancelBatchWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;

	public CancelBatchWebServiceCall() {
		this(null, true);
	}

	public CancelBatchWebServiceCall(List<String> batchIds, boolean all) {
		path = "/Print" + (all ? "/all" : "");

		if(all) {
			body = null;
		} else {
			body = buildJsonBody(batchIds);
		}
	}

	private String buildJsonBody(List<String> batchIds) {
		JSONObject bodyJson = new JSONObject();
		try{
			JSONArray batchIdsJson = new JSONArray();
			for(String id: batchIds){
				batchIdsJson.put(id);
			}
			bodyJson.put("ids", batchIdsJson);
		} catch (JSONException e){
			throw new RuntimeException(e);
		}
		return bodyJson.toString();
	}

	@Override
	public String getMethod() {
		return "DELETE";
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
