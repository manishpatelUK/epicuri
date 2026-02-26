package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Preference implements Parcelable {
    public static final String TAG_KEY = "Key";
    public static final String TAG_VALUE = "Value";
    private String key;
    private String value;

    public Preference(JSONObject jsonObject) throws JSONException{
        key = jsonObject.getString(TAG_KEY);
        value = jsonObject.getString(TAG_VALUE);
    }

    protected Preference(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Preference> CREATOR = new Creator<Preference>() {
        @Override
        public Preference createFromParcel(Parcel in) {
            return new Preference(in);
        }

        @Override
        public Preference[] newArray(int size) {
            return new Preference[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}