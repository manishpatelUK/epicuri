package uk.co.epicuri.waiter.webservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

import uk.co.epicuri.waiter.BuildConfig;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.ValidWebServiceCall;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.ui.EpicuriPreferenceActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.TokenManager.NotLoggedInException;

public class WebServiceTask extends AsyncTask<Void, Integer, Boolean> {
	/**
	 * Increment this when the server API has a breaking change
	 */
	private static final int API_VERSION = 5;

	public static final String ACTION_UPDATE_REQUIRED = "uk.co.epicuri.waiter.action.UPDATE_REQUIRED";

    private static final int DEFAULT_RETRIES = 1;
    public static final String CONNECTION_ERROR = "No internet connection found.";
	private static final String WEB_SERVICE_TASK = "WebServiceTask";
	private static final Set<ValidWebServiceCall> set = new HashSet<>();

    public static Set<ValidWebServiceCall> getSet(){
	    synchronized (set){
	        return set;
        }
    }

	public interface OnSuccessListener {
		void onSuccess(int code, String response);
	}
	public interface OnErrorListener {
		void onError(int code, String response);
	}
	
	private final WebServiceCall mCall;
	private Context mContext;
	private String mToken;
	private final String mUrlPrefix;
	private final boolean mRequiresProgressBar;
	private CharSequence mDialogText = null;

	public WebServiceTask(Context context, WebServiceCall call){
		this(context, call, false);
	}

	public WebServiceTask(Context context, WebServiceCall call, boolean requiredProgressBar){
	    if(call instanceof QuickOrderWebServiceCall){
	        if(getSet().contains(call)){

            }
        }
		mCall = call;
		mContext = context;
		mUrlPrefix = EpicuriPreferenceActivity.getUrlPrefix(context);
		mRequiresProgressBar = requiredProgressBar;

		if(mCall.requiresToken()){
			try {
				mToken = TokenManager.getToken(context);
			} catch (NotLoggedInException e) {
				// not logged in, show login activity
				TokenManager.newTokenAction(context);
			}
		}
	}

	public void setIndicatorText(CharSequence text) {
		mDialogText = text;
	}
	
	private OnSuccessListener onSuccessListener = null;
	public void setOnCompleteListener(OnSuccessListener l){
		onSuccessListener = l;
	}
	
	private OnErrorListener onErrorListener = null;
	public void setOnErrorListener(OnErrorListener l){
		onErrorListener = l;
	}

	private SharedPreferences prefs;
	@Override
	protected void onPreExecute() {
		if(mRequiresProgressBar && null != mDialogText && mContext instanceof EpicuriBaseActivity){
			((EpicuriBaseActivity)mContext).showPleaseWaitDialog(mDialogText);
		}
		Log.d(WEB_SERVICE_TASK, String.format("%s to %s with %s",mCall.getMethod(), mCall.getPath(), mCall.getBody()));
		UpdateService.expireData(mContext, mCall.getUrisToRefresh());
		super.onPreExecute();
	}

	private MyResponse response;
	private Exception error;

