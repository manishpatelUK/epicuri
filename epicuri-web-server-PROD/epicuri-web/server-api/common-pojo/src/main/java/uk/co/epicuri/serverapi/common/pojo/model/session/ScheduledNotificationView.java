package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.host.HostNotificationView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Schedule;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledNotificationView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("Delay")
    private long delay; //in seconds

    @JsonProperty("Notifications")
    private List<HostNotificationView> notifications = new ArrayList<>();

    public ScheduledNotificationView(){}
    public ScheduledNotificationView(Notification notification, List<Notification> notifications, Session session) {
        this.id = notification.getScheduledItemId();
        if(session.getService() != null) {
            this.serviceId = session.getService().getId();
        }
        this.delay = (System.currentTimeMillis() - notification.getTime()) / 1000;

        Service service = session.getService();
        if(service != null && service.getSchedule() != null) {
            Schedule schedule = service.getSchedule();
            List<ScheduledItem> items = schedule.getScheduledItems().stream().filter(s -> s.getId().equals(notification.getScheduledItemId())).collect(Collectors.toList());
            this.notifications = items.stream().map(s -> new HostNotificationView(s, notifications)).collect(Collectors.toList());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public List<HostNotificationView> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<HostNotificationView> notifications) {
        this.notifications = notifications;
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
