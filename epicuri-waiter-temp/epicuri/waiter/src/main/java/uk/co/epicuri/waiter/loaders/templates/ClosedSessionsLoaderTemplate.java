package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;

public class ClosedSessionsLoaderTemplate implements LoadTemplate<ArrayList<EpicuriSessionDetail>> {

	@Override
	public Uri getUri() {
		return EpicuriContent.CLOSED_SESSION_URI;
	}

	@Override
	public ArrayList<EpicuriSessionDetail> parseJson(String jsonString) throws JSONException {
		JSONArray sessionsJson = new JSONArray(jsonString);
		ArrayList<EpicuriSessionDetail> sessions = new ArrayList<EpicuriSessionDetail>(sessionsJson.length());
		
		for(int i=0; i<sessionsJson.length(); i++){
			JSONObject sessionJson = sessionsJson.getJSONObject(i);

			EpicuriSessionDetail session = new EpicuriSessionDetail(sessionJson);
			sessions.add(session);
		}
		Collections.sort(sessions, new Comparator<EpicuriSessionDetail>() {
			@Override
			public int compare(EpicuriSessionDetail lhs, EpicuriSessionDetail rhs) {
				if(lhs.getType() != rhs.getType()){
					// if one is DINE and the other isn't, then DINE comes first
					if(lhs.getType() == EpicuriSessionDetail.SessionType.DINE){
						return -1;
					} else if(rhs.getType() == EpicuriSessionDetail.SessionType.DINE){
						return 1;
					}
				}
				// otherwise, sort by time
				if(lhs.getType() == EpicuriSessionDetail.SessionType.DINE){
					// seated -> compare arrival time
					return(lhs.getStartTime().compareTo(rhs.getStartTime()));
				} else {
					// takeaway -> compare expected time
					return(lhs.getExpectedTime().compareTo(rhs.getExpectedTime()));
				}
			}
		});
		return sessions;
	}

}
