package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class WaiterAppFeatureView implements Parcelable{
    private WaiterAppFeature capability;
    private String capabilityReadableName;
    private boolean enabled;

    public WaiterAppFeatureView(JSONObject jsonObject) throws JSONException{
        capability = WaiterAppFeature.valueOf(jsonObject.getString("capability"));
        capabilityReadableName = jsonObject.getString("capabilityReadableName");
        enabled = jsonObject.getBoolean("enabled");
    }

    protected WaiterAppFeatureView(Parcel in) {
        capabilityReadableName = in.readString();
        enabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(capabilityReadableName);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WaiterAppFeatureView> CREATOR = new Creator<WaiterAppFeatureView>() {
        @Override
        public WaiterAppFeatureView createFromParcel(Parcel in) {
            return new WaiterAppFeatureView(in);
        }

        @Override
        public WaiterAppFeatureView[] newArray(int size) {
            return new WaiterAppFeatureView[size];
        }
    };

    public WaiterAppFeature getCapability() {
        return capability;
    }

    public String getCapabilityReadableName() {
        return capabilityReadableName;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
