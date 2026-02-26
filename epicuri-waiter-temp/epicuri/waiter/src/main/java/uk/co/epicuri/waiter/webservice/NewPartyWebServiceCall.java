package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;

public class NewPartyWebServiceCall implements WebServiceCall {
	final String body;

    public NewPartyWebServiceCall(String name, int number, EpicuriCustomer customer, String[] tables, String serviceId){
        JSONObject o = new JSONObject();
        try {
            o.put("Name", name);
            o.put("NumberOfPeople", number);
            o.put("ServiceId", serviceId);
            o.put("CreateSession", true);
            JSONArray tableArray = new JSONArray();
            for(String table: tables){
                tableArray.put(table);
            }
            o.put("Tables", tableArray);
            if(null != customer){
                JSONObject leadCustomer = new JSONObject();
                leadCustomer.put("Id", customer.getId());
                o.put("LeadCustomer", leadCustomer);
            }
        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }
        body = o.toString();
    }

	public NewPartyWebServiceCall(String name, int number, EpicuriCustomer customer){
		JSONObject o = new JSONObject();
		try {
			o.put("Name", name);
			o.put("NumberOfPeople", number);
			if(null != customer){
				JSONObject leadCustomer = new JSONObject();
				leadCustomer.put("Id", customer.getId());
				o.put("LeadCustomer", leadCustomer);
			}
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
		return true;
	}
	
	@Override
	public String getPath() {
		return "/Waiting";
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{EpicuriContent.PARTIES_URI, EpicuriContent.CHECKIN_URI};
	}
	
	
}
