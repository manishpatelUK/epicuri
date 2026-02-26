package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class VoidDeferredSessionWebServiceCall implements WebServiceCall {
    private final String path;

    public VoidDeferredSessionWebServiceCall(String sessionId) {
        this.path = "/Session/" + sessionId + "/voidDeferred";
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
        return null;
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
