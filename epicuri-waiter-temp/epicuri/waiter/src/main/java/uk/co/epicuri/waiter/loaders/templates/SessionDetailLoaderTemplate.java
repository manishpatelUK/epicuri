package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.loaders.templates.LoadTemplate;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class SessionDetailLoaderTemplate implements LoadTemplate<EpicuriSessionDetail> {
	private final Uri uri;

	public SessionDetailLoaderTemplate(String sessionId) {
		uri = Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId);
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}
	
	@Override
	public EpicuriSessionDetail parseJson(String jsonString) throws JSONException {
		JSONObject jsonObject= new JSONObject(jsonString);

		return new EpicuriSessionDetail(jsonObject);
	}
}
