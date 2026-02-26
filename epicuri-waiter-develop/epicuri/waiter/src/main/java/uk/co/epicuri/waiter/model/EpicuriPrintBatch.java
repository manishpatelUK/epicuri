package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.interfaces.AbstractPrintable;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;

public class EpicuriPrintBatch extends AbstractPrintable implements Parcelable {
    private static final String TAG_ID = "Id";
    private static final String TAG_IDENTIFIER = "Identifier";
    private static final String TAG_TIME = "Time";
    private static final String TAG_ORDERS = "Orders";
    private static final String TAG_PRINTERID = "PrinterId";
    private static final String TAG_TABLES = "Tables";
    private static final String TAG_MODIFY = "Modify";
    private static final String TAG_BATCHTYPE = "BatchType";
    private static final String TAG_IS_SELF_SERVICE = "IsSelfService";
    private static final String TAG_COVERS = "Covers";
    private static final String TAG_DUE_DATE = "DueDate";
    private static final String TAG_PARTY_NAME = "OrderName";
    private static final String TAG_NOTES = "Notes";
    private static final String TAG_STAFF = "staffUserName";
    private static final String TAG_ADDRESS_LINES = "addressLines";

    private static final String TYPE_TAKEAWAY = "Takeaway";
    private static final String TYPE_WAITER = "Seated";
    private static final String TYPE_COLLECTION = "Collection";
    private static final String TYPE_DELIVERY = "Delivery";
    private static final String TYPE_TAB = "Tab";
    private static final String TYPE_AD_HOC = "AdHoc";
    private static final String TAG_DELIVERY_LOCATION = "deliveryLocation";

    private static final String UNKNOWN_PARTY = "UNKNOWN PARTY";

    private final String id;
    private final Date time;
    private final String printerId;
    private final Date dueTime;
    private final ArrayList<EpicuriOrderItem> orders;
    private final ArrayList<String> tables;
    private final boolean modify;
    private final EpicuriSessionDetail.SessionType sessionType;
    private final int covers;
    private final boolean selfService;
    private final String partyName;
    private final String notes;
    private final Date queuedTime;
    private final String staff;
    private final ArrayList<String> addressLines;
    private boolean printShortCode = false;
    private boolean printInfoAtTop = true;
    private boolean printInfoAtBottom = false;
    private boolean printLinesBetweenCourses = false;
    private String deliveryLocation = "";

    private String printerName;

    public String getId() {
        return id;
    }

    public Date getTime() {
        return time;
    }

    public Date getQueuedTime() {
        return queuedTime;
    }

    public String getPrinterId() {
        return printerId;
    }

    public ArrayList<EpicuriOrderItem> getOrders() {
        return orders;
    }

    public ArrayList<String> getTables() {
        return tables;
    }

