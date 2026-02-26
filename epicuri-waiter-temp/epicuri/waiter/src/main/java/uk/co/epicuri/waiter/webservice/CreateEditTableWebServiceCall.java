package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.model.EpicuriTable.Shape;

public class CreateEditTableWebServiceCall implements WebServiceCall {
	private final String method;
	private final String body;
	private final String path;
	
	/**
	 * Create new table
	 * @param name of table
	 * @param shape for the table
	 */
	public CreateEditTableWebServiceCall(String name, Shape shape) {
		path = "/Table";
		method = "POST";
	
		JSONObject bodyJson = new JSONObject();
		try{
			bodyJson.put("Name", name);
			bodyJson.put("DefaultCovers", 1); // TODO: remove once defaultcovers removed from webservice
			bodyJson.put("Position", JSONObject.NULL);
			bodyJson.put("Shape", shape.getId());
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("Shouldn't happen");
		}
		body = bodyJson.toString();
	}
	
	/**
	 * Create new table
	 * @param id
	 * @param name of table
	 * @param shape for the table
	 */
	public CreateEditTableWebServiceCall(String id, String name, Shape shape) {
		path = "/Table/" + id;
		method = "PUT";
	
		JSONObject bodyJson = new JSONObject();
		try{
			bodyJson.put("Name", name);
			bodyJson.put("DefaultCovers", 1); // TODO: remove once defaultcovers removed from webservice
//			bodyJson.put("Position", JSONObject.NULL);
			bodyJson.put("Shape", shape.getId());
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("Shouldn't happen");
		}
		body = bodyJson.toString();
	}
	
	/**
	 * update existing table
	 * @param table
	 */
	public CreateEditTableWebServiceCall(EpicuriTable table) {
				
		path = String.format("/Table/%s", table.getId());
		method = "PUT";

		JSONObject bodyJson = new JSONObject();
		try{
			bodyJson.put("Name", table.getName());
			bodyJson.put("DefaultCovers", 1); // TODO: remove once defaultcovers removed from webservice
			bodyJson.put("Position", JSONObject.NULL);
			bodyJson.put("Shape", table.getShape().getId());
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("Shouldn't happen");
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
		return new Uri[]{EpicuriContent.TABLE_URI};
	}
}
