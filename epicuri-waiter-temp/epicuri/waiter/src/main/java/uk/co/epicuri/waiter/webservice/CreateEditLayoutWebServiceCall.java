package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriTable;

public class CreateEditLayoutWebServiceCall implements WebServiceCall {
	private final String method;
	private final String body;
	private final String path;
	private final String floorId;
	private final String layoutId;
	
	public CreateEditLayoutWebServiceCall(EpicuriFloor.Layout layout, String floorId, boolean temporary){
		
		if(layout.getId() != null && !layout.getId().equals("0") && !layout.getId().equals("-1")){
			method = "PUT";
			path = String.format("/Layout/%s", layout.getId());
		} else {
			method = "POST";
			path = "/Layout";
		}
		this.floorId = floorId;
		this.layoutId = layout.getId();
		
		JSONObject bodyJson = new JSONObject();
		
		try {
			bodyJson.put("Name",layout.getName());
			bodyJson.put("Floor", floorId);
			bodyJson.put("Temporary", temporary);
			JSONArray tablesJson = new JSONArray();
			if(null != layout.getTables()){
				for(EpicuriTable table: layout.getTables()){
					JSONObject tableJson = new JSONObject();
					tableJson.put("Id", table.getId());
					JSONObject positionJson = new JSONObject();
					positionJson.put("X", table.getX());
					positionJson.put("Y", table.getY());
					positionJson.put("Rotation", table.getRotation());
					positionJson.put("ScaleX", table.getWidth());
					positionJson.put("ScaleY", table.getHeight());
					tableJson.put("Position", positionJson);
					tablesJson.put(tableJson);
				}
			}
			bodyJson.put("Tables", tablesJson);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot recover");
		}
		body = bodyJson.toString();
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
		if(layoutId != null && !layoutId.equals("0") && !layoutId.equals("-1")) {
			return new Uri[]{Uri.withAppendedPath(EpicuriContent.FLOOR_URI, String.valueOf(floorId)), Uri.withAppendedPath(EpicuriContent.LAYOUT_URI, layoutId)};
		} else {
			return new Uri[]{};
		}
	}

}
