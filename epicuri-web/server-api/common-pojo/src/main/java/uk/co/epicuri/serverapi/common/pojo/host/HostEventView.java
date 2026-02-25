package uk.co.epicuri.serverapi.common.pojo.host;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostEventView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Session")
    private String sessionId;

    @JsonProperty("Due")
    private long due; // was double

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Target")
    private String target;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Delay")
    public long delay;

    public HostEventView() {}
    public HostEventView(Notification notification, long delay) {
        this.id = notification.getId();
        this.sessionId = notification.getSessionId();
        this.due = notification.getTime() / 1000;
        this.text = notification.getText();
        this.target = notification.getTarget();
        this.type = notification.getNotificationType().getWaiterAppName();
        this.delay = delay / 1000;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getDue() {
        return due;
    }

    public void setDue(long due) {
        this.due = due;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
