package uk.co.epicuri.waiter.model;

/**
 * Created by manish on 23/03/2017.
 */

public class BadgesSingleton {
    private static int walkInCount = 0;
    private static int checkInsCount = 0;
    private static int nonTabbedSessions = 0;

    public static void setWalkInCount(int walkInCount) {
        BadgesSingleton.walkInCount = walkInCount;
    }

    public static void setCheckInsCount(int checkInsCount) {
        BadgesSingleton.checkInsCount = checkInsCount;
    }

    public static void setNonTabbedSessions(int nonTabbedSessions) {
        BadgesSingleton.nonTabbedSessions = nonTabbedSessions;
    }

    public static int getTabsBadgeNumber() {
        return walkInCount + checkInsCount + nonTabbedSessions;
    }
}
