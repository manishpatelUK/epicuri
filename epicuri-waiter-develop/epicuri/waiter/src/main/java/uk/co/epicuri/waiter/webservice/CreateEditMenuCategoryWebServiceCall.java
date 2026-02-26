package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class CreateEditMenuCategoryWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;
	private final String menuId;

	public CreateEditMenuCategoryWebServiceCall(String id, String categoryName, String menuId, List<EpicuriMenu.Group> menuGroups, String[] defaultCourseIds, int orderIndex){
		method = "PUT";
		path = String.format("/MenuCategory/%s", id);
		body = setupBody(categoryName, menuId, menuGroups, defaultCourseIds, orderIndex);
		this.menuId = menuId;
	}
	
	public CreateEditMenuCategoryWebServiceCall(String categoryName, String menuId, String[] defaultCourseIds, int orderIndex){
		method = "POST";
		path = "/MenuCategory";
		body = setupBody(categoryName, menuId, new ArrayList<EpicuriMenu.Group>(0), defaultCourseIds, orderIndex);
		this.menuId = menuId;
	}
	
	private String setupBody(String categoryName, String menuCategoryId, List<EpicuriMenu.Group> menuGroups, String[] defaultCourseIds, int orderIndex){
		try {
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("CategoryName", categoryName);
			jsonBody.put("MenuId", menuCategoryId);
			jsonBody.put("Order", orderIndex);
			JSONArray courses = new JSONArray();
			for(String courseId: defaultCourseIds){
				courses.put(courseId);
			}
			jsonBody.put("DefaultCourseIds", courses);
			JSONArray groupsJson = new JSONArray();
			for(EpicuriMenu.Group g: menuGroups){
				groupsJson.put(g.getId());
			}
			jsonBody.put("MenuGroupIds", groupsJson);
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
