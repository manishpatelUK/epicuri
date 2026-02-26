package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateEditModifierValueWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;

	public CreateEditModifierValueWebServiceCall(
			String id,
			CharSequence name,
			Money price,
			String taxType,
			String plu,
			String modifierGroupId){
		
		method ="PUT";
		path = String.format("/Modifier/%s", id);
		body = setupBody(name, price, taxType, plu, modifierGroupId);
	}
	
	public CreateEditModifierValueWebServiceCall(
			CharSequence name,
			Money price,
			String taxType,
			String plu,
			String modifierGroupId){
		
		method ="POST";
		path = "/Modifier";
		body = setupBody(name, price, taxType, plu, modifierGroupId);
	}
	/*{
    "ModifierValue": "Chips",
    "ModifierGroupId": 10,
    "TaxTypeId": 1,
    "Price": 3
}
	 */
	private String setupBody(
			CharSequence name,
			Money price,
			String taxType,
			String plu,
			String modifierGroupId){
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("ModifierValue", name);
			bodyJson.put("Price", price.getAmount().toPlainString());
			bodyJson.put("TaxTypeId", taxType);
			if(plu.trim().length() > 0) {
				bodyJson.put("plu", plu);
			}
			bodyJson.put("ModifierGroupId", modifierGroupId);
			
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
