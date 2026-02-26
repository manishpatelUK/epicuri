package uk.co.epicuri.waiter.loaders;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.ui.EpicuriPreferenceActivity;
import uk.co.epicuri.waiter.webservice.TokenManager;
import uk.co.epicuri.waiter.webservice.TokenManager.NotLoggedInException;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;
import uk.co.epicuri.waiter.webservice.WebServiceTask.MyResponse;
import uk.co.epicuri.waiter.webservice.WebServiceTask.WebServiceException;

public class OneOffLoader<T> extends AsyncTaskLoader<T> {
	private final LoadTemplate<T> lt;
	private final String mUrlPrefix;
	private static final String LOADER = "ONEOFF_LOADER";

	public OneOffLoader(Context context, LoadTemplate<T> lt) {
		super(context);
		this.lt = lt;

		mUrlPrefix = EpicuriPreferenceActivity.getUrlPrefix(context);
	}

	private T mData;

	@Override
	public T loadInBackground() {
		MyResponse r = makeRequest(lt.getUri());
		if(null == r.getException() && r.getResponseCode() >= 200 && r.getResponseCode() < 300){
			try {
				String response = r.getResponse();
				if(null != response) {
					Log.d(LOADER, lt.getClass().getName() + "->" + response);
					T result = lt.parseJson(response);
					return result;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onStartLoading() {
		if(null != mData){
			deliverResult(mData);
		}

		if(null == mData || takeContentChanged()){
			// always request a refresh when starting up
			forceLoad();
		}    	
	}
	

	@Override
	public void deliverResult(T data) {
		if(isReset()){
			mData = null;
			return;
		}
		mData = data;
		if(isStarted()){
			super.deliverResult(data);
		}
	}

	@Override
	protected void onReset() {
		onStopLoading();

		mData = null;
		super.onReset();
	}

	@Override
	public void onCanceled(T data) {
		super.onCanceled(data);
		mData = null;
	}

	private MyResponse makeRequest(final Uri uri){
		try{
			String token = TokenManager.getToken(getContext());
			MyResponse response;
			try {
                WebServiceCall call = new WebServiceCall() {
                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public String getPath() {
                        return UpdateService.getCacheStringFromUri(uri);
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
				response = WebServiceTask.makeRequest(call, mUrlPrefix, token, getContext(), 0);
			} catch (IOException e) {
				response = new MyResponse();
				response.setException(e);
			} catch (WebServiceException e) {
				response = new MyResponse();
				response.setException(e);
			}
			return response;
		} catch (NotLoggedInException e){
			MyResponse response = new MyResponse();
			response.setException(e);
			return response;
		}
	}

}
