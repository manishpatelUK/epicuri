package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class GetStockControlWebServiceCall implements WebServiceCall {
    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/StockControl";
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
