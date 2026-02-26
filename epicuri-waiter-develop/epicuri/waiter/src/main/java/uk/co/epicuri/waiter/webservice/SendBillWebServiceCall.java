package uk.co.epicuri.waiter.webservice;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;

/**
 * Created by antonandreev on 07.02.18.
 */

public class SendBillWebServiceCall implements WebServiceCall {

    private final JSONObject bodyJSON;
    private final String sessionId;

    public SendBillWebServiceCall(@NonNull String email, @NonNull String sessionId) {
        bodyJSON = new JSONObject();
        try {
            bodyJSON.put("email", email);
        } catch (JSONException e) {
            throw new RuntimeException("really shouldn't happen");
        }
        this.sessionId = sessionId;
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/comms/email/" + sessionId;
    }

    @Override
    public String getBody() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(bodyJSON);
        return jsonArray.toString();
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
