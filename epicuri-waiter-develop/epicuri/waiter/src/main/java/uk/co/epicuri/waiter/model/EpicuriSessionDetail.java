package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.epicuri.waiter.model.EpicuriCustomer.Address;
import uk.co.epicuri.waiter.model.EpicuriEvent.Notification;
import uk.co.epicuri.waiter.model.EpicuriEvent.RecurringNotification;
import uk.co.epicuri.waiter.model.EpicuriEvent.Type;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EpicuriSessionDetail implements Parcelable, Serializable {

	public enum SessionType implements Serializable {
		DINE("Seated"),
		TAB("Tab"),
		AD_HOC("QuickOrder"),
		COLLECTION("Collection"),
		DELIVERY("Delivery"),
		REFUND("Refund");
		
		private final String stringDef;
		SessionType(String s){
			stringDef = s;
		}
		
		@Override
		public String toString() {
			return stringDef;
		}
		
		public static SessionType fromString(String s){
			if(null != s){
				for(SessionType type: values()){
					if(s.equalsIgnoreCase(type.stringDef)){
						return type;
					}
				}
			}
			throw new IllegalArgumentException("Unrecognised session type: " + s);
		}
	}
	
	public enum State {
		EMPTY(1),
		IDLE(2),
		SOON(3),
		ATTENTION(4),
		CLOSED(5);
		
		private final int id;
		State(int id){
			this.id = id;
		}
		
		public static State fromInt(int state){
			for(State s: values()){
				if(s.id == state){
					return s;
				}
			}
			throw new IllegalArgumentException("Unrecognised state " + state);
		}
	}

	public enum Icon {
		NONE, ZZZ, ATTN, BILL
	}


	private static final String TAG_ID = "Id";
	private static final String TAG_READABLE_ID = "ReadableId";
	private static final String TAG_TYPE = "SessionType";
	private static final String TAG_IS_ADHOC = "IsAdHoc";
	private static final String TAG_IS_REFUND = "isRefund";
	private static final String TAG_PARTY_NAME = "PartyName";
	private static final String TAG_DELIVERY_NAME = "Name";
	private static final String TAG_MESSAGE = "Message";
	private static final String TAG_START_TIME = "StartTime";
	private static final String TAG_DELIVERY_ADDRESS = "DeliveryAddress";
	private static final String TAG_SESSION_EXPECTED_TIME = "ExpectedTime";
	private static final String TAG_CLOSED_TIME = "ClosedTime";
	private static final String TAG_VOIDED = "Void";
	private static final String TAG_REQUESTED_BILL = "RequestedBill";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_SERVICE_NAME = "ServiceName";
	private static final String TAG_CHAIR_DATA = "ChairData";
	private static final String TAG_SERVICE_ID = "ServiceId";
	private static final String TAG_SESSION_TAKEAWAY_MENU = "TakeawayMenuId";
	private static final String TAG_TELEPHONE = "Telephone";
	private static final String TAG_ACCEPTED = "Accepted";
	private static final String TAG_DELETED = "Deleted";
	private static final String TAG_REJECTED = "Rejected";
	private static final String TAG_REJECTED_REASON = "RejectionNotice";
	
	private static final String TAG_TIP = "TipTotal";
	private static final String TAG_TIP_AMOUNT = "Tips";
	private static final String TAG_PAID = "Paid";
	
	private static final String TAG_SUBTOTAL = "SubTotal";
	private static final String TAG_TOTAL = "Total";
	private static final String TAG_REMAINING_TOTAL = "RemainingTotal";
    private static final String TAG_OVERPAYMENTS = "OverPayments";
	private static final String TAG_TOTAL_PAYMENT = "totalPayment";
    private static final String TAG_CHANGE = "Change";
	private static final String TAG_DISCOUNT_TOTAL = "DiscountTotal";
	private static final String TAG_DELIVERY_COST = "DeliveryCost";
	private static final String TAG_VATTOTAL = "VATTotal";
	private static final String TAG_TOTAL_BEFORE_DEFERMENT = "TotalBeforeDeferment";
	private static final String TAG_TIP_BEFORE_DEFERMENT = "TipBeforeDeferment";

	private static final String TAG_TABLES = "Tables";
	private static final String TAG_DINERS = "Diners";
	private static final String TAG_DINER = "Diner";
	private static final String TAG_DINER_EPICURI = "EpicuriUser";
	
	private static final String TAG_ITEMS = "Orders";
	private static final String TAG_SCHEDULE_ITEMS = "ScheduleItems";
	private static final String TAG_RECURRING = "RecurringScheduleItems";
	private static final String TAG_ADHOC = "AdhocNotifications";
	private static final String TAG_ADJUSTMENTS = "Adjustments";

    private static final String TAG_MENU_ID = "MenuId";
    public static final String TAG_COURSE_AWAY_SENT = "courseAwayMessagesSent";
	private static final String TAG_LINKED_TO_ID = "linkedTo";

	private String id;
	private String readableId;
	private int apiVersion;
	private SessionType type;
	private boolean adHoc;
	private boolean refund;
	private String name;
	private String message;
	private int numberInParty;
	private Date startTime;
	private Date nextScheduleItemDue;
	private Date expectedTime;
	private Date closedTime;
	private long lag;
	private String course;
	private String actionDue;
	private ArrayList<Furniture> tableLayout;
	private boolean billRequested;
	private Address deliveryAddress;
	private String deliveryPhoneNumber;
	private String serviceId;
	private String takeawayMenuId;
    private String serviceDefaultMenuId;
	private double tipPercentage;
	private Money vatTotal;
	private Money total;
	private Money tip;
	private Money discountTotal;
	private Money remainingTotal;
    private Money overPayments;
    private Money change;
	private Money subtotal;
	private Money deliveryCost;
	private Money totalPayment;
	private Money tipBeforeDeferment;
	private Money totalBeforeDeferment;
	private boolean paid;
	private boolean accepted;
	private boolean deleted;
	private boolean voided;
	private boolean rejected;
	private String rejectedReason;
	private VoidReason voidReason;
	
	private EpicuriTable[] tables;
	private ArrayList<EpicuriEvent.Notification> notifications = new ArrayList<EpicuriEvent.Notification>();
	private ArrayList<EpicuriOrderItem> orders = new ArrayList<EpicuriOrderItem>();
	private ArrayList<Diner> diners = new ArrayList<Diner>();
	private ArrayList<EpicuriAdjustment> adjustments = new ArrayList<EpicuriAdjustment>();
	private Diner takeawayDiner;
    private Map<String, Integer> courseAwayMessagesSent = new HashMap<>();
    private List<String> linkedTo;

	// fields that are entirely local for waiter app (not coming from server)
	private boolean isBillSplitMode = false;
	
	public String getId() {
		return id;
	}

	public String getReadableId() {
		return readableId;
	}

	public int getApiVersion() {
		return apiVersion;
	}

	public SessionType getType() {
		return type;
	}

	public boolean isRefund() {
		return refund;
	}

	public String getServiceId() {
		return serviceId;
	}

    public String getServiceDefaultMenuId() {
        return serviceDefaultMenuId;
    }

    public boolean isAccepted() {
		return accepted;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public boolean isRejected() {
		return rejected;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setTotal(Money total) {
		this.total = total;
	}

	public boolean isBillSplitMode() {
		return !isPaid() && isBillSplitMode;
	}

	public void setBillSplitMode(boolean billSplitMode) {
		isBillSplitMode = billSplitMode;
	}

	public String getRejectedReason() {
		return rejectedReason;
	}

	public String getVoidReason() {
		if(null == voidReason) return null;
		return voidReason.reason;
	}

    public Map<String, Integer> getCourseAwayMessagesSent() {
        return courseAwayMessagesSent;
    }

    public static final long A_FEW_MINUTES = (5 * 60 * 1000);
	public State getState() {
		if(isClosed()) return State.CLOSED;

		Date now = new Date();
		long dueInXSeconds;

		if(type == SessionType.DINE){
			Date nextDue = null;
			String axn = "";
			for(EpicuriEvent.Notification n: notifications){
				// ignore already acknowledged scheduled or ad-hoc actions
				if(n.getType() != Type.TYPE_RECURRING && !n.getAcknowledgements().isEmpty()){
					continue;
				}
				// if the next action is due before the 'nextdue' then overwrite nextDue;
				if(null == nextDue || n.getDue().before(nextDue)){
					nextDue = n.getDue();
					axn = n.getText();
				}
			}

			if(null == nextDue){
				return State.IDLE;
			}
			dueInXSeconds = nextDue.getTime() - now.getTime();
		} else {
			// takeaways only have expected time
			dueInXSeconds = expectedTime.getTime() - now.getTime();
		}
		if(dueInXSeconds > A_FEW_MINUTES) return State.IDLE;
		if(dueInXSeconds > - GlobalSettings.ON_TIME_THRESHOLD) return State.SOON;
		return State.ATTENTION;
	}
	
	public Icon getIcon() {
//		for(EpicuriEvent.Notification n: notifications){
//			if(!n.getAcknowledgements().isEmpty()) continue;
//			if(n.getType() == Type.TYPE_ADHOC) return Icon.ATTN;
//			if(n.getType() == Type.TYPE_SCHEDULED) return Icon.ZZZ;
//		}
		return Icon.NONE; //TODO work out icon
	}

	public String getTakeawayMenuId() {
		return takeawayMenuId;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	public int getNumberInParty() {
		return numberInParty;
	}

	public Address getDeliveryAddress() {
		return deliveryAddress;
	}

	public String getDeliveryPhoneNumber() {
		return deliveryPhoneNumber;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getExpectedTime() {
		return expectedTime;
	}

	public Date getClosedTime() {
		return closedTime;
	}

	public boolean isPaid() {
		return paid;
	}

	public boolean isClosed() { return null != closedTime; }

	/**
	 * get the due date of the next item in the schedule, this includes the session lag
	 * @return date including lag
	 */
	public Date getNextScheduleItemDue() {
		return nextScheduleItemDue;
	}

	/** session lag in milliseconds */
	public long getLag() {
		return lag;
	}
	
	public String getCourse() {
		return course;
	}

	public double getTipPercentage(){
		return tipPercentage;
	}

	public String getTipPercentageFormatted() {
		if(Math.abs(Math.round(tipPercentage) - tipPercentage) < 0.01){
			return String.format(Locale.UK, "%.0f", tipPercentage);
		} else {
			return String.format(Locale.UK, "%.1f", tipPercentage);
		}
	}

	public Money getSuggestedTipAmount(){
		return tip;
	}

	public Money getDeliveryCost() {
		return deliveryCost;
	}

	public String getActionDue() {
		return actionDue;
	}

	public Money getVatTotal() {
		return vatTotal;
	}

	public Money getTotal() {
		return total;
	}

	public Money getTotalBeforeDeferment() {
		return totalBeforeDeferment;
	}

	public Money getTipBeforeDeferment() {
		return tipBeforeDeferment;
	}

	public Money getDiscountTotal() {
		return discountTotal;
	}


	public Money  getRemainingTotal() {
		return remainingTotal;
	}

    public Money getChange() {
        return change;
    }

    public Money getOverPayments() {
        return overPayments;
    }

	public Money getTotalPayment() {
		return totalPayment;
	}

	public void setTotalPayment(Money totalPayment) {
		this.totalPayment = totalPayment;
	}

	public boolean paymentsExceedBill() {
		return !getRemainingTotal().isPositive();
	}

	public Money getSubtotal() {
		return subtotal;
	}

	public ArrayList<EpicuriAdjustment> getAdjustments() {
		return adjustments;
	}

	public ArrayList<Furniture> getTableLayout() {
		return tableLayout;
	}

	private CharSequence tablesStringCached = null;
	public CharSequence getTablesString(){
		if(isAdHoc()) return "N/A";
		if(null == tablesStringCached){
			StringBuilder tablesString = new StringBuilder();
			if(tables == null || tables.length == 0){
				tablesString.append("Tab Started");
			} else {
				boolean first = true;
				for(EpicuriTable table: tables){
					if(!first) tablesString.append(", ");
					first = false;
					tablesString.append(table.getName());
				}
			}
			tablesStringCached = tablesString;
		}
		return tablesStringCached;
	}
	
	public EpicuriTable[] getTables() {
		return tables;
	}

	public boolean isTab(){ return type == SessionType.DINE && tables.length == 0; }

	public Diner getTakeawayDiner() {
		return takeawayDiner;
	}

	public List<EpicuriEvent.Notification> getEvents() {
		return notifications;
	}

	public ArrayList<EpicuriOrderItem> getOrders() {
		return orders;
	}
	
	public ArrayList<EpicuriOrderItem> convertOrdersToPendingOrders(){
		ArrayList<EpicuriOrderItem> result = new ArrayList<EpicuriOrderItem>(orders.size());
		String dinerId = diners.get(0).getId();
		int i=0;
		for(EpicuriOrderItem item: orders){
			item.setDinerId(dinerId); // assign all items to zeroth diner
			item.setId(Integer.toString(i++));
			result.add(item);
		}
		return result;
	}

	public ArrayList<Diner> getDiners() {
		return diners;
	}

	public Diner getTableDiner() {
		for(Diner d: diners){
			if(d.isTable()){
				return d;
			}
		}
		return null;
	}
	
	public boolean isBillRequested(){
		return adHoc || billRequested;
	}

	public boolean isAdHoc() {
		return adHoc;
	}

	public List<String> getLinkedTo() {
		return linkedTo;
	}

	public boolean isDeferred() {
		if(adjustments == null) return false;
		for(EpicuriAdjustment adjustment : adjustments) {
			if(adjustment.getDefermentInfo() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpicuriSessionDetail that = (EpicuriSessionDetail) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
	 * Determine whether the session is closeable
	 * @return true if the session is closeable, false if the session must be force-closed
	 */
	public boolean isCloseable(){
		return getTotal().isZero() || isPaid();
	}

	public EpicuriSessionDetail(JSONObject jsonObject) throws JSONException {
		id = jsonObject.getString(TAG_ID);
		if(jsonObject.has(TAG_READABLE_ID)) {
			readableId = jsonObject.getString(TAG_READABLE_ID);
		} else {
			readableId = "";
		}
		type = SessionType.fromString(jsonObject.getString(TAG_TYPE));
		serviceId = jsonObject.getString(TAG_SERVICE_ID);
		paid = jsonObject.getBoolean(TAG_PAID);
		adHoc = jsonObject.has(TAG_IS_ADHOC) && jsonObject.getBoolean(TAG_IS_ADHOC);
		refund = jsonObject.has(TAG_IS_REFUND) && jsonObject.getBoolean(TAG_IS_REFUND);
		billRequested = adHoc || jsonObject.getBoolean(TAG_REQUESTED_BILL);
		deleted = jsonObject.has(TAG_DELETED) && jsonObject.getBoolean(TAG_DELETED);
		voided = jsonObject.has(TAG_VOIDED) && jsonObject.getBoolean(TAG_VOIDED);
		message = jsonObject.has(TAG_MESSAGE) && !jsonObject.isNull(TAG_MESSAGE) ? jsonObject.getString(TAG_MESSAGE) : null;
        serviceDefaultMenuId = jsonObject.has(TAG_MENU_ID) ? jsonObject.getString(TAG_MENU_ID) : null;

		String tmpPhoneNumber = null;
		StringBuilder customerStringBuilder = new StringBuilder();
		Diner tmpDiner = null;
		if(jsonObject.has(TAG_DINER)){
			JSONObject dinerJson = jsonObject.getJSONObject(TAG_DINER);
			tmpDiner = new Diner(dinerJson);
			EpicuriCustomer cust = tmpDiner.getEpicuriCustomer();
			if(cust != null){
				customerStringBuilder.append(cust.getName());
				tmpPhoneNumber = cust.getPhoneNumber();
			}
		}
		takeawayDiner = tmpDiner;
		
		if(jsonObject.has(TAG_TELEPHONE) && !jsonObject.isNull(TAG_TELEPHONE)){
			// override with top-level phone number, if present
			tmpPhoneNumber = jsonObject.getString(TAG_TELEPHONE); 
		}
		deliveryPhoneNumber = tmpPhoneNumber;
		
		if(!jsonObject.isNull(TAG_PARTY_NAME)){
			name = jsonObject.getString(TAG_PARTY_NAME);
		} else if(!jsonObject.isNull(TAG_DELIVERY_NAME)){
			name = jsonObject.getString(TAG_DELIVERY_NAME);
		} else {
			name = customerStringBuilder.toString();
		}
		
		if(jsonObject.has(TAG_SESSION_EXPECTED_TIME)){
			expectedTime = new Date(1000L * jsonObject.getLong(TAG_SESSION_EXPECTED_TIME));
		} else {
			expectedTime = null;
		}
		
		if(jsonObject.has(TAG_SESSION_TAKEAWAY_MENU)){
			takeawayMenuId = jsonObject.getString(TAG_SESSION_TAKEAWAY_MENU);
		} else {
			takeawayMenuId = "-1";
		}


		// Bit of a hack, shouldn't require this
		total = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_TOTAL)));
        remainingTotal = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_REMAINING_TOTAL)));
		discountTotal = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_DISCOUNT_TOTAL)));
		subtotal = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_SUBTOTAL)));
		vatTotal = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_VATTOTAL)));

		if(jsonObject.has(TAG_TIP_AMOUNT)) {
			tip = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_TIP_AMOUNT)));
		} else {
			tip = null;
		}

		voidReason = jsonObject.isNull("VoidReason") ? null : new VoidReason(jsonObject.getJSONObject("VoidReason"));
		startTime = new Date(1000L * jsonObject.getInt(TAG_START_TIME));
		
		if(jsonObject.has(TAG_CLOSED_TIME) && !jsonObject.isNull(TAG_CLOSED_TIME)){
			final int closedTimeStamp= jsonObject.getInt(TAG_CLOSED_TIME);
			if(0 < closedTimeStamp){
				closedTime = new Date(1000L * closedTimeStamp);
			} else {
				closedTime= null;
			}
		} else {
			closedTime = null;
		}
		if(jsonObject.has(TAG_OVERPAYMENTS)) {
			apiVersion = 2;
			overPayments = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_OVERPAYMENTS)));
			change = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_CHANGE)));
		} else {
			apiVersion = 1;
			if(remainingTotal.isPositive()){
				overPayments = Money.zero(LocalSettings.getCurrencyUnit());
			} else {
				overPayments = Money.zero(LocalSettings.getCurrencyUnit()).minus(remainingTotal);
			}
			change = Money.zero(LocalSettings.getCurrencyUnit());
		}

		if(jsonObject.has(TAG_TOTAL_PAYMENT)) {
			totalPayment = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_TOTAL_PAYMENT)));
		}

		if(jsonObject.has(TAG_TIP)){
			tipPercentage = jsonObject.getDouble(TAG_TIP);
		} else if(jsonObject.has("SuggestedTip")) {
			// DEPRECATED
			tipPercentage = jsonObject.getDouble("SuggestedTip");
		} else {
			tipPercentage = 0;
		}

		if(jsonObject.has(TAG_TIP_BEFORE_DEFERMENT)) {
			tipBeforeDeferment = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_TIP_BEFORE_DEFERMENT)));
		}
		if(jsonObject.has(TAG_TOTAL_BEFORE_DEFERMENT)) {
			totalBeforeDeferment = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_TOTAL_BEFORE_DEFERMENT)));
		}
		
