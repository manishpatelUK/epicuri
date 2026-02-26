package uk.co.epicuri.waiter.loaders;

import android.net.Uri;

import org.json.JSONException;

public interface LoadTemplate<T> {
	Uri getUri();
	T parseJson(String jsonString)  throws JSONException;
}
