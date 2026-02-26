package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DeferredSession implements Parcelable, Serializable {
    private static final String TAG_SESSION_ID = "sessionId";
    private static final String TAG_STAFF_ID = "staffId";
    private static final String TAG_PAID = "paid";
    private static final String TAG_CREATION_TIME = "creationTime";
    private static final String TAG_CUSTOMER = "customer";
    private static final String TAG_REMAINING = "remainingTotal";

    private String sessionId;
    private String staffId;
    private boolean paid = false;
    private long creationTime = 0;
    private EpicuriCustomer customer;
    private double remaining = 0;

    public DeferredSession(JSONObject jsonObject) throws JSONException {
        sessionId = jsonObject.getString(TAG_SESSION_ID);
        staffId = jsonObject.getString(TAG_STAFF_ID);
        paid = jsonObject.getBoolean(TAG_PAID);
        creationTime = jsonObject.getLong(TAG_CREATION_TIME);
        JSONObject customerJson = jsonObject.getJSONObject(TAG_CUSTOMER);
        customer = new EpicuriCustomer(customerJson);
        remaining = jsonObject.getDouble(TAG_REMAINING);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public EpicuriCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(EpicuriCustomer customer) {
        this.customer = customer;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionId);
        dest.writeString(staffId);
        dest.writeByte(paid ? (byte)1 : (byte)0);
        dest.writeLong(creationTime);
        dest.writeDouble(remaining);
        customer.writeToParcel(dest, flags);
    }

    private DeferredSession(Parcel in) {
        sessionId = in.readString();
        staffId = in.readString();
        paid = in.readByte() == (byte) 1;
        creationTime = in.readLong();
        remaining = in.readDouble();
        customer = EpicuriCustomer.CREATOR.createFromParcel(in);
    }

    public static final Creator<DeferredSession> CREATOR = new Creator<DeferredSession>() {
        @Override
        public DeferredSession createFromParcel(Parcel in) {
            return new DeferredSession(in);
        }

        @Override
        public DeferredSession[] newArray(int size) {
            return new DeferredSession[size];
        }
    };
}
