package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriFloor;

public class FloorLayoutLoaderTemplate implements LoadTemplate<EpicuriFloor> {
	private static final String TAG_LAYOUTS = "Layouts";
	
	private final Uri uri;
	
	public FloorLayoutLoaderTemplate(String floorId) {
		this.uri = Uri.withAppendedPath(EpicuriContent.FLOOR_URI, floorId);
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}
	
	@Override
	public EpicuriFloor parseJson(String jsonString) throws JSONException {
		return new EpicuriFloor(new JSONObject(jsonString));
	}
}
