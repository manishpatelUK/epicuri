package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class GetPingWebServiceCall implements WebServiceCall {
    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/healthstatus/xping";
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
