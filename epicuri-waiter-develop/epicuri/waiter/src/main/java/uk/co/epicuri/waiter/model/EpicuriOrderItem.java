package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import uk.co.epicuri.waiter.interfaces.IOrderItem;
import uk.co.epicuri.waiter.model.EpicuriMenu.Course;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;

public class EpicuriOrderItem implements Parcelable, IOrderItem, Serializable {
	public static final String TAG_ITEM_DINER_ID = "dinerId";
	public static final String TAG_ITEM_ID = "Id";
	public static final String TAG_ITEM_MENUITEM = "MenuItem";
	public static final String TAG_ITEM_QUANTITY = "quantity";
	public static final String TAG_ITEM_COURSE = "Course";
	public static final String TAG_ITEM_DISCOUNT_REASON = "DiscountReason";
	public static final String TAG_ITEM_ADJUSTMENT = "adjustment";
	public static final String TAG_ITEM_PRICE = "PriceOverride";
	public static final String TAG_ITEM_COMPLETED = "Completed";
	public static final String TAG_ITEM_NOTE = "Note";
	public static final String TAG_ITEM_MODIFIERS = "Modifiers";
	public static final String TAG_ITEM_DELIVERY_LOCATION = "deliveryLocation";
	public static final String DEFAULT_ID_VALUE = "-1";

	private String id = DEFAULT_ID_VALUE;
	private Money price = null;
	private int quantity = 1;
	private Date delivered;
	private String note;
	private String discountReason;
	private EpicuriMenu.Course course;
	private EpicuriMenu.Course defaultCourse;
	private String sessionId;
	private final EpicuriMenu.Item item;
	private String dinerId;
	private final ArrayList<EpicuriMenu.ModifierValue> chosenModifiers;
	private EpicuriAdjustment adjustment;
	private String deliveryLocation;

	public void setASAP(EpicuriMenu.Course asapCourse){
	    if (defaultCourse == null) {
            defaultCourse = course;
        }
	    course = asapCourse;
    }

    public void resetToDefault(){
	    if(defaultCourse != null) {
            course = defaultCourse;
        }
        defaultCourse = null;
    }

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public EpicuriMenu.Course getCourse() {
		return course;
	}

	public void setCourse(EpicuriMenu.Course course) {
		this.course = course;
		this.defaultCourse = course;
	}

	public String getDinerId() {
		return dinerId;
	}

