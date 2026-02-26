package uk.co.epicuri.waiter.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by pharris on 12/09/14.
 */
public class EpicuriAdjustmentType implements Serializable {
	private static final String TAG_ID = "Id";
	private static final String TAG_NAME = "Name";
	private static final String TAG_SHORTCODE = "shortCode";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_VISIBLE = "visible";
	private static final String TAG_SHOW_ON_RECEIPT = "showOnReceipt";

	public static final int TYPE_PAYMENT = 0;
	public static final int TYPE_DISCOUNT = 1;
	public static final int PAYMENT_TYPE_MEWS = -1;

	private String id;
	private String name;
	private String shortCode;
	private int type;
	private boolean visible;
	private boolean showOnReceipt;

	public EpicuriAdjustmentType(JSONObject in) throws JSONException {
		id = in.getString(TAG_ID);
		name = in.getString(TAG_NAME);
		type = in.getInt(TAG_TYPE);
		if (in.has(TAG_SHORTCODE)) {
			shortCode = in.getString(TAG_SHORTCODE);
		} else {
			shortCode = name;
		}
		visible = in.has(TAG_VISIBLE) && in.getBoolean(TAG_VISIBLE);
		showOnReceipt = in.has(TAG_SHOW_ON_RECEIPT) && in.getBoolean(TAG_SHOW_ON_RECEIPT);
	}

	public String getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

    public boolean isShowOnReceipt() {
        return showOnReceipt;
    }

    public void setShowOnReceipt(boolean showOnReceipt) {
        this.showOnReceipt = showOnReceipt;
    }

    @Override
	public String toString() {
		return name;
	}

	private EpicuriAdjustmentType(){

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EpicuriAdjustmentType that = (EpicuriAdjustmentType) o;

		return id != null ? id.equals(that.id) : that.id == null;

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
