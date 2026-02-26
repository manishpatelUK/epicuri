package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StaffPermissions implements Parcelable{
    private StaffRole staffRole;
    private String roleReadableName;
    private List<WaiterAppFeatureView> booleanCapabilities = new ArrayList<>();

    public StaffPermissions(JSONObject jsonObject) throws JSONException {
        staffRole = StaffRole.valueOf(jsonObject.getString("role"));
        roleReadableName = jsonObject.getString("roleReadableName");
        JSONArray array = jsonObject.getJSONArray("booleanCapabilities");
        for(int i = 0; i < array.length(); i++) {
            booleanCapabilities.add(new WaiterAppFeatureView(array.getJSONObject(i)));
        }
    }

    protected StaffPermissions(Parcel in) {
        staffRole = StaffRole.valueOf(in.readString());
        roleReadableName = in.readString();
        booleanCapabilities = in.createTypedArrayList(WaiterAppFeatureView.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(staffRole.getReadableName());
        dest.writeString(roleReadableName);
        dest.writeTypedList(booleanCapabilities);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StaffPermissions> CREATOR = new Creator<StaffPermissions>() {
        @Override
        public StaffPermissions createFromParcel(Parcel in) {
            return new StaffPermissions(in);
        }

        @Override
        public StaffPermissions[] newArray(int size) {
            return new StaffPermissions[size];
        }
    };

    public StaffRole getStaffRole() {
        return staffRole;
    }

    public String getRoleReadableName() {
        return roleReadableName;
    }

    public List<WaiterAppFeatureView> getBooleanCapabilities() {
        return booleanCapabilities;
    }

    @Override public String toString() {
        return getRoleReadableName();
    }
}
