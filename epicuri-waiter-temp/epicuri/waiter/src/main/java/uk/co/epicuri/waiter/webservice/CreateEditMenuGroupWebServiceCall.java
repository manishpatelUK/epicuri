package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateEditMenuGroupWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;
	private final String menuId;

	public CreateEditMenuGroupWebServiceCall(String id, String groupName, String menuCategoryId, String menuId, ArrayList<String> menuItemIds, int order){
		method = "PUT";
		path = String.format("/MenuGroup/%s", id);
		body = setupBody(groupName, menuCategoryId, menuItemIds, order);
		this.menuId = menuId;
	}
	
	public CreateEditMenuGroupWebServiceCall(String groupName, String menuCategoryId, String menuId, ArrayList<String> menuItemIds, int order){
		method = "POST";
		path = "/MenuGroup";
		body = setupBody(groupName, menuCategoryId, menuItemIds, order);
		this.menuId = menuId;
	}
	/*
	 * {
    "GroupName":"Chicken Starters 2",
    "MenuCategoryId":11,
    "MenuItemIds":[1,2,3],
    "Order":2,
}
	 */
	private String setupBody(String groupName, String menuCategoryId, ArrayList<String> menuItemIds, int order){
		try {
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("GroupName", groupName);
			jsonBody.put("MenuCategoryId", menuCategoryId);
			jsonBody.put("Order", order);
			JSONArray itemIdsJson = new JSONArray();
			for(String id: menuItemIds){
				itemIdsJson.put(id);
			}
			jsonBody.put("MenuItemIds", itemIdsJson);
			
			return jsonBody.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
		return new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))};
	}
}
