package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriTable;

public class SaveLayoutWebServiceCall implements WebServiceCall {
	
	public static class Builder {
		private final List<EpicuriTable> tables;
		private final EpicuriFloor floor;
		private String name;
		private String overwriteId = "0";
		
		private boolean temporary = false;
		
		public Builder(List<EpicuriTable> tables, EpicuriFloor floor, String name){
			this.tables = tables; this.floor = floor; this.name = name;
		}
		public Builder setOverwrite(String id){this.overwriteId = id; return this; }
		public Builder setName(String name){this.name = name; return this; }
		public Builder setTemporary(boolean temporary){ this.temporary= temporary; return this; }
		public SaveLayoutWebServiceCall build(){
			return new SaveLayoutWebServiceCall(tables, floor, name, overwriteId, temporary);
		}
	}
	
	private final String body;
	private final String method;
	private final String path;
	private final String floorId;
	
	public SaveLayoutWebServiceCall(List<EpicuriTable> tables, EpicuriFloor floor, String name, String overwriteId, boolean temporary) {
		
		/*
		 * 02-27 10:55:40.368: V/WebServiceTask(12865): POST to /Layout with [{"Name":"Table 4 (upstairs)","Shape":"square","Id":9,"Rotation":0,"Y":289.36566162109375,"type":"table","X":907.3200073242188,"ScaleX":338.1160888671875,"ScaleY":268.64752197265625}]
  "Tables": [
        {
            "Id": 1,
            "Name": "Table 1",
            "Position": {
                "$id": "1",
                "X": 1,
                "Y": 1,
                "Rotation": 0,
                "ScaleX": 1,
                "ScaleY": 1
            },
            "DefaultCovers": 4,
            "Shape": 0
        }, ...] 
    "Name": "Super Tuesday Service Downstairs",
    "Floor": 1
		 */
		JSONObject response = new JSONObject();
		try{
			JSONArray tableJson = new JSONArray();
			for(EpicuriTable table: tables){
				JSONObject thisTable = new JSONObject();
				thisTable.put("Id", table.getId());
				
				// it's possible the following fields aren't read by the server
				thisTable.put("Name", table.getName());
				thisTable.put("Shape", table.getShape().getId());
				JSONObject position = new JSONObject();
				position.put("X", table.getX());
				position.put("Y", table.getY());
				position.put("Rotation", table.getRotation());
				position.put("ScaleX", table.getWidth());
				position.put("ScaleY", table.getHeight());
				thisTable.put("Position", position);
				
				tableJson.put(thisTable);
			}
			
			response.put("Tables", tableJson);
			response.put("Name", name);
			response.put("Floor", floor.getId());
			response.put("Temporary", temporary);
		} catch (JSONException e){		
			throw new RuntimeException("Error creating JSON");
		}
		body = response.toString();
		
		if(overwriteId != null && !overwriteId.equals("0")){
			path = String.format("/Layout/%s", overwriteId);
			method = "PUT";
		} else {
			path = "/Layout";
			method = "POST";
		}
		this.floorId = floor.getId();
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.FLOOR_URI, floorId)};
	}
}
