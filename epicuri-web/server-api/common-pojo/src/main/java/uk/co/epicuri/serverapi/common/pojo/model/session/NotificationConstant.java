package uk.co.epicuri.serverapi.common.pojo.model.session;

public enum NotificationConstant {
    TEXT_SERVICE_CALL("Service call"),
    TEXT_BILL_REQUEST("Requested Bill"),
    TARGET_WAITER_ACTION("waiter/action");

    private final String text;
    NotificationConstant(String text) {
        this.text = text;
    }

    public String getConstant() {
        return text;
    }
}
