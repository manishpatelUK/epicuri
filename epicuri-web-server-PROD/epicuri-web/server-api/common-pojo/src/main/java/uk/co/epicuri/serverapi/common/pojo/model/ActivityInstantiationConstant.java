package uk.co.epicuri.serverapi.common.pojo.model;

public enum ActivityInstantiationConstant {
    UNKNOWN(-1),
    WAITER(0),
    IOS(1),
    ANDROID(2),
    CUSTOMER(3), // when os not known
    BOOKING_WIDGET(4),
    ONLINE(5);

    private final int id;

    ActivityInstantiationConstant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ActivityInstantiationConstant valueOf(int id) {
        for(ActivityInstantiationConstant constant : values()) {
            if(constant.getId() == id) {
                return constant;
            }
        }

        return UNKNOWN;
    }

    public static boolean fromWaiter(ActivityInstantiationConstant activityInstantiationConstant) {
        return activityInstantiationConstant == WAITER;
    }

    public static boolean fromCustomer(ActivityInstantiationConstant activityInstantiationConstant) {
        return activityInstantiationConstant == ANDROID
                || activityInstantiationConstant == IOS
                || activityInstantiationConstant == CUSTOMER
                || activityInstantiationConstant == BOOKING_WIDGET
                || activityInstantiationConstant == ONLINE;
    }

    public static boolean isSelfService(ActivityInstantiationConstant activityInstantiationConstant) {
        return activityInstantiationConstant == IOS
                || activityInstantiationConstant == ANDROID
                || activityInstantiationConstant == CUSTOMER
                || activityInstantiationConstant == ONLINE
                || activityInstantiationConstant == BOOKING_WIDGET;
    }
}
