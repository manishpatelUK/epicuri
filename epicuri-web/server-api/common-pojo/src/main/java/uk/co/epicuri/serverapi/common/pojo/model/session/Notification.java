package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.NOTIFICATIONS)
public class Notification extends Deletable {
    @Indexed
    private String restaurantId;

    @Indexed
    private long time;

    private String text;
    private String target;

    private Long acknowledged;
    private String sessionId;
    private String scheduledItemId;

    private NotificationType notificationType;

    //recurring only
    private long recurrence;

    public Notification() {}
    public Notification(ScheduledItem scheduledItem, Session session) {
        this.text = scheduledItem.getText();
        this.target = scheduledItem.getTarget();
        this.restaurantId = session.getRestaurantId();
        this.scheduledItemId = scheduledItem.getId();
        this.notificationType = scheduledItem.getNotificationType();
        long currentTime = System.currentTimeMillis();
        if(notificationType == NotificationType.RECURRING) {
            this.time = currentTime + scheduledItem.getInitialDelay() + scheduledItem.getRecurring();
            this.recurrence = scheduledItem.getRecurring();
        } else {
            this.time = currentTime + scheduledItem.getTimeAfterStart();
        }
        this.sessionId = session.getId();
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Long acknowledged) {
        this.acknowledged = acknowledged;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getScheduledItemId() {
        return scheduledItemId;
    }

    public void setScheduledItemId(String scheduledItemId) {
        this.scheduledItemId = scheduledItemId;
    }

    public long getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(long recurrence) {
        this.recurrence = recurrence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Notification that = (Notification) o;

        return EqualsBuilder.reflectionEquals(that, this);
    }

    @Override
    public int hashCode() {
        int result = restaurantId != null ? restaurantId.hashCode() : 0;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (acknowledged != null ? acknowledged.hashCode() : 0);
        result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
        result = 31 * result + (scheduledItemId != null ? scheduledItemId.hashCode() : 0);
        result = 31 * result + (notificationType != null ? notificationType.hashCode() : 0);
        result = 31 * result + (int) (recurrence ^ (recurrence >>> 32));
        return result;
    }
}
