package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.List;

@Document(collection = TableNames.SESSION_ARCHIVE)
public class SessionArchive extends IDAble{
    @Indexed
    private String restaurantId;

    @Indexed
    private String sessionId;

    @Indexed
    private long time = System.currentTimeMillis();

    @Indexed
    private long startTime;

    @Indexed(sparse = true)
    private Long closedTime;

    private Session session;
    private Service service;
    private Party party;
    private List<Batch> batches;
    private List<Order> orders;
    private List<Notification> notifications;
    private List<CheckIn> checkIns;

    public SessionArchive(){}

    public SessionArchive(Session session) {
        this.session = session;
        updateSessionMetadata(session);
        this.sessionId = session.getId();
        this.restaurantId = session.getRestaurantId();
    }

    private void updateSessionMetadata(Session session) {
        this.startTime = session.getStartTime();
        this.closedTime = session.getClosedTime();
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
        updateSessionMetadata(session);
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public List<Batch> getBatches() {
        return batches;
    }

    public void setBatches(List<Batch> batches) {
        this.batches = batches;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<CheckIn> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(List<CheckIn> checkIns) {
        this.checkIns = checkIns;
    }
}
