package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;

import org.joda.money.CurrencyUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import uk.co.epicuri.waiter.model.EpicuriCustomer.Address;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;

public class EpicuriRestaurant implements Parcelable {
	private static final String TAG_NAME = "Name";
	private static final String TAG_ADDRESS = "Address";
	private static final String TAG_TELEPHONE_1 = "Telephone1";
	private static final String TAG_TELEPHONE_2 = "Telephone2";
	private static final String TAG_EMAIL = "Email";
	private static final String TAG_WEBSITE = "Website";
	private static final String TAG_RECEIPT_FOOTER = "ReceiptFooter";
	private static final String TAG_VAT_NUMBER = "VATNumber";
	private static final String TAG_TAKEAWAY_MENUID = "TakeawayMenuId";
	private static final String TAG_TAKEAWAY_TYPES = "TakeawayOffered";
	private static final String TAG_RESTAURANT_DEFAULTS = "RestaurantDefaults";
	private static final String TAG_RECEIPT_IMAGE_URL = "ReceiptImageURL";
	private static final String TAG_TAKEAWAY_PRINTER_ID = "TakeawayPrinterId";
	private static final String TAG_BILLING_PRINTER_ID = "BillingPrinterId";
	private static final String TAG_ADJUSTMENT_TYPES = "AdjustmentTypes";
	private static final String TAG_CURRENCY = "Currency";
	private static final String TAG_TIMEZONE = "Timezone";
	private static final String TAG_MEWS_INTEGRATION = "MewsIntegration";
	private static final String TAG_RECEIPT_TYPE = "ReceiptType";
    private static final String TAG_PERMISSIONS = "permissions";
	private static final String TAG_PAYMENTSENSE = "PaymentSense";
	private static final String TAG_CASHDRAWERS = "cashDrawerConnectedPrinters";
	private static final String TAG_COURSE_AWAY_PRINTER_ID = "defaultCourseAwayPrinterId";

	public static final String DEFAULT_MAXTAKEAWAYVALUE = "MaxTakeawayValue";
	public static final String DEFAULT_MINTAKEAWAYVALUE = "MinTakeawayValue";
	public static final String DEFAULT_RESERVATIONMINIMUMTIME = "ReservationMinimumTime";
	public static final String DEFAULT_TAKEAWAYMINIMUMTIME = "TakeawayMinimumTime";
	public static final String DEFAULT_TAKEAWAYLOCKWINDOW = "TakeawayLockWindow";
	public static final String DEFAULT_REPRINT_BILL = "ReprintBillAtClose";
	public static final String DEFAULT_WALKIN_EXPIRATION_TIME = "WalkinExpirationTime";
	public static final String DEFAULT_VAT_NUMBER = "TaxReferenceLabel";
	public static final String DEFAULT_VAT_LABEL = "TaxLabel";
	public static final String DEFAULT_QO_SCREENSIZE_THRESHOLD = "QOScreenSizeThreshold";
	public static final String DEFAULT_QO_FONT_SCALE_UP = "QOFontScaleUp";
	public static final String DEFAULT_EMAILRECEIPTENABLED = "EmailReceiptsEnabled";
	public static final String DEFAULT_TIP_PERCENTAGE = "DefaultTipPercentage";
	public static final String DOUBLE_HEIGHT_ORDER_PRINTS = "DoubleHeightOrderPrints";
    public static final String DOUBLE_WIDTH_ORDER_PRINTS = "DoubleWidthOrderPrints";
    public static final String BILL_PRINT_FONT_SIZE = "BillPrintFontSize";
	public static final String CONDITIONAL_SERVICE_CHARGE_TEXT = "ConditionalServiceChargeText";
	public static final String FORCE_LOCATION_ON_QO = "ForceLocationOnQO";
	public static final String SHOW_TABLE_ON_QO = "ShowTableOnQO";
	public static final String SHOW_TAB_ON_QO = "ShowTabOnQO";
	public static final String SHOW_REFUND_ON_QO = "ShowRefundOnQO";
	public static final String APPLY_AUTO_TIP_TO_QO = "ApplyAutoTipToQO";
	public static final String ENABLE_STOCK_COUNTDOWN = "EnableStockCountdown";
	public static final String ENABLE_DEFER_SESSIONS = "EnableDeferredSessions";

