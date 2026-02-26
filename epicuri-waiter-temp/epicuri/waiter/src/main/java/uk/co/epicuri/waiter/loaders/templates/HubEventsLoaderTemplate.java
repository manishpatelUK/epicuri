package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriEvent.HubNotification;

public class HubEventsLoaderTemplate implements LoadTemplate<List<HubNotification>> {
	
	@Override
	public Uri getUri() {
		return EpicuriContent.EVENT_URI;
	}

	@Override
	public List<EpicuriEvent.HubNotification> parseJson(String jsonString) throws JSONException {
		JSONArray eventsJson = new JSONArray(jsonString);

		List<EpicuriEvent.HubNotification> hubEvents = new ArrayList<EpicuriEvent.HubNotification>(eventsJson.length());

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -24);
		Date twentyFourHoursAgo = cal.getTime();

		for(int i=0; i<eventsJson.length(); i++){
			HubNotification e = new EpicuriEvent.HubNotification(eventsJson.getJSONObject(i));
			hubEvents.add(e);
		}
		Collections.sort(hubEvents, new Comparator<EpicuriEvent.HubNotification>() {

			@Override
			public int compare(HubNotification lhs, HubNotification rhs) {
				if(lhs.getType() != rhs.getType()){
					// bump adhoc events to the top of the list
					if(lhs.getType() == EpicuriEvent.Type.TYPE_ADHOC) return -1;
					else if(rhs.getType() == EpicuriEvent.Type.TYPE_ADHOC) return 1;
				}
				return lhs.getDue().compareTo(rhs.getDue());
			}
			
		});
		
		List<EpicuriEvent.HubNotification> limitedEvents = new ArrayList<EpicuriEvent.HubNotification>(eventsJson.length());
		//SparseBooleanArray sessions = new SparseBooleanArray();
		Map<String, Boolean> sessions = new HashMap<>();
		for(EpicuriEvent.HubNotification n: hubEvents){
			// if we haven't already seen a notification for this session
			if(null == sessions.get(n.getSessionId())){
				// all adhoc notifications, and any others from sessions

				// add to the list if it's due sooner than 24 hours ago
				if(n.getDue().after(twentyFourHoursAgo)){
					limitedEvents.add(n);
				}
				sessions.put(n.getSessionId(), true);
			}
		}
		
		return limitedEvents;
	}

}
