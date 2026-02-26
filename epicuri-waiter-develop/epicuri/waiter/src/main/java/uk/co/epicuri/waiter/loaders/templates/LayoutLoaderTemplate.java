package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriFloor.Layout;

public class LayoutLoaderTemplate implements LoadTemplate<Layout> {
	private final Uri uri;
	public LayoutLoaderTemplate(String layoutId) {
		uri = Uri.withAppendedPath(EpicuriContent.LAYOUT_URI, layoutId);
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}
	
	@Override
	public Layout parseJson(String jsonString) throws JSONException {
		return new Layout(new JSONObject(jsonString));
	}

}
