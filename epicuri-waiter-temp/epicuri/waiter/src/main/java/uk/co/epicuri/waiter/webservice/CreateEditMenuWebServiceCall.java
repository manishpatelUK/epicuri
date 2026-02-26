package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateEditMenuWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;

	public CreateEditMenuWebServiceCall(String menuName, boolean active){
		method = "POST";
		path = "/Menu";
		body = setupBody(menuName, active, 0);
	}

    public CreateEditMenuWebServiceCall(String id, String menuName, boolean active, int order) {
        method = "PUT";
        path = String.format("/Menu/%s", id);
        body = setupBody(menuName, active, order);
    }

    private String setupBody(String menuName, boolean active, int order){
		try {
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("MenuName", menuName);
			jsonBody.put("Active", active);
			jsonBody.put("order", order);
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
		return new Uri[]{EpicuriContent.MENU_URI};
	}
}
