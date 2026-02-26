package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class EpicuriVatRate implements Parcelable {
	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_RATE = "Rate";
	
	private final String id;
	private final double rate;
	private final String name;
	
	public String getId() {
		return id;
	}
	public double getRate() {
		return rate;
	}

	public String getName() {
		return name;
	}
	public EpicuriVatRate(JSONObject rJson) throws JSONException {
		id = rJson.getString(TAG_ID);
		name = rJson.getString(TAG_NAME);
		rate = rJson.getDouble(TAG_RATE);
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
		dest.writeDouble(rate);
	}
	private EpicuriVatRate(Parcel in){
		id = in.readString();
		name = in.readString();
		rate = in.readDouble();
	}
	
	public static final Parcelable.Creator<EpicuriVatRate> CREATOR = new Creator<EpicuriVatRate>() {
		
		@Override
		public EpicuriVatRate[] newArray(int size) {
			return new EpicuriVatRate[size];
		}
		
		@Override
		public EpicuriVatRate createFromParcel(Parcel source) {
			return new EpicuriVatRate(source);
		}
	};
}
