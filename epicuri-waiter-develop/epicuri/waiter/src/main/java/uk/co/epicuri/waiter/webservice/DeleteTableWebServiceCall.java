package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class DeleteTableWebServiceCall implements WebServiceCall{
    final String path;

    public DeleteTableWebServiceCall(String tableId){
        path = String.format("/Table/%s", tableId);
    }

    @Override
    public String getMethod() {
        return "DELETE";
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
