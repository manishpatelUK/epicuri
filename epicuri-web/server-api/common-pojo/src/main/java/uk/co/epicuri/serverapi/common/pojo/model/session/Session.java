package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = TableNames.SESSIONS)
public class Session extends Deletable implements Comparable<Session> {
    @Indexed
    private String restaurantId;

    @Indexed
    private long startTime; //was double
    private long delay = 0; //calculated delay from notification acks/postpones

    private String name;

    @Indexed
    private Long closedTime;//was double
    private String closedBy;

    private boolean billRequested = false;
    private String cashUpId;
    private boolean removeFromReports = false;

    private VoidReason voidReason;

    private Service service;

    private SessionType sessionType = SessionType.NONE;
    private List<ChairData> chairData = new ArrayList<>();
    private Double tipPercentage;
    private List<String> tables = new ArrayList<>();

    private List<Adjustment> adjustments = new ArrayList<>();

    //takeaways only
    private Integer calculatedDeliveryCost;
    private TakeawayType takeawayType = TakeawayType.NONE;

    private List<Diner> diners = new ArrayList<>();
    private Map<String,Integer> courseAwayMessagesSent = new HashMap<>();

    @DBRef(lazy = true)
    private Party originalParty;

    @Indexed
    private String originalPartyId;

    @DBRef(lazy = true)
    private Booking originalBooking;

    @Indexed(sparse = true)
    private String originalBookingId;

    private String readableId;

    private boolean markedAsPaid;

    private List<String> linkedSession;

    public Session(){}

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(Long closedTime) {
        this.closedTime = closedTime;
    }

    public boolean isBillRequested() {
        return billRequested;
    }

    public void setBillRequested(boolean billRequested) {
        this.billRequested = billRequested;
    }

    public String getCashUpId() {
        return cashUpId;
    }

    public void setCashUpId(String cashUpId) {
        this.cashUpId = cashUpId;
    }

    public boolean isRemoveFromReports() {
        return removeFromReports;
    }

    public void setRemoveFromReports(boolean removeFromReports) {
        this.removeFromReports = removeFromReports;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public Double getTipPercentage() {
        return tipPercentage;
    }

    public void setTipPercentage(Double tipPercentage) {
        this.tipPercentage = tipPercentage;
    }

    public List<ChairData> getChairData() {
        return chairData;
    }

    public void setChairData(List<ChairData> chairData) {
        this.chairData = chairData;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<Adjustment> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<Adjustment> adjustments) {
        this.adjustments = adjustments;
    }

    public String getOriginalBookingId() {
        return originalBookingId;
    }

    public void setOriginalBookingId(String originalBookingId) {
        this.originalBookingId = originalBookingId;
    }

    public Integer getCalculatedDeliveryCost() {
        return calculatedDeliveryCost;
    }

    public void setCalculatedDeliveryCost(Integer calculatedDeliveryCost) {
        this.calculatedDeliveryCost = calculatedDeliveryCost;
    }

    public List<Diner> getDiners() {
        return diners;
    }

    public int getNumberOfRealDiners() { //excludes default diner
        if(sessionType == SessionType.TAKEAWAY || sessionType == SessionType.NONE || sessionType == SessionType.ADHOC || sessionType == SessionType.REFUND) {
            return 1;
        } else {
            return diners.size()-1;
        }
    }

    public Diner getFirstNonDefaultDiner() {
        return diners.stream().filter(d -> !d.isDefaultDiner()).findFirst().orElse(null);
    }

    public void setDiners(List<Diner> diners) {
        this.diners = diners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TakeawayType getTakeawayType() {
        return takeawayType;
    }

    public void setTakeawayType(TakeawayType takeawayType) {
        this.takeawayType = takeawayType;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Party getOriginalParty() {
        return originalParty;
    }

    public void setOriginalParty(Party originalParty) {
        if(originalParty == null) {
            this.originalParty = null;
            this.originalPartyId = null;
        } else {
            this.originalParty = originalParty;
            this.originalPartyId = originalParty.getId();
        }
    }

    public Booking getOriginalBooking() {
        return originalBooking;
    }

    public void setOriginalBooking(Booking originalBooking) {
        if(originalBooking == null) {
            this.originalBooking = null;
            this.originalBookingId = null;
        } else {
            this.originalBooking = originalBooking;
            this.originalBookingId = originalBooking.getId();
        }
    }

    public VoidReason getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(VoidReason voidReason) {
        this.voidReason = voidReason;
    }

    public String getOriginalPartyId() {
        return originalPartyId;
    }

    public void setOriginalPartyId(String originalPartyId) {
        this.originalPartyId = originalPartyId;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getReadableId() {
        return readableId;
    }

    public void setReadableId(String readableId) {
        this.readableId = readableId;
    }

    public boolean isMarkedAsPaid() {
        return markedAsPaid;
    }

    public void setMarkedAsPaid(boolean markedAsPaid) {
        this.markedAsPaid = markedAsPaid;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public List<String> getLinkedSession() {
        return linkedSession;
    }

    public void setLinkedSession(List<String> linkedSession) {
        this.linkedSession = linkedSession;
    }

    public boolean isLinked() {
        return getLinkedSession() != null && getLinkedSession().size() > 0;
    }

    public boolean isDeferred() {
        if(adjustments == null) return false;
        for(Adjustment adjustment : adjustments) {
            if(adjustment.getAdjustmentType() != null) {
                if(adjustment.getAdjustmentType().getName().equals(RestaurantConstants.DEFER_ADJUSTMENT)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, Integer> getCourseAwayMessagesSent() {
        return courseAwayMessagesSent;
    }

    public void setCourseAwayMessagesSent(Map<String, Integer> courseAwayMessagesSent) {
        this.courseAwayMessagesSent = courseAwayMessagesSent;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass() == getClass() && EqualsBuilder.reflectionEquals(this,object,"service","originalParty","originalBooking");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "service","originalParty","originalBooking");
    }

    @Override
    public int compareTo(Session o) {
        return Long.compare(startTime, o.startTime);
    }
}
