package uk.co.epicuri.waiter.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class SavedState extends View.BaseSavedState {

    private final int mHour;
    private final int mMinute;

    public SavedState(Parcelable superState, int hour, int minute) {
        super(superState);
        mHour = hour;
        mMinute = minute;
    }

    private SavedState(Parcel in) {
        super(in);
        mHour = in.readInt();
        mMinute = in.readInt();
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mHour);
        dest.writeInt(mMinute);
    }

    public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };
}