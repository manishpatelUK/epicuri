package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

public class PutLongRunningWebServiceCall implements WebServiceCall{
    private String path, sessionId;
    private boolean longRunning;

    public PutLongRunningWebServiceCall(boolean longRunning, String sessionId) {
        this.longRunning = longRunning;
        this.path = "/Session/" + sessionId + "/longRunning";
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
        JSONObject json = new JSONObject();
        try {
            json.put("flag", longRunning);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
