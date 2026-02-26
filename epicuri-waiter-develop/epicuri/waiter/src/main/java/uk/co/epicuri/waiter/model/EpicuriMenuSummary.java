package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class EpicuriMenuSummary implements Parcelable {
	private static final String TAG_NAME = "MenuName";
	private static final String TAG_ID = "Id";
	private static final String TAG_ACTIVE = "Active";
	private static final String TAG_ORDER = "order";

	private final String name;
	private final boolean active;
	private final String id;
	private final int order;
	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public EpicuriMenuSummary(JSONObject menuJson) throws JSONException {
		name = menuJson.getString(TAG_NAME);
		id = menuJson.getString(TAG_ID);
		active = menuJson.getBoolean(TAG_ACTIVE);
		order = menuJson.getInt(TAG_ORDER);
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
		dest.writeString(name);
		dest.writeInt(active ? 1 : 0);
		dest.writeInt(order);
	}

	public static final Parcelable.Creator<EpicuriMenuSummary> CREATOR = new Parcelable.Creator<EpicuriMenuSummary>() {
		public EpicuriMenuSummary createFromParcel(Parcel in) {
			return new EpicuriMenuSummary(in);
		}

		public EpicuriMenuSummary[] newArray(int size) {
			return new EpicuriMenuSummary[size];
		}
	};

	private EpicuriMenuSummary(Parcel in){
		id = in.readString();
		name = in.readString();
		active = in.readInt() == 1;
		order = in.readInt();
	}

	@Override public boolean equals(Object o) {
		if (this==o) return true;
		if (this == null || o == null) return false;
		if (this.getClass() != o.getClass()) return false;

		EpicuriMenuSummary ems = (EpicuriMenuSummary)o;
		return this.name.equals(ems.getName())&&(this.isActive() == ems.isActive())&&(this.getId
				().equals(ems.getId()));
	}

    public int getOrder() {
        return order;
    }
}
