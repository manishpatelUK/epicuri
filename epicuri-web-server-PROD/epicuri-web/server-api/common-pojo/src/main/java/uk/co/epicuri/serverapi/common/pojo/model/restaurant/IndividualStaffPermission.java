package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import java.util.HashMap;
import java.util.Map;

public class IndividualStaffPermission {
    private StaffRole role;
    private Map<WaiterAppFeature, Boolean> permissions = new HashMap<>();

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public Map<WaiterAppFeature, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<WaiterAppFeature, Boolean> permissions) {
        this.permissions = permissions;
    }
}
