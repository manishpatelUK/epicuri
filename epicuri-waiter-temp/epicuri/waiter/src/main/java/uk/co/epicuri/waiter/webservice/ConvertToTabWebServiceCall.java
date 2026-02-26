package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;

/**
 * Created by pharris on 28/07/15.
 */
public class ConvertToTabWebServiceCall implements WebServiceCall {
    final String body;
    final String sessionId;

    public ConvertToTabWebServiceCall(String sessionId, String name, int number, EpicuriCustomer customer, String[] tables, String serviceId) {
        /*
        {"PartyUpdate":{"Name":"Pete parry","NumberOfPeople":3,"CreateSession":true,"Tables":[],"ServiceId":27, "IsAdHoc":1},
"SessionId":3398}

         */
        this.sessionId = sessionId;
        JSONObject container = new JSONObject();
        try {
            JSONObject party = new JSONObject();
            party.put("Name", name);
            party.put("NumberOfPeople", number);
            party.put("ServiceId", serviceId);
            party.put("CreateSession", true);
            JSONArray tableArray = new JSONArray();
            for(String table: tables){
                tableArray.put(table);
            }
            party.put("Tables", tableArray);
            if(null != customer){
                JSONObject leadCustomer = new JSONObject();
                leadCustomer.put("Id", customer.getId());
                party.put("LeadCustomer", leadCustomer);
            }

            container.put("PartyUpdate", party);
            container.put("SessionId", sessionId);
        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }
        body = container.toString();
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
        return "/Session/ConvertAdHocToTab/";
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[]{
                Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId),
                EpicuriContent.SESSION_URI, EpicuriContent.PARTIES_URI};
    }
}
