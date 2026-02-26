package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class CustomerOnDinerWebServiceCall implements WebServiceCall {
/*
 * PUT api/Diner/{id}

Links an epicuri User to a diner, will not edit closed sessions. User must have checked in past 12 hours. Payload
Change guest name
{
	"EpicuriUser":{
		"Id":1,
		"guestName": "name"
	}
}
*/

	final String path;
	final String body;
	final String sessionId;
	public CustomerOnDinerWebServiceCall(EpicuriSessionDetail.Diner diner, EpicuriCustomer customer, String sessionId){
		path = String.format("/Diner/%s", diner.getId());
		body = String.format("{\"EpicuriUser\": {\"Id\": \"%s\"}}", customer.getId());
		this.sessionId = sessionId;
	}

    public CustomerOnDinerWebServiceCall(EpicuriSessionDetail.Diner diner, String sessionId, String newName){
        path = String.format("/Diner/%s", diner.getId());
        body = String.format("{\"guestName\": \"%s\"}", newName);
        this.sessionId = sessionId;
    }


    @Override
	public String getMethod() {
		return "PUT";
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
		return new Uri[]{EpicuriContent.CHECKIN_URI, Uri.withAppendedPath(EpicuriContent.SESSION_URI, String.valueOf(sessionId))};
	}
	
}
