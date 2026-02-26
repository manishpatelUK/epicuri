package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class CreateRedirectWebServiceCall implements WebServiceCall {
	final String path;
	final String body;

	public CreateRedirectWebServiceCall(String fromPrinter, String toPrinter) {
		path = "/Printer/Redirect";

		JSONObject o = new JSONObject();
		try {
			o.put("From", fromPrinter);
			o.put("To", toPrinter);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		body = o.toString();
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
		return new Uri[]{EpicuriContent.PRINTER_REDIRECT_URI};
	}
}
