package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateEditModifierGroupWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;

	public CreateEditModifierGroupWebServiceCall(
			String id,
			CharSequence name,
			int lowerLimit,
			int upperLimit){
		
		method ="PUT";
		path = String.format("/ModifierGroup/%s", id);
		body = setupBody(name, lowerLimit, upperLimit);
	}
	
	public CreateEditModifierGroupWebServiceCall(
			CharSequence name,
			int lowerLimit,
			int upperLimit){
		method ="POST";
		path = "/ModifierGroup";
		body = setupBody(name, lowerLimit, upperLimit);
	}
	/*
	 * {
	    "GroupName":"Curry Sauce",
	"UpperLimit":1,
	"LowerLimit":1
	}
	 */
	private String setupBody(
			CharSequence name,
			int lowerLimit,
			int upperLimit){
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("GroupName", name);
			bodyJson.put("LowerLimit", lowerLimit);
			bodyJson.put("UpperLimit", upperLimit);
			
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
		return new Uri[]{EpicuriContent.MENUMODIFIER_URI};
	}
}
