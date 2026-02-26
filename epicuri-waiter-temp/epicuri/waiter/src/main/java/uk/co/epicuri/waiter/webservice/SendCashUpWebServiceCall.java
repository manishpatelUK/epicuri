package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public class SendCashUpWebServiceCall implements WebServiceCall {
    private final String cashupId;
    private long start = -1;
    private long end = -1;

    public SendCashUpWebServiceCall(String cashupId) {
        this.cashupId = cashupId;
    }

    public SendCashUpWebServiceCall(String cashupId, long start, long end){
        this(cashupId);
        this.start = start;
        this.end = end;
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        String path = "/comms/email/cashup/" + cashupId;
        if(start != -1 && end != -1){
            path+= "?&start="+start;
            path+= "&end="+end;
        }

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