    public EpicuriPrintBatch(JSONObject source, Date queuedTime) throws JSONException {
        super();

        this.queuedTime = queuedTime;
        id = source.getString(TAG_ID);
        String identifier = source.getString(TAG_IDENTIFIER);
        if (identifier.equals(TYPE_WAITER)) {
            sessionType = SessionType.DINE;
        } else if (identifier.equals(TYPE_TAKEAWAY)) {
            String takeawayType = source.getString(TAG_BATCHTYPE);
            if (takeawayType.equals(TYPE_COLLECTION)) {
                sessionType = SessionType.COLLECTION;
            } else if (takeawayType.equals(TYPE_DELIVERY)) {
                sessionType = SessionType.DELIVERY;
            } else {
                sessionType = null;
            }
        } else if (identifier.equals(TYPE_TAB)) {
            sessionType = SessionType.TAB;
        } else if(identifier.equals(TYPE_AD_HOC)) {
            sessionType = SessionType.AD_HOC;
        } else {
            sessionType = null;
        }
        covers = source.getInt(TAG_COVERS);
        time = new Date(1000L * source.getInt(TAG_TIME));
        printerId = source.getString(TAG_PRINTERID);
        modify = source.getBoolean(TAG_MODIFY);
        selfService = source.getBoolean(TAG_IS_SELF_SERVICE);
        dueTime = source.has(TAG_DUE_DATE) ? new Date(1000L * source.getInt(TAG_DUE_DATE)) : null;
        partyName = source.has(TAG_PARTY_NAME) ? source.getString(TAG_PARTY_NAME) : UNKNOWN_PARTY;
        notes = source.has(TAG_NOTES) && !source.isNull(TAG_NOTES) ? source.getString(TAG_NOTES) : "";

        JSONArray tablesJson = source.getJSONArray(TAG_TABLES);
        tables = new ArrayList<>(tablesJson.length());
        for (int i = 0; i < tablesJson.length(); i++) {
            tables.add(tablesJson.getString(i));
        }

        JSONArray ordersJson = source.getJSONArray(TAG_ORDERS);
        orders = new ArrayList<>(ordersJson.length());
        for (int i = 0; i < ordersJson.length(); i++) {
            orders.add(new EpicuriOrderItem(ordersJson.getJSONObject(i)));
        }
        Collections.sort(orders, new Comparator<EpicuriOrderItem>() {

            @Override
            public int compare(EpicuriOrderItem lhs, EpicuriOrderItem rhs) {
                return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
            }
        });
        if(source.has(TAG_STAFF)) {
            this.staff = source.getString(TAG_STAFF);
        } else {
            this.staff = "STAFF";
        }
        if(source.has(TAG_ADDRESS_LINES)) {
            JSONArray addressJson = source.getJSONArray(TAG_ADDRESS_LINES);
            addressLines = new ArrayList<>(addressJson.length());
            for (int i = 0; i < addressJson.length(); i++) {
                addressLines.add(addressJson.getString(i));
            }
        } else {
            addressLines = null;
        }

        try {
            EpicuriRestaurant restaurant = LocalSettings.getStaticCachedRestaurant();
            if(restaurant != null) {
                this.printShortCode = Boolean.valueOf(restaurant.getRestaurantDefault("PrintShortCode", "true"));
                this.printInfoAtBottom = Boolean.valueOf(restaurant.getRestaurantDefault("ItemPrintBottom", "false"));
                this.printInfoAtTop = Boolean.valueOf(restaurant.getRestaurantDefault("ItemPrintTop", "true"));
                this.printLinesBetweenCourses = Boolean.valueOf(restaurant.getRestaurantDefault("PrintLinesBetweenCourses", "false"));
            }
        } catch (Exception ex) {
            Log.e("Batch",ex.getMessage());
        }

        if(source.has(TAG_DELIVERY_LOCATION)) {
            deliveryLocation = source.getString(TAG_DELIVERY_LOCATION);
            if(deliveryLocation != null) {
                deliveryLocation = deliveryLocation.trim();
            }
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.UK, "JOB: %s - %s for printer %s (%d items)", sessionType, LocalSettings.getDateFormatWithDate().format(time), printerName, orders.size());
    }

