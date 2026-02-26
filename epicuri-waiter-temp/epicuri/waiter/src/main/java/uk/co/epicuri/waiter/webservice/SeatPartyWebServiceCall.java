package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriParty;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

/*
 * POST api/Session/FromParty/3

Turns a waiting party or reservation into a session. Tables can be empty.

Payload

{
"Tables":[3],
"ServiceId":1
 */

public class SeatPartyWebServiceCall implements WebServiceCall {
	private final String body;
	private final String method;
	private final String path;

	/**
	 * Used to reseat a party on different tables
	 * @param session
	 * @param tables
	 */
	public SeatPartyWebServiceCall(EpicuriSessionDetail session, List<String> tables){
		method = "PUT";
		path = String.format("/Session/Tables/%s", session.getId());
		JSONObject o = new JSONObject();
		try {
			JSONArray tableArray = new JSONArray();
			for(String table: tables){
				tableArray.put(table);
			}
			o.put("Tables", tableArray);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
	}

	/**
	 * seat a party, or create a tab by passing an empty tables array
	 * @param party
	 * @param tables
	 * @param serviceId
	 */
	public SeatPartyWebServiceCall(EpicuriParty party, List<String> tables, String serviceId){

		if(party.getSessionId() != null && !(party.getSessionId().equals("0")||party.getSessionId().equals("-1"))){
			method = "PUT";
			path = String.format("/Session/Tables/%s", party.getSessionId());
		} else {
			method = "POST";
			path = String.format("/Session/FromParty/%s", party.getId());
		}

		JSONObject o = new JSONObject();
		try {
			JSONArray tableArray = new JSONArray();
			for(String table: tables){
				tableArray.put(table);
			}
			o.put("Tables", tableArray);
			if(party.getSessionId() == null || party.getSessionId().equals("0") || party.getSessionId().equals("-1")){
				o.put("ServiceId", serviceId);
			}
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
	}

	@Override
	public String getMethod() {
		return method;
	}
	
	@Override
	public boolean requiresToken() {
		return true;
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
	public Uri[] getUrisToRefresh() {
		return new Uri[]{EpicuriContent.SESSION_URI, EpicuriContent.PARTIES_URI};
	}
	
}