	public void setDinerId(String dinerId) {
		this.dinerId = dinerId;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDelivered() {
		return delivered;
	}

	public String getSessionId() {
		return sessionId;
	}

	public EpicuriMenu.Item getItem() {
		return item;
	}
	
	public ArrayList<EpicuriMenu.ModifierValue> getChosenModifiers() {
		return chosenModifiers;
	}
	
	public void setPrice(Money price) {
		this.price = price;
	}
	
	public boolean isPriceOverridden(){
		return null != price;
	}
	public Money getPriceOverride(){
		return price;
	}

	public String getDeliveryLocation() {
		return deliveryLocation;
	}

	public String getDiscountReason() {
		return discountReason;
	}

	public Money getCalculatedPriceIncludingQuantity() {
		return getCalculatedPrice().multipliedBy(quantity);
	}

	/** item price, excluding quantity */
	public Money getCalculatedPrice() {
		Money calculatedPrice = price == null ? item.getPrice() : price;
		for(EpicuriMenu.ModifierValue modifier: chosenModifiers){
			calculatedPrice = calculatedPrice.plus(modifier.getPrice());
		}

		return calculatedPrice;
	}
	
	public EpicuriOrderItem(EpicuriMenu.Item item, Course course){
		this.item = item;
		this.course = course;
		this.chosenModifiers = new ArrayList<>();
		this.note = "";
	}
	
	public EpicuriOrderItem(JSONObject orderJson) throws JSONException {
		
		JSONObject menuItemJson = orderJson.getJSONObject(TAG_ITEM_MENUITEM);
		item = new EpicuriMenu.Item(menuItemJson);
		
		chosenModifiers = new ArrayList<EpicuriMenu.ModifierValue>();
		if(orderJson.has(TAG_ITEM_MODIFIERS)){
			JSONArray modifiersArray = orderJson.getJSONArray(TAG_ITEM_MODIFIERS);
			for(int i=0; i<modifiersArray.length(); i++){
				chosenModifiers.add(new EpicuriMenu.ModifierValue(modifiersArray.getJSONObject(i)));
			}
		}
		
		// price defaults to -1.  Override with non-zero value, otherwise will take value of component parts
		
		id = orderJson.getString(TAG_ITEM_ID);
		if(orderJson.has(TAG_ITEM_DINER_ID)){
			dinerId = orderJson.getString(TAG_ITEM_DINER_ID);
		}
		if(orderJson.has(TAG_ITEM_DISCOUNT_REASON) && !orderJson.isNull(TAG_ITEM_DISCOUNT_REASON)){
			discountReason = orderJson.getString(TAG_ITEM_DISCOUNT_REASON);
		} else {
			discountReason = null;
		}
		
		if(orderJson.has(TAG_ITEM_PRICE) && !orderJson.isNull(TAG_ITEM_PRICE)){
			price = Money.of(LocalSettings.getCurrencyUnit(), orderJson.getDouble(TAG_ITEM_PRICE));
		}
		
		if (orderJson.has(TAG_ITEM_COURSE)) {
			course = new EpicuriMenu.Course(orderJson.getJSONObject(TAG_ITEM_COURSE));
		}
		if (orderJson.has(TAG_ITEM_QUANTITY)) {
			quantity = orderJson.getInt(TAG_ITEM_QUANTITY);
		}
		if (orderJson.has(TAG_ITEM_COMPLETED)) {
			delivered = new Date(1000L * orderJson.getInt(TAG_ITEM_COMPLETED));
		}
		if (orderJson.has(TAG_ITEM_NOTE)) {
			note = orderJson.getString(TAG_ITEM_NOTE);
		}
		if(orderJson.has(TAG_ITEM_ADJUSTMENT)) {
			adjustment = new EpicuriAdjustment(orderJson.getJSONObject(TAG_ITEM_ADJUSTMENT));
		}
		if(orderJson.has(TAG_ITEM_DELIVERY_LOCATION)) {
			deliveryLocation = orderJson.getString(TAG_ITEM_DELIVERY_LOCATION);
		}
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject response = new JSONObject();
		response.put(TAG_ITEM_MENUITEM, item.toJSON());
		response.put(TAG_ITEM_ID, id);
		response.put(TAG_ITEM_DINER_ID, dinerId);
		response.put(TAG_ITEM_COURSE, course.toJSON());
		response.put(TAG_ITEM_QUANTITY, quantity);

		if(null != delivered){
			response.put(TAG_ITEM_COMPLETED, (int)(delivered.getTime() / 1000));
		}

		if(null != note){
			response.put(TAG_ITEM_NOTE, note);
		}

		if(isPriceOverridden()){
			response.put(TAG_ITEM_PRICE, price.getAmount().toPlainString());
		}

		if (adjustment != null){
		    response.put(TAG_ITEM_ADJUSTMENT, adjustment.toJSON());
        }
		
		JSONArray modifiers = new JSONArray();
		for(ModifierValue modifier: chosenModifiers){
			modifiers.put(modifier.toJSON());
		}
		response.put(TAG_ITEM_MODIFIERS, modifiers);
		return response;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("{OrderItem ").append(id == null ? "" : id)
		.append(item.toString())
		.append("Price: "  + price)
		.toString();
	}

	@Override
	public boolean equals(Object o) {
		if(null == o || !(o instanceof EpicuriOrderItem)){
			return false;
		}
		return id.equals(((EpicuriOrderItem)o).id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();//(int)(id ^ (id >>> 32));
	}

	public int pseudoHashCode() {
		int result = price != null ? price.hashCode() : 0;
		result = 31 * result + quantity;
		result = 31 * result + (delivered != null ? delivered.hashCode() : 0);
		result = 31 * result + (note != null ? note.hashCode() : 0);
		result = 31 * result + (discountReason != null ? discountReason.hashCode() : 0);
		result = 31 * result + (course != null ? course.hashCode() : 0);
		result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
		result = 31 * result + (item != null ? item.hashCode() : 0);
		result = 31 * result + (dinerId != null ? dinerId.hashCode() : 0);
		result = 31 * result + (chosenModifiers != null ? chosenModifiers.hashCode() : 0);
		return result;
	}

	public boolean isSameOrder(EpicuriOrderItem o, boolean finalBill){
		if(null == o) throw new NullPointerException("Must pass an object");
		if(!o.item.equals(item)) return false; // different item
		if(o.chosenModifiers.size() != chosenModifiers.size()) return false; // different number of modifiers
		for(ModifierValue v: o.chosenModifiers){
			if(!chosenModifiers.contains(v)) return false; // modifier mismatch
		}
		if(!finalBill) {
			// on final bill these aren't displayed
			if ((null == o.note && null != note) || (null != o.note && !o.note.equals(note))) {
				return false; // notes differ
			}
			if ((null == o.course && null != course) || (null != o.course && !o.course.equals(course))) {
				return false; // different course
			}
			if(o.dinerId != null && !o.dinerId.equals(dinerId)){
				return false; // different diner
			}
		}
		return o.price == price;
	}

	public boolean isSameOrder(EpicuriOrderItem o){
		return isSameOrder(o, false);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<EpicuriOrderItem> CREATOR = new Creator<EpicuriOrderItem>() {
		
		@Override
		public EpicuriOrderItem[] newArray(int size) {
			return new EpicuriOrderItem[size];
		}
		
		@Override
		public EpicuriOrderItem createFromParcel(Parcel source) {
			return new EpicuriOrderItem(source);
		}
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(dinerId);
		if(null != price){
			dest.writeSerializable(price);
		} else {
			dest.writeSerializable(null);
		}
		dest.writeInt(quantity);
		if(null == delivered){
			dest.writeLong(0);
		} else {
			dest.writeLong(delivered.getTime());
		}
		dest.writeParcelableArray(chosenModifiers.toArray(new ModifierValue[chosenModifiers.size()]), 0);
		dest.writeString(note);
		dest.writeString(discountReason);
		dest.writeParcelable(course, 0);
		dest.writeString(sessionId);
		dest.writeParcelable(item, 0);
		if(deliveryLocation == null) {
			dest.writeString("");
		} else {
			dest.writeString(deliveryLocation);
		}
	}
	
	
	private EpicuriOrderItem(Parcel in){
		id = in.readString();
		dinerId = in.readString();
		price = (Money)in.readSerializable();
		quantity = in.readInt();
		long delTime = in.readLong();
		if(0 < delTime){
			delivered = new Date(delTime);
		}
		Parcelable[] tmpArray = in.readParcelableArray(ModifierValue.class.getClassLoader());
		chosenModifiers = new ArrayList<EpicuriMenu.ModifierValue>();
		for(Parcelable p: tmpArray){
			chosenModifiers.add((ModifierValue)p);
		}
		note = in.readString();
		discountReason = in.readString();
		course = in.readParcelable(Course.class.getClassLoader());
		sessionId = in.readString();
		item = in.readParcelable(EpicuriMenu.Item.class.getClassLoader());
		deliveryLocation = in.readString();
	}

	public EpicuriAdjustment getAdjustment() {
		return adjustment;
	}

	public void setAdjustment(EpicuriAdjustment adjustment) {
		this.adjustment = adjustment;
	}


	public static class GroupedOrderItem implements IOrderItem {
		private final EpicuriOrderItem item;
		private int quantity;
		private ArrayList<EpicuriOrderItem> groupedItems = new ArrayList<EpicuriOrderItem>();
		
		public GroupedOrderItem(EpicuriOrderItem item){
			this.item = item;
			quantity = item.quantity;
			groupedItems.add(item);
		}
		public void mergeWith(EpicuriOrderItem newItem){
			mergeWith(newItem, false);
		}
		public void mergeWith(EpicuriOrderItem newItem, boolean finalBill){
			if(!item.isSameOrder(newItem, finalBill)) throw new IllegalArgumentException("Mismatch");
			groupedItems.add(newItem);
			quantity += newItem.quantity;
		}
		
		public int getQuantity(){
			return quantity;
		}
		
		public EpicuriOrderItem getOrderItem(){
			return item;
		}
		
		public ArrayList<EpicuriOrderItem> getGroupedItems(){
			return groupedItems;
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof GroupedOrderItem)) return false;
			return item.isSameOrder(((GroupedOrderItem)o).item);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(new Object[]{item.item.getId(), item.chosenModifiers, item.course});
		}

		@Override
		public String getNote() {
			return item.getNote();
		}

		@Override
		public Course getCourse() {
			return item.getCourse();
		}

		@Override
		public String getDinerId() {
			return item.getDinerId();
		}

		@Override
		public String getId() {
			return item.getId();
		}

		@Override
		public Date getDelivered() {
			return item.getDelivered();
		}

		@Override
		public Item getItem() {
			return item.getItem();
		}

		@Override
		public String getDiscountReason() {
			return item.getDiscountReason();
		}

		@Override
		public ArrayList<ModifierValue> getChosenModifiers() {
			return item.getChosenModifiers();
		}

		@Override
		public boolean isPriceOverridden() {
			return item.isPriceOverridden();
		}

		@Override
		public Money getCalculatedPriceIncludingQuantity() {
			return getCalculatedPrice().multipliedBy(quantity);
		}

		/**
		 * calculated price for the item and it's modifiers.  NOT multiplied by quantity
		 */
		@Override
		public Money getCalculatedPrice() {
			return item.getCalculatedPrice();
		}
		
		@Override
		public Money getPriceOverride() {
			return item.getPriceOverride();
		}
		
	}
}
