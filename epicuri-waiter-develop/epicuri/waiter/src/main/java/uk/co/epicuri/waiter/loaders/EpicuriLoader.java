package uk.co.epicuri.waiter.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONException;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.epicuri.waiter.BuildConfig;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;

public class EpicuriLoader<E> extends AsyncTaskLoader<LoaderWrapper<E>> {
	
	public static final int DEFAULT_REFRESH_PERIOD = 30000;
    private static final String LOADER = "EPICURI_LOADER";

	private final LoadTemplate<E> lt;

	private SharedPreferences prefs;
	
	public EpicuriLoader(Context context, LoadTemplate<E> lt) {
		super(context);
		this.lt = lt;
		prefs = context.getSharedPreferences(EpicuriContent.CACHE_FILE, Context.MODE_PRIVATE);
		
		// always request the first time
    	UpdateService.requestUpdate(getContext(), lt.getUri(), 5);
	}
	
	private ForceLoadContentObserver observer;
	
	public Uri getContentUri(){
		return lt.getUri();
	}
	
	@Override
	public LoaderWrapper<E> loadInBackground() {
		// start listening for changes to the data behind the URI
		getContext().getContentResolver().registerContentObserver(lt.getUri(), true, observer);
		String value = null;
		try {
			value = prefs.getString(UpdateService.getCacheStringFromUri(lt.getUri()), null);
			if(value == null){
				return null;
			}
			Log.d(LOADER, lt.getClass().getName() + "->" + value);
			return new LoaderWrapper<E>(lt.parseJson(value));
		} catch (JSONException e) {
			e.printStackTrace();
			if(BuildConfig.USE_CRASHLYTICS) {
//				Crashlytics.log("Error loading " + lt.getUri().toString());
//				Crashlytics.setString("Value", value);
//				Crashlytics.logException(e);
			}
			return LoaderWrapper.ERROR();
		} catch (RuntimeException e) {
			e.printStackTrace();
			if(BuildConfig.USE_CRASHLYTICS) {
//				Crashlytics.log("Error loading " + lt.getUri().toString());
//				Crashlytics.setString("Value", value);
//				Crashlytics.logException(e);
			}
			return LoaderWrapper.ERROR();
		}
	}
	
	private LoaderWrapper<E> mData;

	@Override
	public void deliverResult(LoaderWrapper<E> data) {
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
	protected void onStartLoading() {
		if(null != mData){
			deliverResult(mData);
		}

		// forceloader should call forceload when appropriate to update the data
		observer = new ForceLoadContentObserver();
		
		if(null == mData || takeContentChanged()){
			// always request a refresh when starting up
//			UpdateService.requestUpdate(getContext(), contentUri);
			forceLoad();
		}


		if(autoRefreshPeriod > 0){
			if(null != timer){
				throw new IllegalStateException("Too many timers");
			}
			timer = new Timer();
			long nextRefreshDue = lastRefreshTime + autoRefreshPeriod - new Date().getTime();
			if(nextRefreshDue < 0) nextRefreshDue = 0;
			Log.d(LOADER, "launching timer for " + lt.getUri() + " with initial delay as " + nextRefreshDue +  "  and interval as " + autoRefreshPeriod);
			timer.scheduleAtFixedRate(new TimerTask() {

				public void run() {
					Log.d(LOADER, "Firing timer for " + lt.getUri().toString());
					lastRefreshTime = new Date().getTime();
					UpdateService.requestUpdate(getContext(), lt.getUri(), 10);
				}

			}, nextRefreshDue, autoRefreshPeriod);
		} else {
			Log.d(LOADER, "not launching timer for " + lt.getUri());
		}
	}
	private long lastRefreshTime = 0;
	private Timer timer = null;
	@Override
	protected void onStopLoading() {
		if(null != timer){
			timer.cancel();
			timer = null;
		}
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		onStopLoading();

		if(null != observer){
			getContext().getContentResolver().unregisterContentObserver(observer);
			observer = null;
		}
		
		mData = null;
	}

	@Override
	public void onCanceled(LoaderWrapper<E> data) {
		super.onCanceled(data);
		mData = null;
	}
	
	private int autoRefreshPeriod = 0;
	public void setAutoRefreshPeriod(int millis){
		this.autoRefreshPeriod = millis;
	}
}
