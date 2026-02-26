package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class EpicuriParty implements Parcelable {
	private static final String TAG_ID = "Id";
	private static final String TAG_NUMBER_OF_PEOPLE = "NumberOfPeople";
	private static final String TAG_NAME = "Name";
	private static final String TAG_CREATED = "Created";
	private static final String TAG_SESSION_ID = "SessionId";
	private static final String TAG_RESERVATION = "ReservationTime";
	private static final String TAG_ACCEPTED = "Accepted";
	private static final String TAG_ARRIVED_TIME = "ArrivedTime";
	private static final String TAG_LEAD_CUSTOMER = "LeadCustomer";
	private static final String TAG_SESSION_TYPE = "sessionType";
    private static final String TAG_BOOKING_ID = "bookingId";

	public static final String TYPE_NONE = "NONE";
	public static final String TYPE_ADHOC = "ADHOC";

	private final String id;
	private final String sessionId;
	private final String sessionType;
	private final String partyName;
	private final int numberInParty;
	private final Date createTime;
	private final Date reservationTime;
	private final Date arrivedTime;
	private final boolean accepted;
	private final EpicuriCustomer leadCustomer;
	private final String reservationId;

	public String getId() {
		return id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getPartyName() {
		return partyName;
	}

	public int getNumberInParty() {
		return numberInParty;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public Date getReservationTime() {
		return reservationTime;
	}

	public Date getArrivedTime() {
		return arrivedTime;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public String getSessionType() {
		return sessionType;
	}

	public EpicuriCustomer getLeadCustomer() {
		return leadCustomer;
	}

	public String getReservationId() {
	    return reservationId;
	}

	/*
	 * [{"Id":3,"NumberofPeople":5,"Name":"Joe Bloggs","Created":1355849421.0},{"Id":6,"NumberofPeople":5,"Name":"Joe Bloggs","Created":1355850116.0}]
	 */
	public EpicuriParty(JSONObject partyJson) throws JSONException {
		id = partyJson.getString(TAG_ID);
	//	id = partyJson.getString(TAG_ID);
		partyName = partyJson.has(TAG_NAME) ? partyJson.getString(TAG_NAME) : "";
		numberInParty = partyJson.getInt(TAG_NUMBER_OF_PEOPLE);
		createTime = new Date(1000L * partyJson.getInt(TAG_CREATED));
		accepted = partyJson.has(TAG_ACCEPTED) && partyJson.getBoolean(TAG_ACCEPTED);
		if(partyJson.has(TAG_RESERVATION)){
			reservationTime = new Date(1000L * partyJson.getInt(TAG_RESERVATION));
		} else {
			reservationTime = null;
		}
		if(partyJson.has(TAG_SESSION_ID)){
			sessionId = partyJson.getString(TAG_SESSION_ID);
		} else {
			sessionId = "-1";
		}
		if(partyJson.has(TAG_ARRIVED_TIME)){
			arrivedTime = new Date(1000L * partyJson.getLong(TAG_ARRIVED_TIME));
		} else {
			arrivedTime = null;
		}
		if(partyJson.has(TAG_LEAD_CUSTOMER) && !partyJson.isNull(TAG_LEAD_CUSTOMER)){
			leadCustomer = new EpicuriCustomer(partyJson.getJSONObject(TAG_LEAD_CUSTOMER));
		} else {
			leadCustomer = null;
		}
		if(partyJson.has(TAG_SESSION_TYPE) && !partyJson.isNull(TAG_SESSION_TYPE)){
			sessionType = partyJson.getString(TAG_SESSION_TYPE);
		} else{
			sessionType = null;
		}

        if(partyJson.has(TAG_BOOKING_ID)){
            reservationId = partyJson.getString(TAG_BOOKING_ID);
        } else {
            reservationId = "-1";
        }
	}

	@Override
	public String toString() {
		return new StringBuffer()
		.append("{Party ").append(id)
		.append(",name: ").append(partyName)
		.append(",num: ").append(numberInParty)
		.append(",created: ").append(LocalSettings.getDateFormat().format(createTime))
		.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EpicuriParty that = (EpicuriParty) o;

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

	public static final Parcelable.Creator<EpicuriParty> CREATOR = new Parcelable.Creator<EpicuriParty>() {
		public EpicuriParty createFromParcel(Parcel in) {
			return new EpicuriParty(in);
		}

		public EpicuriParty[] newArray(int size) {
			return new EpicuriParty[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(partyName);
		dest.writeInt(numberInParty);
		dest.writeString(sessionId);
		dest.writeParcelable(leadCustomer, 0);
		dest.writeLong(null != createTime ? createTime.getTime() : 0);
		dest.writeLong(null != reservationTime ? reservationTime.getTime() : 0);
		dest.writeLong(null != arrivedTime ? arrivedTime.getTime() : 0);
		dest.writeByte((byte) (accepted  ? 1 : 0));
		dest.writeString(sessionType);
		dest.writeString(reservationId);
	}

	private EpicuriParty(Parcel in) {
		id = in.readString();
		partyName = in.readString();
		numberInParty = in.readInt();
		sessionId = in.readString();
		leadCustomer = in.readParcelable(EpicuriCustomer.class.getClassLoader());
		long t;
		t = in.readLong();
		if(t>0){
			createTime = new Date(t);
		} else {
			createTime = null; // shouldn't be null?
		}

		t = in.readLong();
		if(t>0){
			reservationTime = new Date(t);
		} else {
			reservationTime = null;
		}
		t = in.readLong();
		if(t>0){
			arrivedTime = new Date(t);
		} else {
			arrivedTime = null;
		}
		accepted = in.readByte() == 1;
		sessionType = in.readString();
		reservationId = in.readString();
	}
}
