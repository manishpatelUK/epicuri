package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

public interface WebServiceCall {
	String getMethod();
	String getPath();
	String getBody();
	Uri[] getUrisToRefresh();
	boolean requiresToken();
}
