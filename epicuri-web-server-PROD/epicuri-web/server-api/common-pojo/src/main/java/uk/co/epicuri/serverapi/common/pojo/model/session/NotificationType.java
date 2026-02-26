package uk.co.epicuri.serverapi.common.pojo.model.session;

/**
 * Created by manish
 */
public enum NotificationType {
    NONE(""),
    ADHOC("Adhoc"),
    RECURRING("Recurring"),
    SCHEDULED("Notification");

    private final String waiterAppName;

    NotificationType(String waiterAppName) {
        this.waiterAppName = waiterAppName;
    }

    public String getWaiterAppName() {
        return waiterAppName;
    }
}
