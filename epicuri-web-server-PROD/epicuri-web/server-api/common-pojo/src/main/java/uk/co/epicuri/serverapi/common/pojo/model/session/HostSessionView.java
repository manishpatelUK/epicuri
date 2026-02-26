package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostSessionView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ReadableId")
    private String readableId;

    @JsonProperty("SessionType")
    private String sessionType;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("ServiceName")
    private String serviceName;

    @JsonProperty("IsAdHoc")
    private Boolean adhoc;

    @JsonProperty("isRefund")
    private boolean refund;

    @JsonProperty("RequestedBill")
    private boolean billRequested;

    @JsonProperty("Void")
    private boolean voided;

    @JsonProperty("Delay")
    private long delay;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("DeliveryAddress")
    private Address deliveryAddress;

    @JsonProperty("StartTime")
    private long startTime;

    @JsonProperty("ClosedTime")
    private long closedTime;

    @JsonProperty("MenuId")
    private String menuId;

    @JsonProperty("Diner")
    private HostDinerView customer;

    @JsonProperty("Telephone")
    private String telephone;

    @JsonProperty("PartyName")
    private String partyName;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ExpectedTime")
    private Long expectedTime; //in seconds

    @JsonProperty("TakeawayMenuId")
    private String takeawayMenuId;

    @JsonProperty("Tables")
    private List<HostTableView> tables = new ArrayList<>();

    @JsonProperty("Diners")
    private List<HostDinerView> diners = new ArrayList<>();

    @JsonProperty("Orders")
    private List<HostOrderView> orders = new ArrayList<>();

    @JsonProperty("ScheduleItems")
    private List<ScheduledNotificationView> scheduledEvents = new ArrayList<>();

    @JsonProperty("RecurringScheduleItems")
    private List<RecurringNotificationView> recurringEvents = new ArrayList<>();

    @JsonProperty("AdhocNotifications")
    private List<HostNotificationView> adhocEvents = new ArrayList<>();

    @JsonProperty("Adjustments")
    private List<HostAdjustmentView> adjustments = new ArrayList<>();

    @JsonProperty("Accepted")
    private Boolean accepted;

    @JsonProperty("RejectionNotice")
    private String rejectionNotice;

    @JsonProperty("Rejected")
    private Boolean rejected;

    @JsonProperty("Deleted")
    private Boolean deleted;

    @JsonProperty("TipTotal")
    private double tipTotal;

    @JsonProperty("Tips")
    private double tips;

    @JsonProperty("Paid")
    private boolean paid;

    @JsonProperty("SubTotal")
    private double subTotal;

    @JsonProperty("Total")
    private double total;

    @JsonProperty("RemainingTotal")
    private double remainingTotal;

    @JsonProperty("OverPayments")
    private double overPayments;

    @JsonProperty("Change")
    private double change;

    @JsonProperty("DiscountTotal")
    private double discountTotal;

    @JsonProperty("DeliveryCost")
    private Double deliveryCost;

    @JsonProperty("VATTotal")
    private double vatTotal;

    @JsonProperty("totalPayment")
    private double totalPayment;

    @JsonProperty("VoidReason")
    private VoidReasonPayload voidReason;

    @JsonProperty("ChairData")
    private String chairData = "";

    @JsonProperty("courseAwayMessagesSent")
    private Map<String,Integer> courseAwayMessagesSent = new HashMap<>();

    public HostSessionView(){}

    public HostSessionView(Session session,
                           Collection<Order> orders,
                           List<Table> tables,
                           List<Notification> notifications,
                           long calculatedSessionDelayInMillis,
                           Map<String,RestaurantDefault> defaultMap,
                           Map<CalculationKey,Number> calculatedValues,
                           List<Customer> allCustomers,
                           Customer takeawayCustomer,
                           Map<String,Staff> allStaff,
                           Map<String,Preference> allPreferences,
                           BillSplit billSplit,
                           boolean isPaid) {
        setCommonProperties(session, calculatedValues, allStaff, isPaid);
        this.orders = orders.stream().map(o -> new HostOrderView(o,session.getService())).collect(Collectors.toList());
        this.diners = session.getDiners().stream().map(d ->
                new HostDinerView(
                        d,
                        StringUtils.isBlank(d.getCustomerId()) ? null : allCustomers.stream().filter(c -> d.getCustomerId().equals(c.getId())).findFirst().orElse(null),
                        allPreferences,
                        orders,
                        defaultMap.get(FixedDefaults.BIRTHDAY_TIMESPAN),
                        session.getService(),
                        billSplit
                )).collect(Collectors.toList());

        this.tables = tables.stream().map(t -> new HostTableView(t, true)).collect(Collectors.toList());
        this.delay = calculatedSessionDelayInMillis / 1000;
        if(session.getOriginalParty() != null) {
            this.partyName = session.getOriginalParty().getName();
        }

        if(notifications != null && notifications.size() > 0) {
            Map<NotificationType,List<Notification>> notificationTypeListMap = notifications.stream().collect(Collectors.groupingBy(Notification::getNotificationType));
            if(notificationTypeListMap.containsKey(NotificationType.RECURRING)) {
                this.recurringEvents = notificationTypeListMap.get(NotificationType.RECURRING).stream().map(n -> new RecurringNotificationView(n, notifications, session.getService())).collect(Collectors.toList());
            }
            if(notificationTypeListMap.containsKey(NotificationType.SCHEDULED)) {
                this.scheduledEvents = notificationTypeListMap.get(NotificationType.SCHEDULED).stream().map(n -> new ScheduledNotificationView(n, notifications, session)).collect(Collectors.toList());
            }
            if(notificationTypeListMap.containsKey(NotificationType.ADHOC)) {
                this.adhocEvents = notificationTypeListMap.get(NotificationType.ADHOC).stream().map(HostNotificationView::new).collect(Collectors.toList());
            }
        }

        if(recurringEvents.size() == 0) {
            recurringEvents = null;
        }
        if(scheduledEvents.size() == 0) {
            scheduledEvents = null;
        }
        if(adhocEvents.size() == 0) {
            adhocEvents = null;
        }

        if(session.getSessionType() == SessionType.TAKEAWAY) {
            if(session.getCalculatedDeliveryCost() != null) {
                this.deliveryCost = MoneyService.toMoneyRoundNearest(session.getCalculatedDeliveryCost());
            } else {
                this.deliveryCost = 0D;
            }
            this.takeawayMenuId = menuId;
            this.tables = null;
            this.chairData = null;

            this.customer = new HostDinerView(session.getDiners().get(0),
                    takeawayCustomer,
                    allPreferences,
                    orders,
                    defaultMap.get(FixedDefaults.BIRTHDAY_TIMESPAN),
                    session.getService(),
                    billSplit);
        }
    }

    private void setCommonProperties(Session session, Map<CalculationKey,Number> calculatedValues, Map<String,Staff> allStaff, boolean isPaid) {
        this.id = session.getId();
        if(session.getReadableId() != null) {
            readableId = session.getReadableId();
        }
        this.adhoc = session.getSessionType() == SessionType.ADHOC;
        this.refund = session.getSessionType() == SessionType.REFUND;
        this.billRequested = session.isBillRequested();
        sessionType = convertSessionType(session);

        this.name = session.getName();

        if(session.getChairData().size() > 0) {
            try {
                this.chairData = ControllerUtil.OBJECT_MAPPER.writeValueAsString(session.getChairData());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        this.startTime = session.getStartTime() / 1000;
        if(session.getClosedTime() != null) {
            closedTime = session.getClosedTime() / 1000;
        }
        this.paid = session.isMarkedAsPaid() && isPaid;
        if(session.getTipPercentage() != null) {
            this.tipTotal = session.getTipPercentage();
        }
        if(calculatedValues.containsKey(CalculationKey.TIP_TOTAL)) {
            this.tips = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TIP_TOTAL).intValue());
        }
        this.adjustments = session.getAdjustments().stream().filter(a -> !a.isVoided()).map(HostAdjustmentView::new).collect(Collectors.toList());
        if(session.getVoidReason() != null) {
            VoidReason voidReason = session.getVoidReason();
            this.voided = true;
            this.voidReason = new VoidReasonPayload(voidReason.getDescription(), voidReason.getTime(), allStaff.get(voidReason.getStaffId()));
        }

        if(session.getDeleted() != null) {
            this.deleted = true;
        }

        Service service = session.getService();
        if(service != null) {
            this.serviceId = service.getId();
            this.serviceName = service.getName();
            this.menuId = service.getDefaultMenuId();
        } else {
            this.serviceName = ""; //shouldn't be null
        }

        Booking booking = session.getOriginalBooking();
        if(booking != null) {
            this.expectedTime = booking.getTargetTime() / 1000;
            this.deliveryAddress = booking.getDeliveryAddress();
            this.telephone = booking.getTelephone();
            this.message = booking.getNotes();
            this.accepted = booking.isAccepted();
            this.rejected = booking.isRejected();
            this.rejectionNotice = booking.getRejectionNotice();
        } else if (session.getSessionType() == SessionType.TAKEAWAY) {
            //if there is no booking and it's a takeaway (i.e. created on waiter app) then set accepted etc to true
            this.accepted = true;
            this.rejected = false;
        }

        this.subTotal = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.SUB_TOTAL).intValue());
        this.total = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TOTAL).intValue());
        this.remainingTotal = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.REMAINING_TOTAL).intValue());
        this.overPayments = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.OVER_PAYMENTS).intValue());
        this.change = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.CHANGE_DUE).intValue());
        this.discountTotal = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.DISCOUNT_TOTAL).intValue());
        this.vatTotal = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.VAT_TOTAL).intValue());
        this.totalPayment = MoneyService.toMoneyRoundNearest(calculatedValues.get(CalculationKey.TOTAL_PAYMENTS).intValue());

        this.courseAwayMessagesSent = session.getCourseAwayMessagesSent();
    }

    public static String convertSessionType(Session session) {
        String sessionType = "None";
        if(session.getSessionType() == SessionType.SEATED) {
            sessionType = "Seated";
        } else if(session.getSessionType() == SessionType.TAKEAWAY && session.getTakeawayType() == TakeawayType.COLLECTION) {
            sessionType = "Collection";
        } else if(session.getSessionType() == SessionType.TAKEAWAY && session.getTakeawayType() == TakeawayType.DELIVERY) {
            sessionType = "Delivery";
        } else if(session.getSessionType() == SessionType.ADHOC) {
            sessionType = "Seated"; //todo this is retarded and should be fixed one day
        } else if(session.getSessionType() == SessionType.TAB) {
            sessionType = "Seated"; //todo this is retarded and should be fixed one day
        } else if(session.getSessionType() == SessionType.REFUND) {
            sessionType = "Seated"; //todo this is retarded and should be fixed one day
        }
        return sessionType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getAdhoc() {
        return adhoc;
    }

    public void setAdhoc(Boolean adhoc) {
        this.adhoc = adhoc;
    }

    public boolean isBillRequested() {
        return billRequested;
    }

    public void setBillRequested(boolean billRequested) {
        this.billRequested = billRequested;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(long closedTime) {
        this.closedTime = closedTime;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public HostDinerView getCustomer() {
        return customer;
    }

    public void setCustomer(HostDinerView customer) {
        this.customer = customer;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(Long expectedTime) {
        this.expectedTime = expectedTime;
    }

    public String getTakeawayMenuId() {
        return takeawayMenuId;
    }

    public void setTakeawayMenuId(String takeawayMenuId) {
        this.takeawayMenuId = takeawayMenuId;
    }

    public List<HostTableView> getTables() {
        return tables;
    }

    public void setTables(List<HostTableView> tables) {
        this.tables = tables;
    }

    public List<HostDinerView> getDiners() {
        return diners;
    }

    public void setDiners(List<HostDinerView> diners) {
        this.diners = diners;
    }

    public List<HostOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<HostOrderView> orders) {
        this.orders = orders;
    }

    public List<ScheduledNotificationView> getScheduledEvents() {
        return scheduledEvents;
    }

    public void setScheduledEvents(List<ScheduledNotificationView> scheduledEvents) {
        this.scheduledEvents = scheduledEvents;
    }

    public List<RecurringNotificationView> getRecurringEvents() {
        return recurringEvents;
    }

    public void setRecurringEvents(List<RecurringNotificationView> recurringEvents) {
        this.recurringEvents = recurringEvents;
    }

    public List<HostNotificationView> getAdhocEvents() {
        return adhocEvents;
    }

    public void setAdhocEvents(List<HostNotificationView> adhocEvents) {
        this.adhocEvents = adhocEvents;
    }

    public List<HostAdjustmentView> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<HostAdjustmentView> adjustments) {
        this.adjustments = adjustments;
    }

    public Boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public String getRejectionNotice() {
        return rejectionNotice;
    }

    public void setRejectionNotice(String rejectionNotice) {
        this.rejectionNotice = rejectionNotice;
    }

    public Boolean isRejected() {
        return rejected;
    }

    public void setRejected(Boolean rejected) {
        this.rejected = rejected;
    }

    public Boolean getRejected() {
        return rejected;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public double getTipTotal() {
        return tipTotal;
    }

    public void setTipTotal(double tipTotal) {
        this.tipTotal = tipTotal;
    }

    public double getTips() {
        return tips;
    }

    public void setTips(double tips) {
        this.tips = tips;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getRemainingTotal() {
        return remainingTotal;
    }

    public void setRemainingTotal(double remainingTotal) {
        this.remainingTotal = remainingTotal;
    }

    public double getOverPayments() {
        return overPayments;
    }

    public void setOverPayments(double overPayments) {
        this.overPayments = overPayments;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(double discountTotal) {
        this.discountTotal = discountTotal;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public double getVatTotal() {
        return vatTotal;
    }

    public void setVatTotal(double vatTotal) {
        this.vatTotal = vatTotal;
    }

    public VoidReasonPayload getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(VoidReasonPayload voidReason) {
        this.voidReason = voidReason;
    }

    public String getChairData() {
        return chairData;
    }

    public void setChairData(String chairData) {
        this.chairData = chairData;
    }

    public String getReadableId() {
        return readableId;
    }

    public void setReadableId(String readableId) {
        this.readableId = readableId;
    }

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Map<String, Integer> getCourseAwayMessagesSent() {
        return courseAwayMessagesSent;
    }

    public void setCourseAwayMessagesSent(Map<String, Integer> courseAwayMessagesSent) {
        this.courseAwayMessagesSent = courseAwayMessagesSent;
    }

    public boolean isRefund() {
        return refund;
    }

    public void setRefund(boolean refund) {
        this.refund = refund;
    }
}
