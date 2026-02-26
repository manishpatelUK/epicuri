package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MarkBatchAsPrintedWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	
	public MarkBatchAsPrintedWebServiceCall(List<String> batchIds) {
		path = "/Print";
		
		JSONObject bodyJson = new JSONObject();
		try{
			JSONArray batchIdsJson = new JSONArray();
			for(String id: batchIds){
				batchIdsJson.put(id);
			}
			bodyJson.put("batchId", batchIdsJson);
		} catch (JSONException e){
			throw new RuntimeException(e);
		}
		body = bodyJson.toString();
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
    	return new Uri[]{};
    }
}
