package uk.co.epicuri.waiter.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EpicuriLogin implements Parcelable {
	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_USERNAME = "Username";
	private static final String TAG_MANAGER = "Manager";
	private static final String TAG_PIN = "Pin";
	public static final String TAG_ROLE = "Role";
	
	private final String name;
	private final String username;
	private final String id;
	private final String pin;
	private final String role;
	private final boolean manager;
	
	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getId() {
		return id;
	}

	public boolean isManager() {
		return manager;
	}

	public String getPin() {
		return pin;
	}

    public String getRole() {
        return role;
    }

	private EpicuriLogin(SharedPreferences prefs){

		name = prefs.getString(GlobalSettings.PREF_KEY_NAME, "");
		username = prefs.getString(GlobalSettings.PREF_KEY_USERNAME, "");
		pin = prefs.getString(GlobalSettings.PREF_KEY_PIN, "");
		manager = prefs.getBoolean(GlobalSettings.PREF_KEY_MANAGER, false);
		role = prefs.getString(GlobalSettings.PREF_KEY_ROLE, "");
		String storedId;
		try {
			storedId = prefs.getString(GlobalSettings.PREF_KEY_ID, "-1");
		} catch (Exception ex) {
			storedId = "-1";
		}
		id = storedId;
	}

	public static EpicuriLogin fromPreferences(Context c){
		SharedPreferences prefs = c.getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		if(!prefs.contains(GlobalSettings.PREF_KEY_USERNAME)) return null;
		return new EpicuriLogin(prefs);
	}

	public EpicuriLogin(JSONObject o) throws JSONException {
		name = o.getString(TAG_NAME);
		id = o.getString(TAG_ID);
		manager = o.getBoolean(TAG_MANAGER);
		username = o.getString(TAG_USERNAME);
		if(o.has(TAG_PIN)){
			pin = o.getString(TAG_PIN);
		} else {
			pin = null;
		}

        if(o.has(TAG_ROLE)){
            role = o.getString(TAG_ROLE);
        } else {
            role = null;
        }
    }

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(username);
		dest.writeString(name);
		dest.writeInt(manager ? 1 : 0);
		dest.writeString(pin);
		dest.writeString(role);
	}

	public static final Parcelable.Creator<EpicuriLogin> CREATOR = new Parcelable.Creator<EpicuriLogin>() {

		@Override
		public EpicuriLogin[] newArray(int size) {
			return new EpicuriLogin[size];
		}

		@Override
		public EpicuriLogin createFromParcel(Parcel source) {
			return new EpicuriLogin(source);
		}
	};

	private EpicuriLogin(Parcel in){
		id = in.readString();
		username = in.readString();
		name = in.readString();
		manager = in.readInt() == 1;
		pin = in.readString();
		role = in.readString();
	}

}

