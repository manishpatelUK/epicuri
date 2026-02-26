package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class EpicuriService implements Parcelable {
/*
 * 

[{"Id":3,"MenuId":3,"MenuName":"A la carte","Schedule":null,"RecurringScheduleItems":null,"Courses":null},
{"Id":4,"MenuId":3,"MenuName":"A la carte","Schedule":null,"RecurringScheduleItems":null,"Courses":null},
{"Id":5,"MenuId":3,"MenuName":"A la carte","Schedule":null,"RecurringScheduleItems":null,"Courses":null}]


 */
	private static final String TAG_ID = "Id";
	private static final String TAG_MENU_ID = "MenuId";
	private static final String TAG_MENU_NAME = "ServiceName";
	private static final String TAG_SESSION_TYPE = "sessionType";

	//todo don't let fields be public! Use getters
	public final String id;
	public final String menuId;
	public final String name;
	public final String sessionType;
	
	public EpicuriService(JSONObject input) throws JSONException {
		id = input.getString(TAG_ID);
		menuId = input.getString(TAG_MENU_ID);
		name = input.getString(TAG_MENU_NAME);
		if(input.has(TAG_SESSION_TYPE)) {
			sessionType = input.getString(TAG_SESSION_TYPE);
		} else {
			sessionType = "None";
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
		dest.writeString(menuId);
		dest.writeString(name);
		dest.writeString(sessionType);
	}
	
	public static final Parcelable.Creator<EpicuriService> CREATOR = new Creator<EpicuriService>() {
		
		@Override
		public EpicuriService[] newArray(int size) {
			return new EpicuriService[size];
		}
		
		@Override
		public EpicuriService createFromParcel(Parcel source) {
			return new EpicuriService(source);
		}
	};
	
	private EpicuriService(Parcel in){
		id = in.readString();
		menuId = in.readString();
		name = in.readString();
		sessionType = in.readString();
	}
	
	
}
