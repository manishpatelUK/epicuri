package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import kotlin.NotImplementedError;

public class EpicuriAdjustment implements Parcelable, Comparable, Serializable {
	public static final String TAG_ADJUSTMENT_ID = "Id";
	public static final String TAG_ADJUSTMENT_VALUE = "Value";
	public static final String TAG_ADJUSTMENT_TYPE = "TypeId";
	public static final String TAG_ADJUSTMENT_TYPE_OBJECT = "type";
	public static final String TAG_ADJUSTMENT_NUMERICAL_TYPE = "NumericalTypeId";
	public static final String TAG_ADJUSTMENT_REFERENCE = "reference";
	public static final String TAG_ADJUSTMENT_TIMESTAMP = "Created";
	public static final String TAG_EXTRAS = "extras";
	public static final String TAG_STRIPE_CHARGE = "StripeCharge";
	public static final String TAG_DEFER_INFO = "DEFERMENT_NOTE_CUSTOMER_INFO";

	//public static final String NUMERICAL_TYPE_MONEY = "0";
	//public static final String NUMERICAL_TYPE_PERCENTAGE  = "1";

	public static final int NUMERICAL_TYPE_MONEY = 0;
	public static final int NUMERICAL_TYPE_PERCENTAGE  = 1;

	private String id = "-1";
	private Money amount = null;
	private double percentage;
	private String typeId;
	//private int typeId;
	private long timestamp;
	private String reference;
	private EpicuriAdjustmentType type;
	private StripeCharge stripeCharge;
	private String defermentInfo;

//	private Date delivered;
//	private String note;
//	private int sessionId;

	public EpicuriAdjustment(JSONObject adjustmentJson) throws JSONException {
		id = adjustmentJson.getString(TAG_ADJUSTMENT_ID);
		if(adjustmentJson.getInt(TAG_ADJUSTMENT_NUMERICAL_TYPE) == NUMERICAL_TYPE_MONEY){
		//if(adjustmentJson.getString(TAG_ADJUSTMENT_NUMERICAL_TYPE).equals(NUMERICAL_TYPE_MONEY)){
			double val = adjustmentJson.getDouble(TAG_ADJUSTMENT_VALUE);
			val = Math.round(val * 100) / 100.0;
			amount = Money.of(LocalSettings.getCurrencyUnit(), val);
		}
		else {
			percentage = adjustmentJson.getDouble(TAG_ADJUSTMENT_VALUE);
		}

		typeId = adjustmentJson.getString(TAG_ADJUSTMENT_TYPE);
		timestamp = adjustmentJson.getInt(TAG_ADJUSTMENT_TIMESTAMP);
		if (adjustmentJson.has(TAG_ADJUSTMENT_REFERENCE)) reference = adjustmentJson.getString
				(TAG_ADJUSTMENT_REFERENCE);
		if(adjustmentJson.has(TAG_ADJUSTMENT_TYPE_OBJECT)) {
			JSONObject jsonObject = adjustmentJson.getJSONObject(TAG_ADJUSTMENT_TYPE_OBJECT);
			type = new EpicuriAdjustmentType(jsonObject);
		}

		if(adjustmentJson.has(TAG_EXTRAS)){
		    JSONObject extras = adjustmentJson.getJSONObject(TAG_EXTRAS);
		    if(extras.has(TAG_STRIPE_CHARGE)){
		        JSONObject stripe = extras.getJSONObject(TAG_STRIPE_CHARGE);
		        if (stripe.has(TAG_STRIPE_CHARGE)){
		            stripeCharge = new StripeCharge(stripe.getJSONObject(TAG_STRIPE_CHARGE));
                }
            }
		    if(extras.has(TAG_DEFER_INFO)) {
		    	defermentInfo = extras.getString(TAG_DEFER_INFO);
			}
        } else {
		    stripeCharge = null;
        }
	}

//	public int getId() {
//		return id;
//	}

	public String getId() {
		return id;
	}

	public Money getAmount() {
		return amount;
	}

	public String getReference() {
		return reference;
	}

	public String getTypeId() {
		return typeId;
	}

	public double getPercentage() {
		return percentage;
	}

	public EpicuriAdjustmentType getType() {
		return type;
	}

    public StripeCharge getStripeCharge() {
        return stripeCharge;
    }

	public String getDefermentInfo() {
		return defermentInfo;
	}

	@Override
	public int compareTo(Object another) {
		if(!(another instanceof EpicuriAdjustment)) throw new ClassCastException("A EpicuriAdjustment objectExpected");
		return (int)(((EpicuriAdjustment)another).timestamp - timestamp);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeInt(this.id);
		dest.writeString(this.id);
		dest.writeSerializable(this.amount);
		dest.writeDouble(this.percentage);
		//dest.writeInt(this.typeId);
		dest.writeString(this.typeId);
		dest.writeLong(this.timestamp);
		dest.writeString(this.reference);
	}

	private EpicuriAdjustment(Parcel in) {
		//this.id = in.readInt();
		this.id = in.readString();
		this.amount = (Money) in.readSerializable();
		this.percentage = in.readDouble();
		//this.typeId = in.readInt();
		this.typeId = in.readString();
		this.timestamp = in.readLong();
		this.reference = in.readString();
	}

	public static final Creator<EpicuriAdjustment> CREATOR = new Creator<EpicuriAdjustment>() {
		public EpicuriAdjustment createFromParcel(Parcel source) {
			return new EpicuriAdjustment(source);
		}

		public EpicuriAdjustment[] newArray(int size) {
			return new EpicuriAdjustment[size];
		}
	};

    public JSONObject toJSON() throws JSONException {
        JSONObject response = new JSONObject();
        //todo Implement converting to json object
        return response;
    }

    public static class StripeCharge implements Parcelable{
        static final String LAST_4_DIGITS = "last4Digits";
        static final String EXP_MONTH = "expMonth";
        static final String EXP_YEAR = "expYear";
        String last4digits;
        int expMonth;
        int expYear;

        StripeCharge(JSONObject jsonObject) throws JSONException{
            if(jsonObject.has(LAST_4_DIGITS)){
                last4digits = jsonObject.getString(LAST_4_DIGITS);
            }

            if (jsonObject.has(EXP_MONTH) && jsonObject.has(EXP_YEAR)){
                expMonth = jsonObject.getInt(EXP_MONTH);
                expYear = jsonObject.getInt(EXP_YEAR);
            }
        }

        protected StripeCharge(Parcel in) {
            last4digits = in.readString();
            expMonth = in.readInt();
            expYear = in.readInt();
        }

        public static final Creator<StripeCharge> CREATOR = new Creator<StripeCharge>() {
            @Override
            public StripeCharge createFromParcel(Parcel in) {
                return new StripeCharge(in);
            }

            @Override
            public StripeCharge[] newArray(int size) {
                return new StripeCharge[size];
            }
        };

        public String getLast4digits() {
            return last4digits;
        }

        public void setLast4digits(String last4digits) {
            this.last4digits = last4digits;
        }

        public int getExpMonth() {
            return expMonth;
        }

        public void setExpMonth(int expMonth) {
            this.expMonth = expMonth;
        }

        public int getExpYear() {
            return expYear;
        }

        public void setExpYear(int expYear) {
            this.expYear = expYear;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(last4digits);
            dest.writeInt(expMonth);
            dest.writeInt(expYear);
        }
    }
}
