package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class EditPrinterWebServiceCall implements WebServiceCall {
    private final String id;
    private final String body;

    public EditPrinterWebServiceCall(String id, String ipAddress, String macAddress) {
        this.id = id;

        JSONObject o = new JSONObject();
        try {
            o.put("ipAddress", ipAddress);
            o.put("macAddress", macAddress);
        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }
        body = o.toString();
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public String getPath() {
        return "/Printer/" + id;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[0];
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
