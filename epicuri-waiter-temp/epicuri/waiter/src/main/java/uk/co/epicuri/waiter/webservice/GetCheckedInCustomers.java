package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class GetCheckedInCustomers implements WebServiceCall {
    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/Checkin?includeWithParty=true";
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
