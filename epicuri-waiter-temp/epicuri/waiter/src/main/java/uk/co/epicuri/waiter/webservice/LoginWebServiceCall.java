package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginWebServiceCall implements WebServiceCall {
	String body;
	
	public LoginWebServiceCall(String username, String password, String restaurantId){
		JSONObject o = new JSONObject();
		try {
			o.put("Username", username);
			o.put("Password", password);
			o.put("RestaurantId", restaurantId);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
	}

	@Override
	public String getMethod() {
		return "POST";
	}

	@Override
	public boolean requiresToken() {
		return false;
	}

	@Override
	public String getPath() {
		return "/Authentication/Login";
	}

	@Override
	public String getBody() {
		return body;
	}
	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{};
	}
}
