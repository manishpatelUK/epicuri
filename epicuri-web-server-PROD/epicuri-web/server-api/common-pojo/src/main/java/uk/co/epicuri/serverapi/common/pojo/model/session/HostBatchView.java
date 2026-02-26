package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.host.HostOrderView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostBatchView {
    private static transient final String TYPE_TAKEAWAY = "Takeaway";
    private static transient final String TYPE_WAITER = "Seated";
    private static transient final String TYPE_COLLECTION = "Collection";
    private static transient final String TYPE_DELIVERY = "Delivery";
    private static transient final String TYPE_TAB = "Tab";
    private static transient final String TYPE_AD_HOC = "AdHoc";
    private static transient final String UNKNOWN_PARTY = "UNKNOWN PARTY"; //when no name on party/session

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Identifier")
    private String identifier; // one of the TYPE_*s

    @JsonProperty("Time")
    private long time; //was double // in seconds

    @JsonProperty("Tables")
    private List<String> tables = new ArrayList<>();

    @JsonProperty("Orders")
    private List<HostOrderView> orders = new ArrayList<>();

    @JsonProperty("Modify")
    private boolean modify = false;

    @JsonProperty("PrinterId")
    private String printerId;

    @JsonProperty("BatchType")
    private String batchType; //"Deliver" or "Collection"

    @JsonProperty("DueDate")
    private Long dueDate; // was double // in seconds

    @JsonProperty("ServerName")
    private String serverName;

    @JsonProperty("IsSelfService")
    private boolean selfService;

    @JsonProperty("Covers")
    private int covers;

    @JsonProperty("OrderName")
    private String orderName; //party/session name

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("SpoolTime")
    private long spoolTime; // was double //in seconds

    @JsonProperty("Type")
    private String type;

    @JsonProperty("staffUserName")
    private String staffUserName;

    private String publicFacingOrderId;
    private String deliveryLocation;

    private List<String> addressLines;

    public HostBatchView(){}
    public HostBatchView(Batch batch, Session session, List<Order> orders, Booking booking, Map<String,Course> courseMap, Restaurant restaurant, String staffUserName) {
        this.id = batch.getId();
        this.orderName = session.getName();
        if(session.getSessionType() == SessionType.ADHOC) {
            identifier = TYPE_AD_HOC;
            orderName = "Quick Order";
        } else if (session.getSessionType() == SessionType.SEATED) {
            identifier = TYPE_WAITER;
        } else if (session.getSessionType() == SessionType.TAKEAWAY) {
            identifier = TYPE_TAKEAWAY;
            if(booking != null) {
                dueDate = booking.getTargetTime() / 1000;
                if(booking.getTakeawayType() == TakeawayType.COLLECTION) {
                    batchType = TYPE_COLLECTION;
                } else if(booking.getTakeawayType() == TakeawayType.DELIVERY) {
                    batchType = TYPE_DELIVERY;
                    updateAddress(booking);
                }
            }
        } else if (session.getSessionType() == SessionType.TAB) {
            identifier = TYPE_TAB;
        } else {
            identifier = UNKNOWN_PARTY;
        }

        if(batchType == null) {
            //try to get it from the session
            if(session.getTakeawayType() != null && session.getTakeawayType() == TakeawayType.COLLECTION) {
                batchType = TYPE_COLLECTION;
            } else if(session.getTakeawayType() != null && session.getTakeawayType() == TakeawayType.DELIVERY) {
                batchType = TYPE_DELIVERY;
            }
        }

        this.time = batch.getCreationTime()/1000;
        this.covers = session.getNumberOfRealDiners();
        this.printerId = batch.getPrinterId();
        if(orders.stream().anyMatch(o -> ActivityInstantiationConstant.fromCustomer(o.getInstantiatedFrom()))) { // in future... orders could be mixed in 1 batch (but we should prevent that at batch creation time, really
            this.selfService = true;
        }

        tables = restaurant.getTables().stream().filter(t -> session.getTables().contains(t.getId())).map(Table::getName).collect(Collectors.toList());
        this.orders = orders.stream().map(o -> new HostOrderView(o,session.getService())).collect(Collectors.toList());

        orders.sort((o1, o2) -> {
            Course course1 = courseMap.getOrDefault(o1.getCourseId(), new Course());
            Course course2 = courseMap.getOrDefault(o2.getCourseId(), new Course());
            return Short.compare(course1.getOrdering(), course2.getOrdering());
        });

        this.staffUserName = staffUserName;

        if(orders.stream().filter(o -> o.getDeliveryLocation() != null).map(Order::getDeliveryLocation).distinct().count() == 1) {
            this.deliveryLocation = orders.get(0).getDeliveryLocation();
        }

        if(orders.stream().filter(o -> o.getPublicFacingOrderId() != null).map(Order::getPublicFacingOrderId).distinct().count() == 1) {
            this.publicFacingOrderId = orders.get(0).getPublicFacingOrderId();
        }

        if(batch.isDuplicate()) {
            if(this.orderName == null) {
                this.orderName = "";
            }
            this.orderName = this.orderName + " **DUPLICATE**";
        }
    }

    public void updateAddress(Booking booking) {
        Address deliveryAddress = booking.getDeliveryAddress();
        if(deliveryAddress != null) {
            addressLines = new ArrayList<>();
            if(StringUtils.isNotBlank(deliveryAddress.getStreet())) {
                addressLines.add(deliveryAddress.getStreet());
            }
            if(StringUtils.isNotBlank(deliveryAddress.getTown())) {
                addressLines.add(deliveryAddress.getTown());
            }
            if(StringUtils.isNotBlank(deliveryAddress.getCity())) {
                addressLines.add(deliveryAddress.getCity());
            }
            if(StringUtils.isNotBlank(deliveryAddress.getPostcode())) {
                addressLines.add(deliveryAddress.getPostcode());
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<HostOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<HostOrderView> orders) {
        this.orders = orders;
    }

    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isSelfService() {
        return selfService;
    }

    public void setIsSelfService(boolean isSelfService) {
        selfService = isSelfService;
    }

    public int getCovers() {
        return covers;
    }

    public void setCovers(int covers) {
        this.covers = covers;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getSpoolTime() {
        return spoolTime;
    }

    public void setSpoolTime(long spoolTime) {
        this.spoolTime = spoolTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStaffUserName() {
        return staffUserName;
    }

    public void setStaffUserName(String staffUserName) {
        this.staffUserName = staffUserName;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getPublicFacingOrderId() {
        return publicFacingOrderId;
    }

    public void setPublicFacingOrderId(String publicFacingOrderId) {
        this.publicFacingOrderId = publicFacingOrderId;
    }
}
