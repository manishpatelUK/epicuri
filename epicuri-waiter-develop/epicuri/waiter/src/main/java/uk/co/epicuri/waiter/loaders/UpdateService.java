package uk.co.epicuri.waiter.loaders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.ui.EpicuriPreferenceActivity;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.TokenManager.NotLoggedInException;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;
import uk.co.epicuri.waiter.webservice.WebServiceTask.MyResponse;
import uk.co.epicuri.waiter.webservice.WebServiceTask.WebServiceException;

/**
 * background process to load data from webservice into contentprovider 
 * @author Pete Harris <peteh@thedistance.co.uk>
 */
public class UpdateService extends PriorityIntentService {

	public static final String ACTION_CONNECTION_ERROR = "uk.co.epicuri.waiter.CONNECTION_ERROR"; 
	public static final String ACTION_REFRESH_STARTED = "uk.co.epicuri.waiter.REFRESH_STARTED";
	public static final String ACTION_REFRESH_STOPPED = "uk.co.epicuri.waiter.REFRESH_STOPPED";
	public UpdateService() {
		super("EpicuriUpdateService");
	}

	private SharedPreferences prefs;
    LocalBroadcastManager bm;
	private String mUrlPrefix;

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = getSharedPreferences(EpicuriContent.CACHE_FILE, MODE_PRIVATE);
		mUrlPrefix = EpicuriPreferenceActivity.getUrlPrefix(this);
	}
	
	@Override
	protected boolean shouldAddToQueue(QueueItem item, PriorityBlockingQueue<QueueItem> queue) {
		if(item == null || item.intent == null) {
			return false;
		}
		Uri data = item.intent.getData();
		for(QueueItem qi: queue){
			// don't insert duplicate requests
			if(qi.intent.getData().equals(data)) return false;
		}
		return super.shouldAddToQueue(item, queue);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_REFRESH_STARTED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_REFRESH_STOPPED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
	    bm = LocalBroadcastManager.getInstance(this);
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_REFRESH_STARTED);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		bm.sendBroadcast(broadcastIntent);

		Uri contentUri = intent.getData();
		makeRequest(contentUri);
	}

	private void makeRequest(final Uri uri){
		Log.d("EpicuriUpdateService", "make request for " + uri);
		try{
			String token = TokenManager.getToken(this);
			MyResponse response;
			try {
                WebServiceCall call = new WebServiceCall() {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public String getPath() {
                        return getCacheStringFromUri(uri);
                    }

                    @Override
                    public String getBody() {
                        return null;
                    }

                    @Override
                    public Uri[] getUrisToRefresh() {
                        return null;
                    }

                    @Override
                    public boolean requiresToken() {
                        return true;
                    }
                };
				response = WebServiceTask.makeRequest(call, mUrlPrefix, token, this, 0);
			} catch (IOException e) {
				response = new MyResponse();
				response.setException(e);
			} catch (WebServiceException e) {
				response = new MyResponse();
				response.setException(e);
			}
			handleResponse(response, uri);
			return;
		} catch (NotLoggedInException e){
			return;
		}
	}

	/**
	 * handle response, along with any exceptions
	 * @param response
	 * @param uri
	 */
	private void handleResponse(MyResponse response, Uri uri){
		Log.d("EpicuriUpdateService", "received result for " + uri);
		String error;
		int statusCode = response.getResponseCode();
		if(statusCode >= 200 && statusCode < 300) {
			String responseString = response.getResponse();
			if(null != responseString){
				updateContentProvider(responseString, uri);
				return;
			} else {
				error = "No body returned by " + uri.toString();
			}
		} else if(statusCode == 401 ){
			TokenManager.logout(this);
			return;
		} else if(response.getException() != null){
			error = response.getException().toString();
		} else {
			error = response.getResponse();
		}


        bm = LocalBroadcastManager.getInstance(this);
        Intent errorIntent = new Intent(ACTION_CONNECTION_ERROR);
		errorIntent.addCategory(Intent.CATEGORY_DEFAULT);
		errorIntent.putExtra(Intent.EXTRA_TEXT, error);
		bm.sendBroadcast(errorIntent);
	}

	// actually do the update
	private void updateContentProvider(String responseBody, Uri uri){
		String cacheKey = getCacheStringFromUri(uri);
		prefs.edit().putString(cacheKey, responseBody).commit();

		getContentResolver().notifyChange(Uri.parse("content://" + EpicuriContent.AUTHORITY + cacheKey), null);
	}

	public static String getCacheStringFromUri(Uri uri){
		String queryString = uri.getPath();
		if(null != uri.getQuery()){
			queryString += "?" + uri.getQuery();
		}
		return queryString;
	}

	/**
	 * static method for ease of calling refresh
	 * @param context
	 * @param uri
	 */
	public static void requestUpdate(Context context, Uri uri){
		requestUpdate(context, uri, 0);
	}
	public static void requestUpdate(Context context, Uri uri, int priority){
		Intent intent = new Intent(context, UpdateService.class);
		intent.setData(uri);
		intent.putExtra(EXTRA_PRIORITY, priority);
		try {
			context.startService(intent);
		} catch (Exception ex) {
			Log.e("UpdateService","Could not start UpdateService",ex);
		}
	}

	public static void expireData(Context context, Uri[] urisToRefresh) {
		if(null == urisToRefresh) return;
		SharedPreferences.Editor e = context.getSharedPreferences(EpicuriContent.CACHE_FILE, MODE_PRIVATE).edit();
		for(Uri uri: urisToRefresh){
			e.remove(getCacheStringFromUri(uri));
			context.getContentResolver().notifyChange(uri, null);
		}
		e.commit();
	}
}
