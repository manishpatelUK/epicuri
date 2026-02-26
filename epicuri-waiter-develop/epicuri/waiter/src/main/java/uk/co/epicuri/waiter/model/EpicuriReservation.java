package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class EpicuriReservation implements Parcelable {

	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_NOTES = "Notes";
	private static final String TAG_TELEPHONE = "Telephone";
	private static final String TAG_NUMBER_IN_PARTY = "NumberOfPeople";
	private static final String TAG_START_TIME = "ReservationTime";
	private static final String TAG_CUSTOMER = "LeadCustomer";
	private static final String TAG_ACCEPTED = "Accepted";
	private static final String TAG_SESSION_ID = "SessionId";
	private static final String TAG_ARRIVED = "ArrivedTime";
	private static final String TAG_TIMED_OUT = "TimedOut";
	private static final String TAG_DELETED = "Deleted";
	private static final String TAG_IS_BIRTHDAY = "IsBirthday";
	private static final String TAG_REJECTED_REASON = "RejectionNotice";
	private static final String TAG_OMIT_FROM_CHECKS = "omitFromChecks";
	private static final String TAG_EMAIL = "email";

	public enum ReservationType {
		RESERVATION_DINE("DINE"),
		RESERVATION_TAKE_AWAY("TAKEAWAY");
		
		private final String stringDef;
		ReservationType(String s){
			stringDef = s;
		}

		@Override
		public String toString() {
			return stringDef;
		}

		public static ReservationType fromString(String code) {
			if(null != code){
				for(ReservationType t: values()){
					if(t.stringDef.equalsIgnoreCase(code)){
						return t;
					}
				}
			}
			throw new IllegalArgumentException("ReservationType not recognised: " + code);
		}
	}
	
	public static EpicuriReservation at(Date time){
		EpicuriReservation r = new EpicuriReservation();
		r.setStartDate(time);
		r.setNumberInParty(1);
		return r;
	}
	
	private EpicuriReservation(){
		id = "-1";
		accepted = false;
		rejectedReason = null;
		sessionId = "0";
		name = "";
		startDate = new Date();
		arrivedTime = null;
		numberInParty = 1;
		notes = "";
		phoneNumber = "";
		epicuriUser = null;
		birthday = false;
		deleted = false;
		timedOut = false;
		omitFromChecks = false;
		type = ReservationType.RESERVATION_DINE;
		email = "";
	}

	private final String id;
	private final Date startDate;
	private final Date arrivedTime; 
	private final String name;
	private final String notes;
	private final int numberInParty;
	private final ReservationType type;
	private final EpicuriCustomer epicuriUser;
	private final String phoneNumber;
	private final String sessionId;
	private final boolean accepted;
	private final boolean deleted;
	private final boolean timedOut;
	private final String rejectedReason;
	private final int durationInMinutes = 120; // TODO: read duration from "Setting" web service
	private final boolean birthday;
	private final boolean omitFromChecks;
	private final String email;

	private boolean isModified = false;
	public boolean isModified() {
		return isModified;
	}
	
	public void revert(){
		if(!isModified) return;
		
		this.isModified = false;
	}
	
	private void setModified() {
		if(isModified()) return; //unchanged
		
		this.isModified = true;
		
		modifiedStartDate = startDate;
		modifiedName = name;
		modifiedNotes = notes;
		modifiedNumberInParty = numberInParty;
		modifiedPhoneNumber = phoneNumber;
		modifiedType = type;
	}

	private Date modifiedStartDate;
	private String modifiedName;
	private String modifiedNotes;
	private int modifiedNumberInParty;
	private ReservationType modifiedType;
	private String modifiedPhoneNumber;
	
	public Date getStartDate() {
		if(isModified) return modifiedStartDate;
		return startDate;
	}

	public void setStartDate(Date startDate) {
		setModified();
		this.modifiedStartDate = startDate;
	}

	public String getName() {
		if(isModified) return modifiedName;
		return name;
	}

	public void setName(String name) {
		setModified();
		this.modifiedName = name;
	}

	public String getNotes() {
		if(isModified) return modifiedNotes;
		return notes;
	}

	public void setNotes(String notes) {
		setModified();
		this.modifiedNotes = notes;
	}

	public int getNumberInParty() {
		if(isModified) return modifiedNumberInParty;
		return numberInParty;
	}

	public void setNumberInParty(int numberInParty) {
		setModified();
		this.modifiedNumberInParty = numberInParty;
	}

	public ReservationType getType() {
		if(isModified) return modifiedType;
		return type;
	}

	public void setType(ReservationType type) {
		setModified();
		this.modifiedType = type;
	}

	public String getPhoneNumber() {
		if(isModified) return modifiedPhoneNumber;
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		setModified();
		this.modifiedPhoneNumber = phoneNumber;
	}

	public String getId() {
		return id;
	}
	
	public boolean isAccepted() {
		return accepted;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isTimedOut() {
		return timedOut;
	}

	public boolean isBirthday() {
		return birthday;
	}

	public String getRejectedReason() {
		return rejectedReason;
	}

	public int getDurationInMinutes() {
		return durationInMinutes;
	}
	
	public EpicuriCustomer getEpicuriUser() {
		return epicuriUser;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Date getArrivedTime() {
		return arrivedTime;
	}

    public boolean isOmitFromChecks() {
        return omitFromChecks;
    }

	public String getEmail() {
		return email;
	}

	public EpicuriReservation(JSONObject reservationJson) throws JSONException{
		id = reservationJson.getString(TAG_ID);
		sessionId = reservationJson.getString(TAG_SESSION_ID);
		name = reservationJson.getString(TAG_NAME);
		birthday = reservationJson.getBoolean(TAG_IS_BIRTHDAY);
		startDate = new Date(1000L * reservationJson.getInt(TAG_START_TIME));
		if(reservationJson.has(TAG_REJECTED_REASON)){
			rejectedReason = reservationJson.getString(TAG_REJECTED_REASON);
		} else {
			rejectedReason = null;
		}
		if(reservationJson.isNull(TAG_ARRIVED)){
			arrivedTime = null;
		} else {
			arrivedTime = new Date(1000L * reservationJson.getInt(TAG_ARRIVED));
		}
		deleted = reservationJson.has(TAG_DELETED) && reservationJson.getBoolean(TAG_DELETED);
		timedOut = reservationJson.has(TAG_TIMED_OUT) && reservationJson.getBoolean(TAG_TIMED_OUT);
		numberInParty = reservationJson.getInt(TAG_NUMBER_IN_PARTY);
		notes = reservationJson.getString(TAG_NOTES);
		phoneNumber = reservationJson.getString(TAG_TELEPHONE);
		accepted = reservationJson.getBoolean(TAG_ACCEPTED);
		if(reservationJson.has(TAG_CUSTOMER) && !reservationJson.isNull(TAG_CUSTOMER)){
			epicuriUser = new EpicuriCustomer(reservationJson.getJSONObject(TAG_CUSTOMER));
		} else {
			epicuriUser = null;
		}

		if (reservationJson.has(TAG_OMIT_FROM_CHECKS)){
		    omitFromChecks = reservationJson.getBoolean(TAG_OMIT_FROM_CHECKS);
        } else {
		    omitFromChecks = false;
        }
		type = ReservationType.RESERVATION_DINE;
		if(reservationJson.has(TAG_EMAIL)) {
			email = reservationJson.getString(TAG_EMAIL);
		} else {
			email = null;
		}
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("{Reservation ").append(id == null ? "" : id)
		.append(",name").append(name)
		.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EpicuriReservation that = (EpicuriReservation) o;

		return id != null ? id.equals(that.id) : that.id == null;

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(sessionId);
		dest.writeLong(startDate.getTime());
		if(null == arrivedTime){
			dest.writeLong(0);
		} else {
			dest.writeLong(arrivedTime.getTime());
		}
		dest.writeString(name);
		dest.writeString(notes);
		dest.writeString(rejectedReason);
		dest.writeInt(numberInParty);
		dest.writeInt(type.ordinal());
		dest.writeParcelable(epicuriUser, 0);
		dest.writeString(phoneNumber);
		dest.writeInt(accepted ? 1 : 0);
		dest.writeInt(deleted ? 1 : 0);
		dest.writeInt(timedOut ? 1 : 0);
		dest.writeInt(birthday ? 1 : 0);
		dest.writeInt(omitFromChecks ? 1 : 0);
		dest.writeString(email);
	}

	private EpicuriReservation(Parcel in){
		id = in.readString();
		sessionId = in.readString();
		startDate = new Date(in.readLong());
		long arriveTime = in.readLong();
		if(0 == arriveTime){
			this.arrivedTime = null;
		} else {
			this.arrivedTime = new Date(arriveTime);
		}
		name = in.readString();
		notes = in.readString();
		rejectedReason = in.readString();
		numberInParty = in.readInt();
		type = ReservationType.values()[in.readInt()];
		epicuriUser = in.readParcelable(EpicuriCustomer.class.getClassLoader());
		phoneNumber = in.readString();
		accepted = in.readInt() == 0x1;
		deleted = in.readInt() == 0x1;
		timedOut = in.readInt() == 0x1;
		birthday = in.readInt() == 0x1;
		omitFromChecks = in.readInt() == 0x1;
		email = in.readString();
	}
	
	public static final Parcelable.Creator<EpicuriReservation> CREATOR = new Parcelable.Creator<EpicuriReservation>() {
		
		@Override
		public EpicuriReservation[] newArray(int size) {
			return new EpicuriReservation[size];
		}
		
		@Override
		public EpicuriReservation createFromParcel(Parcel source) {
			return new EpicuriReservation(source);
		}
	};
}
