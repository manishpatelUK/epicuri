package uk.co.epicuri.waiter;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class EpicuriApplication extends Application {
	/**
	 * API version of most recent HTTP request from server
	 */
	private int apiVersion;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		disableConnectionReuseIfNecessary();
	}

	private void disableConnectionReuseIfNecessary() {
		// Work around pre-Froyo bugs in HTTP connection reuse.
//		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");

//		}
	}

	public static EpicuriApplication getInstance(Context context){
		return (EpicuriApplication)context.getApplicationContext();
	}

	public void setApiVersion(int apiVersion) {
		this.apiVersion = apiVersion;
	}

	public int getApiVersion() {
		return apiVersion;
	}
}
