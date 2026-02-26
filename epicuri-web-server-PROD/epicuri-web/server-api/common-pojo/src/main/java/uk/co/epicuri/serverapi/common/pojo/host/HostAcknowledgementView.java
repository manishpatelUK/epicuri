package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostAcknowledgementView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("NotificationId")
    private String notificationId;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("Time")
    private long time; //was double, in seconds

    public HostAcknowledgementView(Notification notification) {
        this.id = notification.getId();
        this.notificationId = notification.getId();
        this.sessionId = notification.getSessionId();
        this.time = notification.getAcknowledged() / 1000;
    }

    public HostAcknowledgementView() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
