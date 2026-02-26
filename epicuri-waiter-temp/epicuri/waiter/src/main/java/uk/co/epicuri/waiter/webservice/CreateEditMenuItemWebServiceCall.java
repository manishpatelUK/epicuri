package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;

public class CreateEditMenuItemWebServiceCall implements WebServiceCall {
	private final String method;
	private final String path;
	private final String body;

	public CreateEditMenuItemWebServiceCall(
			String id,
			CharSequence name,
			Money price,
			CharSequence description,
			String taxType,
			int itemTypeId,
			EpicuriMenu.Printer printer,
			Collection<EpicuriMenu.ModifierGroup> modifierGroups,
			EpicuriMenu.Tag[] tags,
			String[] menuGroups,
			boolean unavailable,
			String shortCode,
            ArrayList<String> allergies,
            ArrayList<String> diets,
            String imageUrl,
			CharSequence plu){
		
		method ="PUT";
		path = String.format("/MenuItem/%s", id);
		body = setupBody(name, price, description, taxType, itemTypeId, printer, modifierGroups, tags, menuGroups, unavailable, shortCode, allergies, diets, imageUrl, plu);
	}
	
	public CreateEditMenuItemWebServiceCall(
			CharSequence name,
			Money price,
			CharSequence description,
			String taxType,
			int itemTypeId,
			EpicuriMenu.Printer printer,
			Collection<EpicuriMenu.ModifierGroup> modifierGroups,
			EpicuriMenu.Tag[] tags,
			String[] menuGroups,
			boolean unavailable,
			String shortCode,
            ArrayList<String> allergies,
            ArrayList<String> diets,
            String imageUrl,
			CharSequence plu){
		method ="POST";
		path = "/MenuItem";
		body = setupBody(name, price, description, taxType, itemTypeId, printer, modifierGroups, tags, menuGroups, unavailable, shortCode, allergies, diets, imageUrl, plu);
	}
	/*
{
    "Name": "Sausage",
    "Price": 1.23,
    "Description": "int",
    "TaxTypeId": 1,
    "DefaultPrinter":2,
    "ModifierGroups": [
        6
    ],
    "MenuGroups": [
        20
    ],
    "Tags": [
        1,
        2
    ]
}
	 */
	private String setupBody(
			CharSequence name,
			Money price,
			CharSequence description,
			String taxType,
			int itemTypeId,
			EpicuriMenu.Printer printer,
			Collection<EpicuriMenu.ModifierGroup> modifierGroups,
			EpicuriMenu.Tag[] tags,
			String[] menuGroups,
			boolean unavailable,
			String shortCode,
			ArrayList<String> allergies,
			ArrayList<String> diets,
			String imageUrl,
			CharSequence plu){
		JSONObject bodyJson = new JSONObject();
		try {
			bodyJson.put("Name", name);
			bodyJson.put("Price", price.getAmount().toPlainString());
			bodyJson.put("Description", description);
			bodyJson.put("TaxTypeId", taxType);
			bodyJson.put("DefaultPrinter", printer.getId());
			bodyJson.put("Unavailable", unavailable);
			bodyJson.put("ShortCode", shortCode);
			JSONArray modifierGroupsJson = new JSONArray();
			if(modifierGroups == null) {
				modifierGroups = new ArrayList<>();
			}
			for(EpicuriMenu.ModifierGroup g: modifierGroups){
				modifierGroupsJson.put(g.getId());
			}
			bodyJson.put("ModifierGroups", modifierGroupsJson);
			bodyJson.put("MenuItemTypeId", itemTypeId);
			
			JSONArray tagsJson = new JSONArray();
			for(int i=0; i<tags.length; i++){
				tagsJson.put(tags[i].getId());
			}
			bodyJson.put("TagIds", tagsJson);

			JSONArray groupsJson = new JSONArray();
			for(int i=0; i<menuGroups.length; i++){
				groupsJson.put(menuGroups[i]);
			}
			bodyJson.put("MenuGroups", groupsJson);

			JSONArray allergiesJson = new JSONArray();
			for (int i = 0; i < allergies.size(); i++){
				allergiesJson.put(allergies.get(i));
			}
			bodyJson.put("allergyIds", allergiesJson);

			JSONArray dietsJson = new JSONArray();
			for (int i = 0; i < diets.size(); i++){
				dietsJson.put(diets.get(i));
			}
			bodyJson.put("dietaryIds", dietsJson);

			bodyJson.put("imageURL", imageUrl);

			if(plu != null && plu.length() > 0 && plu.toString().trim().length() > 0) {
				bodyJson.put("plu", plu);
			}

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
		return new Uri[]{
				EpicuriContent.MENU_URI,
				EpicuriContent.MENUITEM_URI,
				EpicuriContent.MENUITEM_URI.buildUpon().appendQueryParameter("orphaned", "true").build(),
				EpicuriContent.MENUITEM_URI.buildUpon().appendQueryParameter("orphaned", "false").build()};
	}
}
