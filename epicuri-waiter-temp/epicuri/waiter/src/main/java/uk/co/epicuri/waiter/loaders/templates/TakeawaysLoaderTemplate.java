package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;

public class TakeawaysLoaderTemplate implements LoadTemplate<ArrayList<EpicuriSessionDetail>> {
	private final Uri uri;
	
	private static Uri generateUri(Date fromTime, Date toTime){
		Uri.Builder buildUri = EpicuriContent.TAKEAWAY_URI.buildUpon();
		buildUri.appendQueryParameter("fromTime", String.valueOf((int)(fromTime.getTime() / 1000)));
		if(null != toTime){
			buildUri.appendQueryParameter("toTime", String.valueOf((int)(toTime.getTime() / 1000)));
		}
		return buildUri.build();
	}

	/**
	 * get pending takeaways
	 */
	public TakeawaysLoaderTemplate() {
		uri = EpicuriContent.TAKEAWAY_URI.buildUpon()
				.appendQueryParameter("pendingWaiterAction", "true")
				.build();
	}

	/**
	 * get all takeaways between fromTime and toTime
	 * @param fromTime
	 * @param toTime
	 */
	public TakeawaysLoaderTemplate(Date fromTime, Date toTime) {
		uri = generateUri(fromTime, toTime);
	}

	@Override
	public Uri getUri() {
		return uri;
	}
	
	@Override
	public ArrayList<EpicuriSessionDetail> parseJson(String jsonString) throws JSONException {
		JSONArray sessionsJson = new JSONArray(jsonString);
		ArrayList<EpicuriSessionDetail> sessions = new ArrayList<EpicuriSessionDetail>(sessionsJson.length());
		
		for(int i=0; i<sessionsJson.length(); i++){
			JSONObject sessionJson = sessionsJson.getJSONObject(i);

			EpicuriSessionDetail session = new EpicuriSessionDetail(sessionJson);
			if(session.getType() != SessionType.DINE){
				sessions.add(session);
			}
		}
		Collections.sort(sessions, new Comparator<EpicuriSessionDetail>() {
			@Override
			public int compare(EpicuriSessionDetail lhs,
					EpicuriSessionDetail rhs) {
				if(lhs.getExpectedTime().before(rhs.getExpectedTime())){
					return -1;
				} else if(lhs.getExpectedTime().after(rhs.getExpectedTime())){
					return 1;
				} else {
					return lhs.getName().compareTo(rhs.getName());
				}
			}
		});
		return sessions;
	}

}
