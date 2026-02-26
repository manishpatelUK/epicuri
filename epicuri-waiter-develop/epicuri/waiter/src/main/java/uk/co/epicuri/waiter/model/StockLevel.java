package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class StockLevel implements Parcelable, Serializable {
    private String id;
    private String restaurantId;
    private String plu;
    private int level = 0;
    private boolean trackable;

    public StockLevel(String id, String restaurantId, String plu, int level, boolean trackable) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.plu = plu;
        this.level = level;
        this.trackable = trackable;
    }

    public StockLevel(JSONObject object) throws JSONException {
        if(object.has("id")) {
            this.id = object.getString("id");
        }
        this.restaurantId = object.getString("restaurantId");
        this.plu = object.getString("plu");
        this.level = object.getInt("level");
        this.trackable = object.getBoolean("trackable");
    }

    public StockLevel(Parcel in) {
        this.id = in.readString();
        this.restaurantId = in.readString();
        this.plu = in.readString();
        this.level = in.readInt();
        this.trackable = in.readByte() == 0x01;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.restaurantId);
        dest.writeString(this.plu);
        dest.writeInt(this.level);
        dest.writeByte(this.trackable ? (byte)0x01 : (byte)0x00);
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("restaurantId", restaurantId);
            jsonObject.put("plu", plu);
            jsonObject.put("level", level);
            jsonObject.put("trackable", trackable);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonObject.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isTrackable() {
        return trackable;
    }

    public void setTrackable(boolean trackable) {
        this.trackable = trackable;
    }

    public static final Creator<StockLevel> CREATOR = new Creator<StockLevel>() {
        public StockLevel createFromParcel(Parcel source) {
            return new StockLevel(source);
        }

        public StockLevel[] newArray(int size) {
            return new StockLevel[size];
        }
    };
}
