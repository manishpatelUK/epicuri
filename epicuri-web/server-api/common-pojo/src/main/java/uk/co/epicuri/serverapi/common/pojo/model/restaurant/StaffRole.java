package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

public enum StaffRole {
    UNKNOWN(Integer.MAX_VALUE,"UNKNOWN"),
    EPICURI_ADMIN(Integer.MIN_VALUE,"Epicuri Admin"),
    SUPER_ADMIN(10, "Super Admin"),
    SITE_OWNER(20, "Site Owner"),
    MANAGER(30, "Manager"),
    ASSISTANT_MANAGER(40, "Assistant Manager"),
    HOST_STAFF(50, "Host Staff"),
    WAIT_STAFF(50, "Wait Staff"),
    THIRD_PARTY(60, "3rd Party (External)");

    private final int securityLevel;
    private final String readableName;
    StaffRole() {
        this(Integer.MAX_VALUE, "UNKNOWN");
    }

    StaffRole(int securityLevel, String readableName) {
        this.securityLevel = securityLevel;
        this.readableName = readableName;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public boolean isHigherOrEqualSecurityLevelThan(StaffRole minimumLevel) {
        return isHigherOrEqualSecurityLevelThan(minimumLevel.securityLevel);
    }

    public boolean isHigherSecurityLevelThan(StaffRole minimumLevel) {
        return isHigherSecurityLevelThan(minimumLevel.securityLevel);
    }

    public boolean isHigherOrEqualSecurityLevelThan(int minimumLevel) {
        return securityLevel <= minimumLevel;
    }

    public boolean isHigherSecurityLevelThan(int minimumLevel) {
        return securityLevel < minimumLevel;
    }

    public String getReadableName() {
        return readableName;
    }
}
