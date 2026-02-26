package uk.co.epicuri.waiter.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by pharris on 23/09/14.
 */
public class EpicuriCashUp implements Parcelable {
	static final String TAG_ID = "Id";
	static final String TAG_START_TIME = "StartTime";
	static final String TAG_END_TIME = "EndTime";
	static final String TAG_WRAP_UP = "WrapUp";
	static final String TAG_REPORT = "Report";
	static final String TAG_PAYMENT_REPORT = "PaymentReport";
	static final String TAG_ADJUSTMENT_REPORT = "AdjustmentReport";
    static final String TAG_ITEM_ADJUSTMENT_REPORT = "ItemAdjustmentLossReport";
	static final String TAG_REFUND_REPORT = "RefundReport";
	static final String TAG_REFUND_PAYMENTS_REPORT = "RefundPaymentReport";


	public static final String[] KEYS = new String[]{
			"SeatedSessionsCount",
			"TakeawaySessionsCount",
			"CoversCount",
			"VoidSeatedSessionValue",
			"VoidSeatedSessionCount",
			"VoidTakeawaySessionValue",
			"VoidTakeawaySessionCount",
			"FoodValue",
			"FoodVAT",
			"FoodCount",
			"DrinkValue",
			"DrinkVAT",
			"DrinkCount",
			"OtherValue",
			"OtherVAT",
			"OtherCount",
			"GrossValue",
			"VATValue",
			"NetValue",
			"Tips"
	};

	public static final String[] LABELS = new String[]{
			"Seated Sessions Count",
			"Takeaway Sessions Count",
			"Covers Count",
			"Void Seated Session Value",
			"Void Seated Session Count",
			"Void Takeaway Session Value",
			"Void Takeaway Session Count",
			"Food Value",
			"Food VAT",
			"Food Count",
			"Drink Value",
			"Drink VAT",
			"Drink Count",
			"Other Value",
			"Other VAT",
			"Other Count",
			"Gross Value",
			"VAT Value",
			"Net Value",
			"Overpayment/Tips"
	};

	private String id;
	private Date startTime;
	private Date endTime;
	private boolean wrapUp;
	private Map<String, Double> report;
	private Map<String, Double> paymentReport;
	private Map<String, Double> adjustmentReport;
	private Map<String, Double> itemAdjustmentLossReport;
	private Map<String, Double> refundReport;
	private Map<String, Double> refundPaymentReport;

