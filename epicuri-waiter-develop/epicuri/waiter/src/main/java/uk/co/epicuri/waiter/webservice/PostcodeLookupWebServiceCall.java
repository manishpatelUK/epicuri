package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

/**
 * Created by antonandreev on 07.02.18.
 */

public class PostcodeLookupWebServiceCall implements WebServiceCall {
    private final String code;

    public PostcodeLookupWebServiceCall(String postcode) {
        this.code = postcode;
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/Customer/addressLookup?postcode="+code;
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
