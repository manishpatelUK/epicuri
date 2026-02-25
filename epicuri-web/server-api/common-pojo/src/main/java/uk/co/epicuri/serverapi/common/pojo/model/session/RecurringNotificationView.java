package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.host.HostNotificationView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringNotificationView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("InitialDelay")
    private long initialDelay; //in seconds

    @JsonProperty("Notifications")
    private List<HostNotificationView> notifications = new ArrayList<>();

    @JsonProperty("Period")
    private long period; //in seconds

    public RecurringNotificationView(){}
    public RecurringNotificationView(Notification notification, List<Notification> notifications, Service service) {
        this.id = notification.getScheduledItemId();
        this.serviceId = service.getId();
        ScheduledItem item = service.getSchedule().getRecurringItems().stream().filter(s -> s.getId().contains(notification.getScheduledItemId())).findFirst().orElse(null);
        if(item != null) {
            this.initialDelay = item.getInitialDelay() / 1000;
            this.period = item.getRecurring() / 1000;
            this.notifications.add(new HostNotificationView(item, notifications));
        } else {
            this.initialDelay = 0;
            this.period = 0;
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

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        initialDelay = initialDelay;
    }

    public List<HostNotificationView> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<HostNotificationView> notifications) {
        this.notifications = notifications;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
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
