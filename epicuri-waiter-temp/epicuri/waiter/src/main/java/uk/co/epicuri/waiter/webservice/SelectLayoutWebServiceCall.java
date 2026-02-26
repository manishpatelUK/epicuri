package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;


public class SelectLayoutWebServiceCall implements WebServiceCall {
	private final String path;
	private final String body;
	private final String floorId;
	
	public SelectLayoutWebServiceCall(String floorId, String layoutId){
		path = String.format("/Floor/%s", floorId);
		
		JSONObject json = new JSONObject();
		try{
			json.put("Id", layoutId);
		} catch(JSONException e){
			throw new RuntimeException("Unexpected JSON error");
		}
		body = json.toString();
		this.floorId = floorId;
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.FLOOR_URI, floorId), EpicuriContent.FLOOR_URI};
	}
	
}