    public byte[] getPrintCodes(boolean doubleHeight, boolean doubleWidth) {
        ArrayList<byte[]> message = new ArrayList<byte[]>();
        preloadWithSizes(message, doubleWidth, doubleHeight);

        message.add("\n\n\n\n\n".getBytes()); // add line breaks to put padding at the top

        List<byte[]> headerInfo = createHeaderInformation();
        if(printInfoAtTop) {
            message.addAll(headerInfo);
        }

        if (modify) {
            message.add("\n * ORDER HAS BEEN MODIFIED *".getBytes());
        }

        ArrayList<EpicuriOrderItem.GroupedOrderItem> groupedOrders = new ArrayList<EpicuriOrderItem.GroupedOrderItem>();

        List<EpicuriOrderItem> orders = getOrders();
        Collections.sort(orders, new Comparator<EpicuriOrderItem>() {

            @Override
            public int compare(EpicuriOrderItem lhs, EpicuriOrderItem rhs) {
                return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
            }

        });

        for (EpicuriOrderItem o : orders) {
            boolean merged = false;

            for (EpicuriOrderItem.GroupedOrderItem go : groupedOrders) {
                if (go.getOrderItem().isSameOrder(o)) {
                    go.mergeWith(o);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                groupedOrders.add(new EpicuriOrderItem.GroupedOrderItem(o));
            }
        }

        String courseId = "-1";
        for (EpicuriOrderItem.GroupedOrderItem i : groupedOrders) {
            if (sessionType == EpicuriSessionDetail.SessionType.DINE && !courseId.equals(i.getCourse().getId())) {
                if (!courseId.equals("-1")) {
                    message.add("\n".getBytes());
                    if(printLinesBetweenCourses) {
                        message.add("\n".getBytes());
                        message.add(DASHED_LINE);
                    }
                }
                courseId = i.getCourse().getId();
                message.add("\n".getBytes());
                message.add(i.getCourse().getName().toUpperCase().getBytes());
            }

            message.add(("\n" + i.getQuantity() + "x " + getItemName(i.getItem())).getBytes());

            for (EpicuriMenu.ModifierValue modifier : i.getChosenModifiers()) {
                message.add(("\n   " + modifier.getName()).getBytes());
            }
            if (null != i.getNote() && i.getNote().length() > 0) {
                message.add(("\n   \"" + i.getNote() + "\"").getBytes());
            }
        }
        if (!TextUtils.isEmpty(notes)) {
            message.add(DASHED_LINE);
            message.add(("\n" + notes).getBytes());
            message.add(DASHED_LINE);
        }

        if(printInfoAtBottom) {
            message.add("\n\n\n".getBytes());
            message.add(DASHED_LINE);
            message.add("\n".getBytes());
            message.addAll(headerInfo);
        }

        return merge(message);
    }

    public List<byte[]> createHeaderInformation() {
        List<byte[]> message = new ArrayList<byte[]>();
        if (sessionType == SessionType.DINE) {
            if (tables.isEmpty()) {
                if (partyName.equals(UNKNOWN_PARTY)) {
                    message.add((partyName + "\n").getBytes());
                }
            } else {
                message.add("Table: ".getBytes());
                for (String table : tables) {
                    message.add(table.getBytes());
                    message.add(" ".getBytes());
                }
                message.add("\n".getBytes());
            }
            message.add("Ordered: ".getBytes());
            message.add(LocalSettings.getDateFormatWithDate().format(time).getBytes());

            message.add(DASHED_LINE);

            if (selfService) {
                message.add("\nOrdered by: SELF SERVICE".getBytes());
                if(deliveryLocation != null && !TextUtils.isEmpty(deliveryLocation)) {
                    message.add(("\nDeliver To: " + deliveryLocation).getBytes());
                }
            } else {
                message.add(("\nOrdered by: " + staff).getBytes());
            }
            message.add(("\nGuests: " + String.valueOf(covers)).getBytes());
        } else if (sessionType == SessionType.TAB) {
            if (!partyName.equals(UNKNOWN_PARTY)) {
                message.add(("Tab: " + partyName + "\n").getBytes());
            } else {
                message.add(("Tab\n").getBytes());
            }

            message.add("Ordered: ".getBytes());
            message.add(LocalSettings.getDateFormatWithDate().format(time).getBytes());

            message.add(DASHED_LINE);

            if (selfService) {
                message.add("\nOrdered by: SELF SERVICE".getBytes());
            } else {
                message.add(("\nOrdered by: " + staff).getBytes());
            }
            message.add(("\nGuests: " + String.valueOf(covers)).getBytes());
        } else if (sessionType == SessionType.AD_HOC){
            message.add(("Quick Order\n").getBytes());
            message.add("Ordered: ".getBytes());
            message.add(LocalSettings.getDateFormatWithDate().format(time).getBytes());
            if(deliveryLocation != null && deliveryLocation.length() > 0) {
                message.add(("\nOrder ID: " + deliveryLocation + "\n").getBytes());
            }
        } else if (null != sessionType) {
            message.add(("Type: " + (sessionType == SessionType.DELIVERY ? "DELIVERY" : "COLLECTION")).getBytes());
            message.add("\nDue: ".getBytes());
            message.add(LocalSettings.getDateFormatWithDate().format(dueTime).getBytes());
            if(addressLines != null) {
                message.add("\n".getBytes());
                for(String addressLine : addressLines) {
                    message.add((addressLine + "\n").getBytes());
                }
            }

            message.add(DASHED_LINE);
            if (selfService) {
                message.add("\nSELF SERVICE".getBytes());
            } else {
                message.add(("\nOrdered by " + staff).getBytes());
            }
            message.add(("\nName: " + partyName).getBytes());

        } else {
            message.add("Unknown type".getBytes());
        }
        message.add(DASHED_LINE);

        return message;
    }

    private String getItemName(EpicuriMenu.Item item) {
        if(printShortCode && item.getShortCode() != null && !TextUtils.isEmpty(item.getShortCode().trim())) {
            return item.getShortCode().trim();
        } else {
            return item.getName();
        }
    }

    public String getPrintText() {
        try {
            String printString = new String(getPrintOutput(), "UTF-8");
            printString = printString.replaceAll(".W0.h0.W1.h1", "");  // strip out resize codes
            return printString;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "CANNOT PREVIEW";
        }
    }

    public byte[] getPrintOutput(boolean doubleHeight, boolean doubleWidth) {
        return getPrintCodes(doubleHeight, doubleWidth);
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeString(this.printerId);
        dest.writeLong(this.dueTime != null ? this.dueTime.getTime() : -1);
        dest.writeTypedList(this.orders);
        dest.writeStringList(this.tables);
        dest.writeByte(this.modify ? (byte) 1 : (byte) 0);
        dest.writeInt(this.sessionType == null ? -1 : this.sessionType.ordinal());
        dest.writeInt(this.covers);
        dest.writeByte(this.selfService ? (byte) 1 : (byte) 0);
        dest.writeString(this.partyName);
        dest.writeString(this.notes);
        dest.writeLong(this.queuedTime != null ? this.queuedTime.getTime() : -1);
        dest.writeString(this.staff);
        dest.writeString(this.printerName);
        dest.writeStringList(this.addressLines);
        dest.writeByte(this.printShortCode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.printInfoAtTop ? (byte) 1 : (byte) 0);
        dest.writeByte(this.printInfoAtBottom ? (byte) 1 : (byte) 0);
        dest.writeByte(this.printLinesBetweenCourses ? (byte) 1 : (byte) 0);
        dest.writeString(this.deliveryLocation);
    }

    protected EpicuriPrintBatch(Parcel in) {
        this.id = in.readString();
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.printerId = in.readString();
        long tmpDueTime = in.readLong();
        this.dueTime = tmpDueTime == -1 ? null : new Date(tmpDueTime);
        this.orders = in.createTypedArrayList(EpicuriOrderItem.CREATOR);
        this.tables = in.createStringArrayList();
        this.modify = in.readByte() != 0;
        int tmpSessionType = in.readInt();
        this.sessionType = tmpSessionType == -1 ? null : SessionType.values()[tmpSessionType];
        this.covers = in.readInt();
        this.selfService = in.readByte() != 0;
        this.partyName = in.readString();
        this.notes = in.readString();
        long tmpQueuedTime = in.readLong();
        this.queuedTime = tmpQueuedTime == -1 ? null : new Date(tmpQueuedTime);
        this.staff = in.readString();
        this.printerName = in.readString();
        this.addressLines = in.createStringArrayList();
        this.printShortCode = in.readByte() != 0;
        this.printInfoAtTop = in.readByte() != 0;
        this.printInfoAtBottom = in.readByte() != 0;
        this.printLinesBetweenCourses = in.readByte() != 0;
        this.deliveryLocation = in.readString();
    }

    public static final Parcelable.Creator<EpicuriPrintBatch> CREATOR =
            new Parcelable.Creator<EpicuriPrintBatch>() {
                @Override public EpicuriPrintBatch createFromParcel(Parcel source) {
                    return new EpicuriPrintBatch(source);
                }

                @Override public EpicuriPrintBatch[] newArray(int size) {
                    return new EpicuriPrintBatch[size];
                }
            };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpicuriPrintBatch that = (EpicuriPrintBatch) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (printerId != null ? !printerId.equals(that.printerId) : that.printerId != null)
            return false;
        return printerName != null ? printerName.equals(that.printerName) : that.printerName == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (printerId != null ? printerId.hashCode() : 0);
        result = 31 * result + (printerName != null ? printerName.hashCode() : 0);
        return result;
    }
}