	public static final int RECEIPT_TYPE_NORMAL = 0;
	public static final int RECEIPT_TYPE_HOTEL = 1;

	public static final String PAYMENT_SENSE = "PAYMENTSENSE";
	public static final String PAYMENT_SENSE_GRATUITY = "PAYMENTSENSE (GRATUITY)";

	private final String name;
	private final Address address;
	private final String phoneNumber;
	private final String receiptFooter;
	private final String vatCode;
	private final String takeawayMenuId;
	private final EnumSet<EpicuriSessionDetail.SessionType> takeawayTypes;
	private final JSONObject restaurantDefaults;
	private final String receiptImageURL;
	private final String takeawayPrinterId;
	private final String billingPrinterId;
	private final String website;
	private final String email;
	private final String currency;
	private final String timezone;
	private final boolean mewsEnabled;
	private final int receiptType;
	private final String paymentsenseHost;
	private final String paymentsenseKey;
	private final ArrayList<EpicuriAdjustmentType> adjustmentTypes = new ArrayList<EpicuriAdjustmentType>();
    private final JSONObject permissions;
    private final List<String> connectedCashDrawers;
    private final String defaultCourseAwayPrinterId;

	private final ArrayList<PaymentsenseTerminal> terminals = new ArrayList<>();

	public String getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public String getWebsite() {
		return website;
	}

	public CurrencyUnit getCurrency() {
		return CurrencyUnit.of(currency);
	}

	public TimeZone getTimezone() {
		return TimeZone.getTimeZone(timezone);
	}

	public String getVatLabel(){
		String vatLabel = getRestaurantDefault(DEFAULT_VAT_LABEL);
		if(null != vatLabel) return vatLabel;
		return "VAT";
	}
	public boolean isEmailReceiptsEnabled(){
		return restaurantDefaults!=null && restaurantDefaults.optBoolean(DEFAULT_EMAILRECEIPTENABLED);
	}

