package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

/**
 * Created by manish on 03/03/2018.
 */

public class GetSessionWebServiceCall implements WebServiceCall {
    private String id;

    public GetSessionWebServiceCall(String sessionId) {
        this.id = sessionId;
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/Session/" + id;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[]{};
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
