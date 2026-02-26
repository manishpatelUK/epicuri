package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateEditLoginWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;

	public CreateEditLoginWebServiceCall(
            String id,
            CharSequence name,
            CharSequence username,
            CharSequence password,
            CharSequence pin,
            CharSequence role){
		
		method ="PUT";
		path = String.format("/Staff/%s", id);
		body = setupBody(name, username, password, pin, role);
	}
	
	public CreateEditLoginWebServiceCall(
            CharSequence name,
            CharSequence username,
            CharSequence password,
            CharSequence pin, CharSequence role){
		
		method ="POST";
		path = "/Staff";
		body = setupBody(name, username, password, pin, role);
	}
	/*{"Name":"manager","Username":"manager","Password":"test","Pin":"5678", "role":"MANAGER"}
	 */
	private String setupBody(
            CharSequence name,
            CharSequence username,
            CharSequence password,
            CharSequence pin,
            CharSequence role){
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("Name", name);
			bodyJson.put("Username", username);
			if(null != password && password.length() > 0){
				bodyJson.put("Password", password);
			}
			if(null != pin && pin.length() > 0){
				bodyJson.put("Pin", pin);
			}

			bodyJson.put("Role", role);
			
			return bodyJson.toString();
		} catch (JSONException e) {
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
		return new Uri[]{EpicuriContent.LOGIN_URI};
	}

}