	public double getDefaultTipPercentage(){
		if(restaurantDefaults.has(DEFAULT_TIP_PERCENTAGE)) {
			try {
				return restaurantDefaults.getDouble(DEFAULT_TIP_PERCENTAGE);
			} catch (JSONException e) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public String getVatNumberLabel() {
		String vatNumberLabel = getRestaurantDefault(DEFAULT_VAT_NUMBER);
		if(null != vatNumberLabel) return vatNumberLabel;
		return "VAT Number";
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getReceiptFooter() {
		return receiptFooter;
	}

	public String getVatCode() {
		return vatCode;
	}
	
	public String getTakeawayMenuId() {
		return takeawayMenuId;
	}

	public String getReceiptImageURL() {
		return receiptImageURL;
	}

	public boolean isMewsEnabled() {
		return mewsEnabled;
	}

	public int getReceiptType() {
		return receiptType;
	}

	public String getPaymentsenseHost() {
		return paymentsenseHost;
	}

	public String getPaymentsenseKey() {
		return paymentsenseKey;
	}

	public float getQOScreenThresholdInches() {
		try {
			return Float.valueOf(getRestaurantDefault(DEFAULT_QO_SCREENSIZE_THRESHOLD, "10"));
		} catch(Exception ex) {
			return 10f;
		}
	}

	public float getQOFontUpscale() {
		try {
			return Float.valueOf(getRestaurantDefault(DEFAULT_QO_FONT_SCALE_UP, "1"));
		} catch(Exception ex) {
			return 1f;
		}
	}

	public String getRestaurantDefault(String key){
		return getRestaurantDefault(key, null);
	}

	public String getRestaurantDefault(String key, String fallback){
		if(null != restaurantDefaults && restaurantDefaults.has(key)){
			try {
				return restaurantDefaults.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return fallback;
	}

	public String getBillingPrinterId() {
		return billingPrinterId;
	}

	public String getTakeawayPrinterId() {
		return takeawayPrinterId;
	}

	public String getDefaultCourseAwayPrinterId() {
		return defaultCourseAwayPrinterId;
	}

	public boolean isDoubleHeight() {
	    if(restaurantDefaults.has(DOUBLE_HEIGHT_ORDER_PRINTS)) {
            try {
                return restaurantDefaults.getBoolean(DOUBLE_HEIGHT_ORDER_PRINTS);
            } catch (JSONException e) {
                return true;
            }
        } else {
	        return true;
        }
    }

    public boolean isDoubleWidth() {
        if(restaurantDefaults.has(DOUBLE_WIDTH_ORDER_PRINTS)) {
            try {
                return restaurantDefaults.getBoolean(DOUBLE_WIDTH_ORDER_PRINTS);
            } catch (JSONException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean isTableOnQOShown() {
		if(restaurantDefaults.has(SHOW_TABLE_ON_QO)) {
			try {
				return restaurantDefaults.getBoolean(SHOW_TABLE_ON_QO);
			} catch (JSONException e) {
				return true;
			}
		} else {
			return true;
		}
	}

	public boolean isTabOnQOShown() {
		if(restaurantDefaults.has(SHOW_TAB_ON_QO)) {
			try {
				return restaurantDefaults.getBoolean(SHOW_TAB_ON_QO);
			} catch (JSONException e) {
				return true;
			}
		} else {
			return true;
		}
	}

	public boolean isRefundOnQOShown() {
		if(restaurantDefaults.has(SHOW_REFUND_ON_QO)) {
			try {
				return restaurantDefaults.getBoolean(SHOW_REFUND_ON_QO);
			} catch (JSONException e) {
				return true;
			}
		} else {
			return true;
		}
	}

	public boolean isApplyAutoTipToQO() {
		if(restaurantDefaults.has(APPLY_AUTO_TIP_TO_QO)) {
			try {
				return restaurantDefaults.getBoolean(APPLY_AUTO_TIP_TO_QO);
			} catch (JSONException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isForceLocationSelectionOnQO() {
		if(restaurantDefaults.has(FORCE_LOCATION_ON_QO)) {
			try {
				return restaurantDefaults.getBoolean(FORCE_LOCATION_ON_QO);
			} catch (JSONException e) {
				return true;
			}
		} else {
			return true;
		}
	}

    public boolean getPermission(WaiterAppFeature waiterAppFeature) {
	    return getPermission(waiterAppFeature, true);
    }

	public boolean getPermission(WaiterAppFeature waiterAppFeature, boolean defaultValue) {
		if(permissions == null) {
			return defaultValue;
		}

		if(!permissions.has(waiterAppFeature.name())) {
			return defaultValue;
		}

		try {
			return permissions.getBoolean(waiterAppFeature.name());
		} catch (JSONException e) {
			return defaultValue;
		}
	}

    public double getBillPrintFontSize() {
	    if(restaurantDefaults.has(BILL_PRINT_FONT_SIZE)) {
            try {
                return restaurantDefaults.getDouble(BILL_PRINT_FONT_SIZE);
            } catch (JSONException e) {
                return 30;
            }
        } else {
	        return 30;
        }
    }

    public String getServiceChargeText() {
		if(restaurantDefaults.has(CONDITIONAL_SERVICE_CHARGE_TEXT)) {
			try {
				return restaurantDefaults.getString(CONDITIONAL_SERVICE_CHARGE_TEXT);
			} catch (JSONException e) {
				return "";
			}
		} else {
			return "";
		}
	}

	public boolean stockCountdownEnabled() {
		if(restaurantDefaults.has(ENABLE_STOCK_COUNTDOWN)) {
			try {
				return restaurantDefaults.getBoolean(ENABLE_STOCK_COUNTDOWN);
			} catch (JSONException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean deferredSessionsEnabled() {
		if(restaurantDefaults.has(ENABLE_STOCK_COUNTDOWN)) {
			try {
				return restaurantDefaults.getBoolean(ENABLE_DEFER_SESSIONS);
			} catch (JSONException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public ArrayList<PaymentsenseTerminal> getTerminals() {
		return terminals;
	}

	public void setTerminals(List<PaymentsenseTerminal> terminals) {
		this.terminals.clear();
		this.terminals.addAll(terminals);
	}

	public List<String> getConnectedCashDrawers() {
		return connectedCashDrawers;
	}
	public EnumSet<EpicuriSessionDetail.SessionType> getTakeawayTypes() {
		return takeawayTypes;
	}

	public EpicuriRestaurant(JSONObject json) throws JSONException {
		name = json.getString(TAG_NAME);
		address = new Address(json.getJSONObject(TAG_ADDRESS));
		email = json.isNull(TAG_EMAIL) ? null : json.getString(TAG_EMAIL);
		website = json.isNull(TAG_WEBSITE) ? null : json.getString(TAG_WEBSITE);
		phoneNumber = json.getString(TAG_TELEPHONE_1);
		vatCode = json.isNull(TAG_VAT_NUMBER) ? null : json.getString(TAG_VAT_NUMBER);
		receiptFooter = json.isNull(TAG_RECEIPT_FOOTER) ? null : json.getString(TAG_RECEIPT_FOOTER);
		takeawayMenuId = json.getString(TAG_TAKEAWAY_MENUID);
		receiptImageURL = json.isNull(TAG_RECEIPT_IMAGE_URL) ? null : json.getString(TAG_RECEIPT_IMAGE_URL);
		takeawayPrinterId = json.isNull(TAG_TAKEAWAY_PRINTER_ID) ? "-1" : json.getString(TAG_TAKEAWAY_PRINTER_ID);
		billingPrinterId = json.isNull(TAG_BILLING_PRINTER_ID) ? "-1" : json.getString(TAG_BILLING_PRINTER_ID);
		currency = json.isNull(TAG_CURRENCY) ? "GBP" : json.getString(TAG_CURRENCY);
		timezone = json.isNull(TAG_TIMEZONE) ? "Europe/London" : json.getString(TAG_TIMEZONE);
		mewsEnabled = !json.isNull(TAG_MEWS_INTEGRATION) && json.getBoolean(TAG_MEWS_INTEGRATION);
		receiptType = json.isNull(TAG_RECEIPT_TYPE) ? RECEIPT_TYPE_NORMAL : json.getInt(TAG_RECEIPT_TYPE);

		int takeawayEnum = json.getInt(TAG_TAKEAWAY_TYPES);
		switch(takeawayEnum){
		case 0:
			takeawayTypes = EnumSet.noneOf(SessionType.class);
			break;
		case 1:
			takeawayTypes = EnumSet.of(SessionType.DELIVERY);
			break;
		case 2:
			takeawayTypes = EnumSet.of(SessionType.COLLECTION);
			break;
		case 3:
			takeawayTypes = EnumSet.of(SessionType.DELIVERY, SessionType.COLLECTION);
			break;
		default:
			throw new IllegalArgumentException("Enum value not recognised");
		}
		asJson = json.toString();

		JSONObject paymentsense = json.has(TAG_PAYMENTSENSE) ? json.getJSONObject
				(TAG_PAYMENTSENSE) : null;
		paymentsenseHost = (paymentsense == null) ? "" : paymentsense.getString("host");
		paymentsenseKey = (paymentsense == null) ? "" : paymentsense.getString("key");

		if(json.has(TAG_ADJUSTMENT_TYPES)){
			JSONArray a = json.getJSONArray(TAG_ADJUSTMENT_TYPES);
			for(int i=0; i<a.length(); i++){
				JSONObject t = a.getJSONObject(i);
				adjustmentTypes.add(new EpicuriAdjustmentType(t));
			}
		}

		restaurantDefaults = json.getJSONObject(TAG_RESTAURANT_DEFAULTS);
		if(json.has(TAG_PERMISSIONS)) {
            permissions = json.getJSONObject(TAG_PERMISSIONS);
        } else {
		    permissions = new JSONObject();
        }

		connectedCashDrawers = new ArrayList<>();
        if(json.has(TAG_CASHDRAWERS)) {
			JSONArray array = json.getJSONArray(TAG_CASHDRAWERS);
			for(int i = 0; i < array.length(); i++) {
				connectedCashDrawers.add(array.getString(i));
			}
		}

		if(json.has(TAG_COURSE_AWAY_PRINTER_ID)) {
        	defaultCourseAwayPrinterId = json.getString(TAG_COURSE_AWAY_PRINTER_ID);
		} else {
        	defaultCourseAwayPrinterId = null;
		}
	}
	private final String asJson;

	public String toJson(){
		return asJson;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(asJson);
		dest.writeString(name);
		dest.writeParcelable(address, 0);
		dest.writeString(phoneNumber);
		dest.writeString(email);
		dest.writeString(website);
		dest.writeInt(mewsEnabled ? 1 : 0);
		dest.writeInt(receiptType);
		dest.writeString(vatCode);
		dest.writeString(currency);
		dest.writeString(timezone);
		dest.writeString(receiptFooter);
		dest.writeString(takeawayMenuId);
		dest.writeString(takeawayPrinterId);
		dest.writeString(billingPrinterId);
		dest.writeString(receiptImageURL);
		dest.writeSerializable(takeawayTypes);
		dest.writeString(restaurantDefaults.toString());
		dest.writeString(paymentsenseHost);
		dest.writeString(paymentsenseKey);
		dest.writeString(permissions.toString());
		dest.writeStringList(connectedCashDrawers);
		dest.writeString(defaultCourseAwayPrinterId);
	}

	private EpicuriRestaurant(Parcel in){
		asJson = in.readString();
		name = in.readString();
		address = in.readParcelable(Address.class.getClassLoader());
		phoneNumber = in.readString();
		email = in.readString();
		website = in.readString();
		mewsEnabled = 1 == in.readInt();
		receiptType = in.readInt();
		vatCode = in.readString();
		currency = in.readString();
		timezone = in.readString();
		receiptFooter = in.readString();
		takeawayMenuId = in.readString();
		takeawayPrinterId = in.readString();
		billingPrinterId = in.readString();
		receiptImageURL = in.readString();
		takeawayTypes = (EnumSet<SessionType>)in.readSerializable();

		JSONObject rd;
		try {
			rd = new JSONObject(in.readString());
		} catch (JSONException e) {
			rd = null;
			e.printStackTrace();
		}
		restaurantDefaults = rd;
        paymentsenseHost = in.readString();
        paymentsenseKey = in.readString();

        JSONObject perms;
        try {
            perms = new JSONObject(in.readString());
        } catch (JSONException e) {
            perms = null;
            e.printStackTrace();
        }
        permissions = perms;
        connectedCashDrawers = new ArrayList<>();
        in.readStringList(connectedCashDrawers);
        defaultCourseAwayPrinterId = in.readString();
	}

	public static final Parcelable.Creator<EpicuriRestaurant> CREATOR = new Parcelable.Creator<EpicuriRestaurant>() {

		@Override
		public EpicuriRestaurant[] newArray(int size) {
			return new EpicuriRestaurant[size];
		}

		@Override
		public EpicuriRestaurant createFromParcel(Parcel source) {
			return new EpicuriRestaurant(source);
		}
	};
	public ArrayList<EpicuriAdjustmentType> getPaymentTypes(){
		ArrayList<EpicuriAdjustmentType> result = new ArrayList<>();
		for(EpicuriAdjustmentType type: adjustmentTypes){
			if(type.getType() == EpicuriAdjustmentType.TYPE_PAYMENT){
				if((mewsEnabled && type.getName().toLowerCase().contains("mews"))
						|| (type.getId() != null && !type.getId().equals("-1"))){
					result.add(type);
				}
			}
		}
		return result;
	}
	public ArrayList<EpicuriAdjustmentType> getDiscountTypes(){
		ArrayList<EpicuriAdjustmentType> result = new ArrayList<>();
		for(EpicuriAdjustmentType type: adjustmentTypes){
			if(type.getType() == EpicuriAdjustmentType.TYPE_DISCOUNT) result.add(type);
		}
		return result;
	}

	public Map<String,EpicuriAdjustmentType> getAdjustmentTypesLookup() {
		Map<String,EpicuriAdjustmentType> result = new LinkedHashMap<>(adjustmentTypes.size());
		for(EpicuriAdjustmentType t: adjustmentTypes){
			result.put(t.getId(), t);
		}
		return result;
	}

	public boolean hasPaymentSenseAdjustmentType() {
		return getPaymentSensePaymentType() != null;
	}

	public EpicuriAdjustmentType getPaymentSensePaymentType() {
		return getPaymentType(PAYMENT_SENSE);
	}

	public EpicuriAdjustmentType getPaymentSenseGratuityType() {
		return getPaymentType(PAYMENT_SENSE_GRATUITY);
	}

	public EpicuriAdjustmentType getPaymentType(String name) {
		for(EpicuriAdjustmentType type : adjustmentTypes) {
			if(type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}
}
