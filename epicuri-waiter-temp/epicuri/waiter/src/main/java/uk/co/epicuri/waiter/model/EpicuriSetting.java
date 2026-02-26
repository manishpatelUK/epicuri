package uk.co.epicuri.waiter.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class EpicuriSetting {
	// TODO: get units for the reservation time slot
	/** key for reservation time slot - get default reservation time (in hours??) */ 
	public static final String KEY_RESERVATION_TIME = "ReservationTimeSlot";
	
	private HashMap<String, String> settings;
	
	public String get(String key){
		return settings.get(key);
	}
	
	public EpicuriSetting(JSONObject object) throws JSONException {
		
		@SuppressWarnings("unchecked") //Using legacy API
		Iterator<String> i = object.keys();
		
		for(; i.hasNext(); ){
			String key = i.next();
			settings.put(key, object.getString(key)); 
		}
	}
}
