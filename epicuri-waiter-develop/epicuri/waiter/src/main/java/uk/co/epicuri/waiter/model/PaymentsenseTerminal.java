package uk.co.epicuri.waiter.model;


import android.os.Parcel;
import android.os.Parcelable;

public class PaymentsenseTerminal implements Parcelable {
    String tpi;
    String location;

    public PaymentsenseTerminal() {
    }

    public String getTpi() {
        return tpi;
    }

    public void setTpi(String tpi) {
        this.tpi = tpi;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tpi);
        dest.writeString(this.location);
    }

    protected PaymentsenseTerminal(Parcel in) {
        this.tpi = in.readString();
        this.location = in.readString();
    }

    public static final Parcelable.Creator<PaymentsenseTerminal> CREATOR =
            new Parcelable.Creator<PaymentsenseTerminal>() {
                @Override public PaymentsenseTerminal createFromParcel(Parcel source) {
                    return new PaymentsenseTerminal(source);
                }

                @Override public PaymentsenseTerminal[] newArray(int size) {
                    return new PaymentsenseTerminal[size];
                }
            };
}