	public EpicuriCashUp(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getString(TAG_ID);
		startTime = new Date(1000L * jsonObject.getInt(TAG_START_TIME));
		endTime = new Date(1000L * jsonObject.getInt(TAG_END_TIME));
		wrapUp = jsonObject.getBoolean(TAG_WRAP_UP);
		report = new HashMap<>();
		paymentReport = new HashMap<>();
		adjustmentReport = new HashMap<>();
        itemAdjustmentLossReport = new HashMap<>();
        refundReport = new HashMap<>();
        refundPaymentReport = new HashMap<>();

		JSONObject reportJson = jsonObject.getJSONObject(TAG_REPORT);
		Iterator<String> keyIterator = reportJson.keys();

		while(keyIterator.hasNext()){
			String key = keyIterator.next();
			report.put(key, reportJson.getDouble(key));
		}

		if(jsonObject.has(TAG_PAYMENT_REPORT) && !jsonObject.isNull(TAG_PAYMENT_REPORT)) {
			reportJson = jsonObject.getJSONObject(TAG_PAYMENT_REPORT);
			keyIterator = reportJson.keys();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				paymentReport.put(key, reportJson.getDouble(key));
			}
		}
		if(jsonObject.has(TAG_ADJUSTMENT_REPORT) && !jsonObject.isNull(TAG_ADJUSTMENT_REPORT)) {
			reportJson = jsonObject.getJSONObject(TAG_ADJUSTMENT_REPORT);
			keyIterator = reportJson.keys();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				adjustmentReport.put(key, reportJson.getDouble(key));
			}
		}
        if(jsonObject.has(TAG_ITEM_ADJUSTMENT_REPORT) && !jsonObject.isNull(TAG_ITEM_ADJUSTMENT_REPORT)) {
            reportJson = jsonObject.getJSONObject(TAG_ITEM_ADJUSTMENT_REPORT);
            keyIterator = reportJson.keys();

            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                itemAdjustmentLossReport.put(key, reportJson.getDouble(key));
            }
        }
		if(jsonObject.has(TAG_REFUND_REPORT) && !jsonObject.isNull(TAG_REFUND_REPORT)) {
			reportJson = jsonObject.getJSONObject(TAG_REFUND_REPORT);
			keyIterator = reportJson.keys();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				refundReport.put(key, reportJson.getDouble(key));
			}
		}
		if(jsonObject.has(TAG_REFUND_PAYMENTS_REPORT) && !jsonObject.isNull(TAG_REFUND_PAYMENTS_REPORT)) {
			reportJson = jsonObject.getJSONObject(TAG_REFUND_PAYMENTS_REPORT);
			keyIterator = reportJson.keys();

			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				refundPaymentReport.put(key, reportJson.getDouble(key));
			}
		}
	}

	public String getId() {
		return id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String generatePrintString(Context context){
		EpicuriRestaurant r = LocalSettings.getInstance(context).getCachedRestaurant();
		String vatLabel = r.getVatLabel();

		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
		StringBuilder sb = new StringBuilder(String.format("Cash Up - %s %s\n\n", dateFormat.format(endTime), timeFormat.format(endTime)));
		for(int i=0; i<KEYS.length; i++){
			final String s = KEYS[i];
			sb.append(LABELS[i].replace("VAT", vatLabel)).append(": ");
			if(s.endsWith("Count")) sb.append(String.format(Locale.UK, "%,.0f\n", report.get(s)));
			else sb.append(String.format(Locale.UK, "%,.02f\n", report.get(s)));
		}
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeLong(startTime != null ? startTime.getTime() : -1);
		dest.writeLong(endTime != null ? endTime.getTime() : -1);
		dest.writeByte(wrapUp ? (byte) 1 : (byte) 0);
		dest.writeMap(this.report);
		dest.writeMap(this.paymentReport);
		dest.writeMap(this.adjustmentReport);
        dest.writeMap(this.itemAdjustmentLossReport);
        dest.writeMap(this.refundReport);
		dest.writeMap(this.refundPaymentReport);
	}

	private EpicuriCashUp(Parcel in) {
		this.id = in.readString();
		long tmpStartTime = in.readLong();
		this.startTime = tmpStartTime == -1 ? null : new Date(tmpStartTime);
		long tmpEndTime = in.readLong();
		this.endTime = tmpEndTime == -1 ? null : new Date(tmpEndTime);
		this.wrapUp = in.readByte() != 0;
		this.report = new HashMap<>();
		in.readMap(report, Double.class.getClassLoader());
		this.paymentReport= new HashMap<>();
		in.readMap(paymentReport, Double.class.getClassLoader());
		this.adjustmentReport= new HashMap<>();
		in.readMap(adjustmentReport, Double.class.getClassLoader());
        this.itemAdjustmentLossReport = new HashMap<>();
        in.readMap(itemAdjustmentLossReport, Double.class.getClassLoader());
        this.refundReport = new HashMap<>();
        in.readMap(refundReport, Double.class.getClassLoader());
		this.refundPaymentReport = new HashMap<>();
		in.readMap(refundPaymentReport, Double.class.getClassLoader());
	}

	public static final Parcelable.Creator<EpicuriCashUp> CREATOR = new Parcelable.Creator<EpicuriCashUp>() {
		public EpicuriCashUp createFromParcel(Parcel source) {
			return new EpicuriCashUp(source);
		}

		public EpicuriCashUp[] newArray(int size) {
			return new EpicuriCashUp[size];
		}
	};

	public Map<String, Double> getReport() {
		return report;
	}

	public boolean isCashup() {
		return !wrapUp;
	}

	public Map<String, Double> getPaymentReport() {
		return paymentReport;
	}

	public Map<String, Double> getAdjustmentReport() {
		return adjustmentReport;
	}

    public Map<String, Double> getItemAdjustmentLossReport() {
        return itemAdjustmentLossReport;
    }

	public Map<String, Double> getRefundReport() {
		return refundReport;
	}

	public Map<String, Double> getRefundPaymentReport() {
		return refundPaymentReport;
	}
}