	@Override
	protected Boolean doInBackground(Void... arg0) {
        if (!networkAvailable(mContext)) {
            error = new Exception(CONNECTION_ERROR);
            return false;
        }

		try {
			response = makeRequest(mCall, mUrlPrefix, mToken, mContext, DEFAULT_RETRIES);
		} catch (UnsupportedEncodingException e) {
			error = e;
			Log.e("WebServiceTask error", e.toString());
			return false;
		} catch (IOException e) {
			error = e;
			Log.e("WebServiceTask IO error", e.toString());
			return false;
		} catch (WebServiceException e) {
			error = e;
			Log.e("WebServiceTask error", e.toString());
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(mRequiresProgressBar && mContext instanceof EpicuriBaseActivity){
			((EpicuriBaseActivity)mContext).dismissPleaseWaitDialog();
		}
			
		if(result){
			Log.d(WEB_SERVICE_TASK,String.format("Returned %d: %s", response.responseCode, response.response));
			if(response.responseCode == 401){
				TokenManager.logout(mContext);
			}
			boolean ok = response.responseCode >= 200 && response.responseCode  < 300;
			if(ok){
				if(null != mCall.getUrisToRefresh()) {
					for (Uri uri : mCall.getUrisToRefresh()) {
                        if (!uri.equals(EpicuriContent.CLOSED_SESSION_URI)) {
                            UpdateService.requestUpdate(mContext, uri, 6);
                        }
                    }
				}
				if(null != onSuccessListener){
					onSuccessListener.onSuccess(response.responseCode, response.response);
				}
				return;
			} else if(null != onErrorListener){
				Log.e("WebServiceTask", "Propogate error: " + response.getResponse());
				onErrorListener.onError(response.responseCode, response.response);
			} else {
			
				String message = "Unhandled error response - " + response.responseCode;
				try {
					if(null != response.response){
						JSONObject responseJson = new JSONObject(response.response);
						message = responseJson.getString("Message");
						if(responseJson.has("MessageDetail")){
							message = message + "\n\n" + responseJson.getString("MessageDetail");
						}
					}
				} catch (JSONException e){
					Log.e("WebServiceTask IO erro", "error: " + e.toString());
				}
				Log.e(WEB_SERVICE_TASK, message);
				if(null != mDialogText){
					Log.e("WebServiceTask", "error message: " + message);
					Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			error.printStackTrace();
			if(null != onErrorListener){
				if(error instanceof UpgradeRequiredException){
					onErrorListener.onError(HttpURLConnection.HTTP_NOT_ACCEPTABLE, error.getMessage());
				} else {
					onErrorListener.onError(0, error.getMessage());
				}
			} else if(null != mDialogText){
				Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public static class WebServiceException extends Exception {
		private static final long serialVersionUID = 4529439136255522120L;
		
		private final int mCode;
		public WebServiceException(int code){
			super();
			mCode = code;
		}
		
		@Override
		public String getMessage() {
			return String.format(Locale.UK, "Server responded with status %d", mCode);
		}

		public int getCode(){
			return mCode;
		}
	}

	public static class UpgradeRequiredException extends WebServiceException {
		public UpgradeRequiredException() {
			super(HttpURLConnection.HTTP_NOT_ACCEPTABLE);
		}

		@Override
		public String getMessage() {
			return "Client is too old to understand the Epicuri server";
		}
	}
	
	public static class MyResponse {
		private String response;
		private int responseCode;
		private Exception exception;
		public String apiVersionString;

		public String getResponse() {
			return response;
		}
		public int getResponseCode() {
			return responseCode;
		}
		public Exception getException() {
			return exception;
		}
		public void setException(Exception exception) {
			this.exception = exception;
		}
	}

	private static int increment = 0;
	
	private static String cachedUserAgent;
	private static String getUserAgent(Context context){
		if(null == cachedUserAgent){
			try {
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				cachedUserAgent = String.format(Locale.UK, "EpicuriWaiter/%d", pInfo.versionCode);
			} catch (Exception e) {
				Log.e(WEB_SERVICE_TASK, "Error whilst getting version", e);
				Log.i(WEB_SERVICE_TASK, "Returning default package value of " + BuildConfig.VERSION_CODE);
				cachedUserAgent = String.format(Locale.UK, "EpicuriWaiter/%d", BuildConfig.VERSION_CODE);
			}
		}
		return cachedUserAgent;
	}
	
	@SuppressLint("HardwareIds")
	public static MyResponse makeRequest(WebServiceCall call, String urlPrefix, String token, Context context, int attempt) throws IOException, WebServiceException {
        URL url = new URL(urlPrefix + call.getPath());
		HttpURLConnection urlConnection = null;

		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = manager.getConnectionInfo();

		final int thisInstance = increment++;
		Log.d(WEB_SERVICE_TASK, String.format("TASK %d is %s %s", thisInstance, call.getMethod(), call.getPath()));
		try {
			urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod(call.getMethod());
			urlConnection.setReadTimeout(20000);
			urlConnection.setConnectTimeout(10000);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("User-Agent", getUserAgent(context));

			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("X-Epicuri-API-Version", String.valueOf(API_VERSION));
			int ip = wifiInfo.getIpAddress();
			String ipStr = String.format(Locale.UK, "%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
			urlConnection.setRequestProperty("X-Epicuri-IP", ipStr);
			urlConnection.setRequestProperty("X-Epicuri-MAC", wifiInfo.getMacAddress());
			urlConnection.setRequestProperty("X-Epicuri-SSID", wifiInfo.getSSID());
			if(null != token){
				urlConnection.setRequestProperty("Authorization", token);
			}

			MyResponse myResponse = new MyResponse();

			if(null != call.getBody()){
				urlConnection.setDoOutput(true);
				urlConnection.setChunkedStreamingMode(0);
                PrintWriter pw = new PrintWriter(new BufferedOutputStream(urlConnection.getOutputStream()));
                pw.write(call.getBody());
				pw.flush();
				pw.close();
			}

			long elapsed = - new Date().getTime();
			urlConnection.connect();
			
			myResponse.responseCode = urlConnection.getResponseCode();
			if(myResponse.responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE){
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(ACTION_UPDATE_REQUIRED);
				broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

				throw new UpgradeRequiredException();
			}
			elapsed += new Date().getTime();
			if(elapsed > 9000) {
				Log.e(WEB_SERVICE_TASK, "TASK " + (thisInstance) + " response code " + myResponse.responseCode + " took " + elapsed + " Milliseconds");
			} else if(elapsed > 3000) {
				Log.w(WEB_SERVICE_TASK, "TASK " + (thisInstance) + " response code " + myResponse.responseCode + " took " + elapsed + " Milliseconds");
			} else {
				Log.i(WEB_SERVICE_TASK, "TASK " + (thisInstance) + " response code " + myResponse.responseCode + " took " + elapsed + " Milliseconds");
			}

			if(urlConnection.getErrorStream() != null){
				myResponse.response = streamToString(urlConnection.getErrorStream());	
			} else {
				myResponse.response = streamToString(urlConnection.getInputStream());
			}
			myResponse.apiVersionString = urlConnection.getHeaderField("X-Epicuri-API-Version");
			if(null == myResponse.apiVersionString) myResponse.apiVersionString = "0";
            int apiVersion = 0;
            try {
                apiVersion = Integer.parseInt(myResponse.apiVersionString);
            } catch (NumberFormatException e){
//                use default of zero
            }
			EpicuriApplication.getInstance(context).setApiVersion(apiVersion);

			Log.d(WEB_SERVICE_TASK, String.format("TASK %d response received", thisInstance));
			if(myResponse.getResponse() != null) {
				Log.d(WEB_SERVICE_TASK, String.format("  Response TASK %d: %s",thisInstance, myResponse.getResponse()));
			}
            
			return myResponse;
		} catch (SocketTimeoutException timeoutException){
			Log.e(WEB_SERVICE_TASK, String.format("TASK %d socket timed out. %d tries remaining for %s", thisInstance, attempt-1, url.toString()));
			if(attempt > 0){
				return makeRequest(call, urlPrefix, token, context, attempt-1);
			} else {
				throw timeoutException;
			}
		} catch (IOException ioExceptio){
			Log.e(WEB_SERVICE_TASK, "IO Exception for "  + url.toString());
			throw(ioExceptio);
		} finally {
			if(null != urlConnection){
				urlConnection.disconnect();
			}
		}
	}
	
	private static String streamToString(InputStream in){
		InputStream bufferedin = new BufferedInputStream(in);
		String response = null;
        Scanner scanner = new Scanner(bufferedin, "UTF-8").useDelimiter("\\A");
        if (scanner.hasNext()){
        	response = scanner.next();
        }
        try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return response;
	}

    public boolean networkAvailable(Context context) {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm == null) {
            	return false;
			}
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isAvailable();
            /*NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    status = true;
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //return status;

    }
	
}
