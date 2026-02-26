package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateCashUpWebServiceCall implements WebServiceCall {
	final String body;
	final boolean simulate;

	public CreateCashUpWebServiceCall(Date from, Date to){
		this(from, to, false);
	}

	public CreateCashUpWebServiceCall(Date from, Date to, boolean simulate){
		JSONObject o = new JSONObject();
		try {
			if(null != from){
				o.put("StartTime", from.getTime() / 1000L);
			}
			o.put("EndTime", to.getTime() / 1000L);
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
		this.simulate = simulate;
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
		return simulate ? "/CashUp/Simulate" : "/CashUp";
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return new Uri[]{EpicuriContent.CASHUP_URI};
	}
	
	
}