//		if(jsonObject.has(TAG_ACTION_DUE)){
//			actionDue = jsonObject.getString(TAG_ACTION_DUE);
//		}
		actionDue = null;

		if(jsonObject.has(TAG_CHAIR_DATA) 
				&& !jsonObject.isNull(TAG_CHAIR_DATA)
				&& !jsonObject.getString(TAG_CHAIR_DATA).trim().equals("")){
			
			String tableLayoutString = jsonObject.getString(TAG_CHAIR_DATA);
			JSONArray furnitureJson = new JSONArray(tableLayoutString);
			tableLayout = new ArrayList<Furniture>(furnitureJson.length());
			for(int i=0; i<furnitureJson.length(); i++){
				tableLayout.add(new Furniture(furnitureJson.getJSONObject(i)));
			}
		} else {
			tableLayout = null;
		}
		
		
		{
			JSONArray ordersJson = jsonObject.getJSONArray(TAG_ITEMS);
			for(int i=0; i<ordersJson.length(); i++){
				JSONObject orderJson = ordersJson.getJSONObject(i);
				EpicuriOrderItem item = new EpicuriOrderItem(orderJson);
				orders.add(item);
			}
		}
		
		switch(type) {
		case DINE:{
			course = jsonObject.getString(TAG_SERVICE_NAME);
			lag = 1000L * jsonObject.getInt(TAG_DELAY);
			accepted = true;
			rejected = false;
			rejectedReason = null;
			deliveryAddress = null;
			deliveryCost = null;
			{
				Date nextItemDue = null;
				if(!jsonObject.isNull(TAG_SCHEDULE_ITEMS)){
					
					JSONArray eventsJson = jsonObject.getJSONArray(TAG_SCHEDULE_ITEMS);
					ArrayList<EpicuriEvent> events = new ArrayList<EpicuriEvent>(eventsJson.length());
					for(int i=0; i<eventsJson.length(); i++){
						JSONObject eventJson = eventsJson.getJSONObject(i);
						EpicuriEvent event = new EpicuriEvent(eventJson);
						events.add(event);
					}
					
					Collections.sort(events, new Comparator<EpicuriEvent>() {
						@Override
						public int compare(EpicuriEvent lhs, EpicuriEvent rhs) {
							if(lhs.getDelay() < rhs.getDelay()) return -1;
							return 1;
						}
						
					});
					
					final long startTimeMillis = getStartTime().getTime();
					for(EpicuriEvent e: events){
						for(EpicuriEvent.ScheduledEventNotification n: e.getNotifications()){
							if(n.getTarget().startsWith(GlobalSettings.TARGET)){
								n.setDue(startTimeMillis, lag);
								notifications.add(n);
								if(n.getAcknowledgements().size() == 0 && null == nextItemDue){
									nextItemDue = n.getDue();
								}
							}
						}						
					}
				}
				nextScheduleItemDue = nextItemDue;
				
				if(!jsonObject.isNull(TAG_RECURRING)){
					JSONArray eventsJson = jsonObject.getJSONArray(TAG_RECURRING);
					for(int i=0; i<eventsJson.length(); i++){
						EpicuriEvent.RecurringEvent event = new EpicuriEvent.RecurringEvent(eventsJson.getJSONObject(i));

//						long timeOffset = startTime.getTime() + delay * 1000L * 60l;
						for(RecurringNotification n: event.getNotifications()){
							long mostRecentAcknowledgement = 0;
							for(Date d: n.getAcknowledgements()){
								if(d.getTime() > mostRecentAcknowledgement){
									mostRecentAcknowledgement = d.getTime();
								}
							}
							if(0 == mostRecentAcknowledgement){
								// no notifications set, so use start time + initial delay
								mostRecentAcknowledgement = startTime.getTime() + event.getInitialDelay();
							} else {
								// schedule next action for ack'd time + period
								mostRecentAcknowledgement += event.getPeriod();
							}
							n.setNextDue(new Date(mostRecentAcknowledgement));
							notifications.add(n);
						}
					}
				}

				boolean isFirst = true;
				for(Notification n: notifications){
					if(n.getType() == Type.TYPE_SCHEDULED){
						if(isFirst && n.getAcknowledgements().isEmpty()){
							((EpicuriEvent.ScheduledEventNotification)n).setFutureAction(false);
							isFirst = false;
						} else {
							((EpicuriEvent.ScheduledEventNotification)n).setFutureAction(true);
						}
					}
				}
			}

			{
				int tmpNumberInParty = 0;
				JSONArray dinersJson = jsonObject.getJSONArray(TAG_DINERS);
				for(int i=0; i<dinersJson.length(); i++){
					JSONObject dinerJson = dinersJson.getJSONObject(i);
					EpicuriSessionDetail.Diner diner = new EpicuriSessionDetail.Diner(dinerJson);
					if(!diner.isTable()){
						tmpNumberInParty++;
					}
					diners.add(diner);
				}
				numberInParty = tmpNumberInParty;
			}
			
			JSONArray tablesJson = jsonObject.getJSONArray(TAG_TABLES);
			tables = new EpicuriTable[tablesJson.length()];
			for(int j=0; j<tablesJson.length(); j++){
				tables[j] = new EpicuriTable(tablesJson.getJSONObject(j));
			}
			break;
		}
		case DELIVERY:
		case COLLECTION: {
			nextScheduleItemDue = null;
			course = null;
			lag = 0;
			numberInParty = 1;
			accepted = jsonObject.getBoolean(TAG_ACCEPTED);
			rejected = jsonObject.getBoolean(TAG_REJECTED);
			JSONObject dinerJson = jsonObject.getJSONObject(TAG_DINER);
			EpicuriSessionDetail.Diner diner = new EpicuriSessionDetail.Diner(dinerJson);
			diners.add(diner);
			tables = null;
			tablesStringCached = "Takeaway";
			if(jsonObject.has(TAG_REJECTED_REASON) && !jsonObject.isNull(TAG_REJECTED_REASON)){
				rejectedReason = jsonObject.getString(TAG_REJECTED_REASON);
			} else {
				rejectedReason = null;
			}

			if(jsonObject.has(TAG_DELIVERY_ADDRESS)){
				deliveryAddress = new Address(jsonObject.getJSONObject(TAG_DELIVERY_ADDRESS));
			} else {
				deliveryAddress = null;
			}
			if(jsonObject.has(TAG_DELIVERY_COST) && !jsonObject.isNull(TAG_DELIVERY_COST)){
				deliveryCost = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit().getCurrencyCode(), jsonObject.getDouble(TAG_DELIVERY_COST)));
			} else {
				deliveryCost = null;
			}
			
			break;
		}
		default:
			throw new IllegalStateException();
		}

		if(jsonObject.has(TAG_ADHOC) && !jsonObject.isNull(TAG_ADHOC)) {
			JSONArray eventsJson = jsonObject.getJSONArray(TAG_ADHOC);
			for(int i=0; i<eventsJson.length(); i++){
				JSONObject eventJson = eventsJson.getJSONObject(i);
				EpicuriEvent.Notification n = new EpicuriEvent.AdhocNotification(eventJson);
				if(n.getTarget().equals(GlobalSettings.TARGET)){
					notifications.add(n);
				}
			}
		}
		
		Collections.sort(notifications, new Comparator<Notification>() {

			@Override
			public int compare(Notification lhs, Notification rhs) {
				if(lhs.getType() != rhs.getType()){
					// bump adhoc events to the top of the list
					if(lhs.getType() == Type.TYPE_ADHOC) return -1;
					else if(rhs.getType() == Type.TYPE_ADHOC) return 1;
				}
				return lhs.getDue().compareTo(rhs.getDue());
			}
		});

		if(jsonObject.has(TAG_ADJUSTMENTS)){
			JSONArray adjustmentsJson = jsonObject.getJSONArray(TAG_ADJUSTMENTS);
			for(int i=0; i<adjustmentsJson.length(); i++){
				JSONObject adjustmentJson = adjustmentsJson.getJSONObject(i);
				EpicuriAdjustment a = new EpicuriAdjustment(adjustmentJson);
				adjustments.add(a);
			}
		}

		if(jsonObject.has(TAG_COURSE_AWAY_SENT)){
            courseAwayMessagesSent = toMap(jsonObject.getJSONObject(TAG_COURSE_AWAY_SENT));
        } else {
		    courseAwayMessagesSent = new HashMap<>();
        }
		if(jsonObject.has(TAG_LINKED_TO_ID)) {
			linkedTo = new ArrayList<>();
			JSONArray jsonArray = jsonObject.getJSONArray(TAG_LINKED_TO_ID);
			for(int i = 0; i < jsonArray.length(); i++) {
				linkedTo.add(jsonArray.getString(i));
			}
		}
	}

    public static Map<String, Integer> toMap(JSONObject jsonobj)  throws JSONException {
        Map<String, Integer> map = new HashMap<>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Integer value = (Integer) jsonobj.get(key);
            map.put(key, value);
        }   return map;
    }

	public void setRemainingTotal(Money remainingTotal) {
		this.remainingTotal = remainingTotal;
	}

	public int getNumberOfDishes(){
		int totalQuantity = 0;
		for(EpicuriOrderItem i: orders){
			totalQuantity += i.getQuantity();
		}
		return totalQuantity;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static final Parcelable.Creator<EpicuriSessionDetail> CREATOR = new Parcelable.Creator<EpicuriSessionDetail>() {
		
		@Override
		public EpicuriSessionDetail[] newArray(int size) {
			return new EpicuriSessionDetail[size];
		}
		
		@Override
		public EpicuriSessionDetail createFromParcel(Parcel source) {
			return new EpicuriSessionDetail(source);
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
/*
 * 
	private final int id;
	private final SessionType type;
	private final String name;
	private final int numberInParty;
	private final Date startTime;
	private final Date expectedTime;
	private final Date closedTime;
	private final int delay;
	private final String course;
	private final String actionDue;
	private final ArrayList<Furniture> tableLayout;
	private final boolean billRequested;
	private final String tables;
	private final Address deliveryAddress;
	private final String deliveryPhoneNumber;
	
	private final ArrayList<EpicuriEvent.Notification> notifications = new ArrayList<EpicuriEvent.Notification>();
	private final ArrayList<EpicuriOrderItem> orders = new ArrayList<EpicuriOrderItem>();
	private final HashMap<Long, List<EpicuriOrderItem>> pendingOrders = new HashMap<Long, List<EpicuriOrderItem>>();
	private final ArrayList<Diner> diners = new ArrayList<Diner>();(non-Javadoc)
 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(readableId);
		dest.writeInt(apiVersion);
		dest.writeString(type.toString());
		dest.writeString(name);
		dest.writeString(message);
		dest.writeInt(numberInParty);
		dest.writeString(takeawayMenuId);
        dest.writeString(serviceDefaultMenuId);
		dest.writeByte(paid ? (byte) 1 : (byte) 0);
		dest.writeByte(accepted ? (byte)1 : (byte)0);
		dest.writeByte(deleted ? (byte) 1 : (byte) 0);
		dest.writeByte(rejected ? (byte) 1 : (byte) 0);
		dest.writeByte(voided? (byte) 1 : (byte) 0);
		dest.writeString(rejectedReason);
		dest.writeParcelable(voidReason, 0);
		dest.writeLong(null == startTime ? 0 : startTime.getTime());
		dest.writeLong(null == expectedTime ? 0 : expectedTime.getTime());
		dest.writeLong(null == closedTime ? 0 : closedTime.getTime());
		dest.writeLong(null == nextScheduleItemDue ? 0 : nextScheduleItemDue.getTime());
		dest.writeString(null == total ? null : total.toString());
		dest.writeString(null == remainingTotal ? null : remainingTotal.toString());
        dest.writeString(null == overPayments ? null : overPayments.toString());
        dest.writeString(null == totalPayment ? null : totalPayment.toString());
        dest.writeString(null == change ? null : change.toString());
		dest.writeString(null == discountTotal ? null : discountTotal.toString());
		dest.writeString(null == subtotal ? null : subtotal.toString());
		dest.writeString(null == vatTotal ? null : vatTotal.toString());
		dest.writeString(null == deliveryCost ? null : deliveryCost.toString());
		dest.writeString(null == tip ? null : tip.toString());
		dest.writeDouble(tipPercentage);
		dest.writeLong(lag);
		dest.writeString(course);
		dest.writeString(actionDue);
		if(null == tableLayout){
			dest.writeByte((byte)0);
		} else {
			dest.writeByte((byte)1);
			dest.writeTypedList(tableLayout);
		}
		dest.writeInt(billRequested ? 1 : 0);
		dest.writeInt(adHoc ? 1 : 0);
		dest.writeInt(refund ? 1 : 0);
		dest.writeParcelableArray(tables, 0);
		dest.writeParcelable(deliveryAddress, 0);
		dest.writeString(deliveryPhoneNumber);
		dest.writeString(serviceId);
		dest.writeParcelable(takeawayDiner, 0);
		
//		dest.write
//		private final ArrayList<EpicuriEvent.Notification> notifications = new ArrayList<EpicuriEvent.Notification>();
//		private final ArrayList<EpicuriOrderItem> orders = new ArrayList<EpicuriOrderItem>();
//		private final ArrayList<Diner> diners = new ArrayList<Diner>();(non-Javadoc)
//		private final HashMap<Long, List<EpicuriOrderItem>> pendingOrders = new HashMap<Long, List<EpicuriOrderItem>>();

		// TODO persist missing lists
//		dest.writeTypedList(notifications);
		dest.writeTypedList(orders);
		dest.writeTypedList(diners);
		dest.writeTypedList(adjustments);
//		dest.writeMap(pendingOrders);

        dest.writeInt(courseAwayMessagesSent.size());
        for(Map.Entry<String, Integer> entry : courseAwayMessagesSent.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue());
        }
		dest.writeString(null == totalBeforeDeferment ? null : totalBeforeDeferment.toString());
		dest.writeString(null == tipBeforeDeferment ? null : tipBeforeDeferment.toString());
		if(linkedTo != null) {
			dest.writeInt(linkedTo.size());
			for(String string : linkedTo) {
				dest.writeString(string);
			}
		} else {
			dest.writeInt(0);
		}
	}
	
	private EpicuriSessionDetail(Parcel in) {
		id = in.readString();
		readableId = in.readString();
		apiVersion = in.readInt();
		type = SessionType.fromString(in.readString());
		name = in.readString();
		message = in.readString();
		numberInParty = in.readInt();
		takeawayMenuId = in.readString();
        serviceDefaultMenuId = in.readString();
		paid = in.readByte() == 0x1;
		accepted = in.readByte() == 0x1;
		deleted = in.readByte() == 0x1;
		rejected = in.readByte() == 0x1;
		voided = in.readByte() == 0x1;
		rejectedReason = in.readString();
		voidReason = in.readParcelable(VoidReason.class.getClassLoader());
		long tmpLong;
		tmpLong = in.readLong();
		if(0 == tmpLong){
			startTime = null;
		} else {
			startTime = new Date(tmpLong);
		}
		
		tmpLong = in.readLong();
		if(0 == tmpLong){
			expectedTime = null;
		} else {
			expectedTime = new Date(tmpLong);
		}
		
		tmpLong = in.readLong();
		if(0 == tmpLong){
			closedTime = null;
		} else {
			closedTime = new Date(tmpLong);
		}

		tmpLong = in.readLong();
		if(0 == tmpLong){
			nextScheduleItemDue = null; 
		} else{
			nextScheduleItemDue = new Date(tmpLong);
		}

		String tmpString;

		tmpString = in.readString();
		if(null != tmpString){
			total= Money.parse(tmpString);
		} else {
			total= null;
		}
		tmpString = in.readString();
		if(null != tmpString){
			remainingTotal = Money.parse(tmpString);
		} else {
			remainingTotal = null;
		}
        tmpString = in.readString();
        if(null != tmpString){
            overPayments = Money.parse(tmpString);
        } else {
            overPayments = null;
        }
        tmpString = in.readString();
        if(null != tmpString) {
        	totalPayment = Money.parse(tmpString);
		} else {
        	totalPayment = null;
		}
        tmpString = in.readString();
        if(null != tmpString){
            change = Money.parse(tmpString);
        } else {
            change = null;
        }
		tmpString = in.readString();
		if(null != tmpString){
			discountTotal = Money.parse(tmpString);
		} else {
			discountTotal = null;
		}
		tmpString = in.readString();
		if(null != tmpString){
			subtotal= Money.parse(tmpString);
		} else {
			subtotal= null;
		}
		tmpString = in.readString();
		if(null != tmpString){
			vatTotal = Money.parse(tmpString);
		} else {
			vatTotal = null;
		}
		tmpString = in.readString();
		if(null != tmpString){
			deliveryCost = Money.parse(tmpString);
		} else {
			deliveryCost = null;
		}
		tmpString = in.readString();
		if(null != tmpString){
			tip = Money.parse(tmpString);
		} else {
			tip = null;
		}
		tipPercentage = in.readDouble();
		
		lag = in.readLong();
		course = in.readString();
		actionDue = in.readString();
		if(1 == in.readByte()){
			tableLayout = new ArrayList<Furniture>();
			in.readTypedList(tableLayout, Furniture.CREATOR);
		} else {
			tableLayout = null;
		}
		billRequested = in.readInt() == 1;
		adHoc = in.readInt() == 1;
		refund = in.readInt() == 1;
		{
			Parcelable[] temp = in.readParcelableArray(EpicuriTable.class.getClassLoader());
			if(null == temp){
				tables = null;
			} else {
				tables = new EpicuriTable[temp.length];
				for(int i=0; i<temp.length; i++){
					tables[i] = (EpicuriTable) temp[i];
				}
			}
		}
		deliveryAddress = in.readParcelable(Address.class.getClassLoader());
		deliveryPhoneNumber = in.readString();
		serviceId = in.readString();
		takeawayDiner = in.readParcelable(EpicuriCustomer.class.getClassLoader());
		
		// TODO: read notifications
		
		in.readTypedList(orders, EpicuriOrderItem.CREATOR);
		in.readTypedList(diners, EpicuriSessionDetail.Diner.CREATOR);
		in.readTypedList(adjustments, EpicuriAdjustment.CREATOR);
//		in.readMap(pendingOrders, HashMap.class.getClassLoader());

        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            Integer value = in.readInt();
            courseAwayMessagesSent.put(key,value);
        }
        tmpString = in.readString();
        if(tmpString != null) {
        	totalBeforeDeferment = Money.parse(tmpString);
		}
		tmpString = in.readString();
		if(tmpString != null) {
			tipBeforeDeferment = Money.parse(tmpString);
		}
		size = in.readInt();
		if(size > 0) {
			linkedTo = new ArrayList<>();
			for(int i = 0; i < size; i++) {
				linkedTo.add(in.readString());
			}
		}

	}


	public static final class Diner implements Parcelable, Serializable {
		private static final String TAG_ID = "Id";
		private static final String TAG_IS_TABLE = "IsTable";
		private static final String TAG_DISCOUNTS = "discounts";
		private static final String TAG_TIP = "tip";
		private static final String TAG_TOTAL = "total";
		private static final String TAG_SUBTOTAL = "subTotal";
		private static final String TAG_VAT = "vat";
		private static final String TAG_EPICURI = "EpicuriUser";
		private static final String TAG_ORDERS = "Orders";
		private static final String TAG_OBJ_ORDERS = "orders";
		private static final String TAG_IS_BIRTHDAY = "IsBirthday";
        private static final String TAG_NAME = "name";

		private Diner(){
			id = "-1";
			isTable = false;
            epicuriCustomer = null;
            orders = new String[0];
            obj_orders = new EpicuriOrderItem[0];
            isBirthday = false;
            discounts = tip = total = subTotal = vat = null;
            name = null;
		}
		public static Diner getDummyDiner(){
			return new Diner();
		}

		private final String id;
		private final boolean isTable;
		private final Money discounts;
		private final Money tip;
		private final Money total;
		private final Money subTotal;
		private final Money vat;
		private final EpicuriCustomer epicuriCustomer;
		private final String[] orders;
		private final EpicuriOrderItem[] obj_orders;
		private final boolean isBirthday;
        private final String name;

        private final Money ZERO = Money.zero(LocalSettings.getCurrencyUnit());
		
		public String getId() {
			return id;
		}

		public boolean isTable() {
			return isTable;
		}
		
		public boolean isBirthday() {
			return isBirthday;
		}

		public EpicuriCustomer getEpicuriCustomer(){
			return epicuriCustomer;
		}

		public String[] getOrders() {
			return orders;
		}

		public Money getDiscounts() {
			return discounts;
		}

		public Money getTip() {
			return tip;
		}

		public Money getTotal() {
			return total;
		}

		public Money getSubTotal() {
			return subTotal;
		}

		public EpicuriOrderItem[] getObj_orders() {
			return obj_orders;
		}

        public Money getVat() {
            return vat;
        }

        public String getName() {
            return name;
        }

        public Diner(JSONObject dinerJson) throws JSONException {
			id = dinerJson.getString(TAG_ID);
			isTable = dinerJson.getBoolean(TAG_IS_TABLE);
			if(dinerJson.has(TAG_DISCOUNTS)) {
                discounts = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit()
                        .getCurrencyCode(), dinerJson.getDouble(TAG_DISCOUNTS)));
            } else {
			    discounts = ZERO;
            }
            if(dinerJson.has(TAG_TIP)) {
                tip = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit()
                        .getCurrencyCode(), dinerJson.getDouble(TAG_TIP)));
            } else {
			    tip = ZERO;
            }
            if(dinerJson.has(TAG_TOTAL)) {
                total = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit()
                        .getCurrencyCode(), dinerJson.getDouble(TAG_TOTAL)));
            } else {
			    total = ZERO;
            }
            if(dinerJson.has(TAG_SUBTOTAL)) {
                subTotal = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit()
                        .getCurrencyCode(), dinerJson.getDouble(TAG_SUBTOTAL)));
            } else {
			    subTotal = ZERO;
            }
            if(dinerJson.has(TAG_VAT)) {
                vat = Money.parse(String.format(Locale.UK, "%s %.02f", LocalSettings.getCurrencyUnit()
                        .getCurrencyCode(), dinerJson.getDouble(TAG_VAT)));
            } else {
			    vat = ZERO;
            }
			isBirthday = dinerJson.getBoolean(TAG_IS_BIRTHDAY);
			JSONArray orderJson = dinerJson.getJSONArray(TAG_ORDERS);
			orders = new String[orderJson.length()];
			for(int i=0; i<orderJson.length(); i++){
				orders[i] = orderJson.getString(i);
			}
			if(dinerJson.has(TAG_EPICURI) && !dinerJson.isNull(TAG_EPICURI)){
				epicuriCustomer = new EpicuriCustomer(dinerJson.getJSONObject(TAG_EPICURI));
			} else {
				epicuriCustomer = null;
			}
			if(dinerJson.has(TAG_OBJ_ORDERS) && !dinerJson.isNull(TAG_OBJ_ORDERS)) {
				JSONArray objs = dinerJson.getJSONArray(TAG_OBJ_ORDERS);
				obj_orders = new EpicuriOrderItem[objs.length()];
				for (int i = 0; i < objs.length(); ++i)
					obj_orders[i] = new EpicuriOrderItem(objs.getJSONObject(i));
			} else obj_orders = new EpicuriOrderItem[0];
			name = dinerJson.has(TAG_NAME) ? dinerJson.getString(TAG_NAME) : "Guest";
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder()
			.append("{Diner of type ")
			.append(isTable ? "Table" : "Guest")
			.append(" with ID ").append(id == null ? "" : id)
			.append(",epicuri: ").append(null == epicuriCustomer ? "": epicuriCustomer.getId())
			.append(",orders: ");
			for(int i=0;i<orders.length; i++){
				sb.append(orders[i]).append(",");
			}
			sb.append("}");
			return sb.toString();
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Diner)){
				return false;
			}
			return id.equals(((Diner)o).id);
		}

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

		@Override public int describeContents() {
			return 0;
		}

		@Override public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(this.id);
			dest.writeByte(this.isTable ? (byte) 1 : (byte) 0);
			dest.writeSerializable(this.discounts);
			dest.writeSerializable(this.tip);
			dest.writeSerializable(this.total);
			dest.writeSerializable(this.subTotal);
			dest.writeSerializable(this.vat);
			dest.writeParcelable(this.epicuriCustomer, flags);
			dest.writeStringArray(this.orders);
			dest.writeTypedArray(this.obj_orders, flags);
			dest.writeByte(this.isBirthday ? (byte) 1 : (byte) 0);
			dest.writeString(name);
		}

		protected Diner(Parcel in) {
			this.id = in.readString();
			this.isTable = in.readByte() != 0;
			this.discounts = (Money) in.readSerializable();
			this.tip = (Money) in.readSerializable();
			this.total = (Money) in.readSerializable();
			this.subTotal = (Money) in.readSerializable();
			this.vat = (Money) in.readSerializable();
			this.epicuriCustomer = in.readParcelable(EpicuriCustomer.class.getClassLoader());
			this.orders = in.createStringArray();
			this.obj_orders = in.createTypedArray(EpicuriOrderItem.CREATOR);
			this.isBirthday = in.readByte() != 0;
			this.name = in.readString();
		}

		public static final Creator<Diner> CREATOR = new Creator<Diner>() {
			@Override public Diner createFromParcel(Parcel source) {
				return new Diner(source);
			}

			@Override public Diner[] newArray(int size) {
				return new Diner[size];
			}
		};
	}

	
	public static class Furniture implements Parcelable, Serializable {
		private static final String TAG_TYPE = "type";
		private static final String TAG_X = "x";
		private static final String TAG_Y = "y";
		private static final String TAG_WIDTH = "width";
		private static final String TAG_BREADTH = "breadth";
		private static final String TAG_ROTATION = "rotation";
		private static final String TAG_DINER_ID = "dinerId";
		
		private float x;
		private float y; 
		private final String dinerId;
		public String getDinerId() { return dinerId; }
		
		private float width = 250;
		private float breadth = 100;
		private float rotation = 45;
		
		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

		public void setPosition(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(float width) {
			this.width = width;
		}

		public float getBreadth() {
			return breadth;
		}

		public void setBreadth(float breadth) {
			this.breadth = breadth;
		}

		/** angle in degrees */
		public float getRotation() {
			return rotation;
		}
		
		/** angle in degrees */
		public void setRotation(float rotation) {
			this.rotation = rotation;
		}

		public Furniture (JSONObject tableJson) throws JSONException{
			x = (float)tableJson.getDouble(TAG_X);
			y = (float)tableJson.getDouble(TAG_Y);
			width = (float)tableJson.getDouble(TAG_WIDTH);
			breadth = (float)tableJson.getDouble(TAG_BREADTH);
			rotation = (float)tableJson.getDouble(TAG_ROTATION);
			dinerId = tableJson.getString(TAG_DINER_ID);
		}
		
		public JSONObject toJson(){ 
			try {
				JSONObject outputJson = new JSONObject();
				outputJson.put(TAG_TYPE, "table");
				outputJson.put(TAG_X, x);
				outputJson.put(TAG_Y, y);
				outputJson.put(TAG_WIDTH, width);
				outputJson.put(TAG_BREADTH, breadth);
				outputJson.put(TAG_ROTATION, rotation);
				outputJson.put(TAG_DINER_ID, dinerId);
				return outputJson;
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		public static final Parcelable.Creator<Furniture> CREATOR = new Parcelable.Creator<EpicuriSessionDetail.Furniture>() {
			
			@Override
			public Furniture[] newArray(int size) {
				return new Furniture[size];
			}
			
			@Override
			public Furniture createFromParcel(Parcel source) {
				return new Furniture(source);
			}
		};
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeFloat(x);
			dest.writeFloat(y);
			dest.writeString(dinerId);
			dest.writeFloat(width);
			dest.writeFloat(breadth);
			dest.writeFloat(rotation);
		}
		private Furniture(Parcel in){
			x = in.readFloat();
			y = in.readFloat();
			dinerId = in.readString();
			width = in.readFloat();
			breadth = in.readFloat();
			rotation = in.readFloat();
		}
	}


	public Diner getDinerFromId(String dinerId) {
		for(Diner d: diners){
			if(d.getId() != null && d.getId().equals(dinerId)){
				return d;
			}
		}
		return null;
	}

	public static EpicuriSessionDetail getSessionForTable(
			List<EpicuriSessionDetail> sessions, String tableId) throws IllegalArgumentException{
		if(null == sessions) throw new IllegalArgumentException("Passed a null list");
		for (EpicuriSessionDetail s : sessions) {
			if (null != s.tables) {
				for(EpicuriTable thisTable: s.tables) {
					if (thisTable.getId().equals(tableId)) {
						return s;
					}
				}
			}
		}
		return null;
	}

	public static EpicuriSessionDetail getSessionForId(
			List<EpicuriSessionDetail> sessions, String id) {
		if(null == sessions) return null;
		for (EpicuriSessionDetail s : sessions) {
			if(s.id.equals(id)) return s;
		}
		return null;
	}


    public String getStatusString() {
	    if(isVoided()) {
		    return "Voided";
	    } else if (isClosed()) {
		    return "Closed";
	    } else if (isPaid()) {
            return "Paid";
        } else if (isBillRequested()) {
            return "Bill Requested";
        }
	    if(getType() == SessionType.DINE) {
		    if (!getOrders().isEmpty()) {
			    return "Orders placed";
		    } else if (getTables() != null && getTables().length > 0) {
			    return "Seated";
		    } else {
			    return "In Progress";
		    }
	    } else {
		    long millisOverdue = (new Date().getTime() - getExpectedTime().getTime());
		    if (millisOverdue > 0) {
			    return GlobalSettings.minsLate(millisOverdue).toString();
		    } else {
			    return "";
		    }
	    }
    }

	public static class VoidReason implements Parcelable, Serializable {
		String reason;

		public VoidReason(JSONObject jsonObject) throws JSONException {
			if(!jsonObject.isNull("Reason")){
				reason = jsonObject.getString("Reason");
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(this.reason);
		}

		private VoidReason(Parcel in) {
			this.reason = in.readString();
		}

		public static final Creator<VoidReason> CREATOR = new Creator<VoidReason>() {
			public VoidReason createFromParcel(Parcel source) {
				return new VoidReason(source);
			}

			public VoidReason[] newArray(int size) {
				return new VoidReason[size];
			}
		};
	}


	// these methods are used to recalcalate tip and total if there is a non-refundable overpayment
	public Money getFudgedReceiptSuggestedTipAmount() {
		if(getOverPayments().isZero()) return getSuggestedTipAmount();
		return getSuggestedTipAmount().plus(getOverPayments());
	}

	public String getFudgedReceiptTipPercentage() {
		if(getOverPayments().isZero()) return getTipPercentageFormatted();

		double calculatedPercentage = 100d * getFudgedReceiptSuggestedTipAmount().getAmount().doubleValue() /
				getSubtotal().plus(discountTotal).getAmount().doubleValue();
		if(Math.abs(Math.round(calculatedPercentage) - calculatedPercentage) < 0.01){
			return String.format(Locale.UK, "%.0f", calculatedPercentage);
		} else {
			return String.format(Locale.UK, "%.1f", calculatedPercentage);
		}
	}

	public Money getFudgedReceiptTotal() {
		if(getOverPayments().isZero()) return getTotal();
		return getTotal().plus(getOverPayments());
	}
}
