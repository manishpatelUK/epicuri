package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PostLogFilesWebServiceCall implements WebServiceCall {
    private final List<String> logs;

    public PostLogFilesWebServiceCall(List<String> logs) {
        this.logs = logs;
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/device/logs";
    }

    @Override
    public String getBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("logs", new JSONArray(logs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
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
