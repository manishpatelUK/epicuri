package uk.co.epicuri.serverapi.common.pojo.host;

import uk.co.epicuri.serverapi.common.pojo.model.restaurant.IndividualStaffPermission;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffPermissions;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.WaiterAppFeature;

import java.util.ArrayList;
import java.util.List;

public class StaffPermissionView {
    private StaffRole role;
    private String roleReadableName;
    private List<WaiterAppFeatureView> booleanCapabilities = new ArrayList<>();

    public StaffPermissionView(){}
    public StaffPermissionView(IndividualStaffPermission individualStaffPermission){
        this.role = individualStaffPermission.getRole();
        this.roleReadableName = individualStaffPermission.getRole().getReadableName();
        for(WaiterAppFeature feature : WaiterAppFeature.values()) {
            WaiterAppFeatureView waiterAppFeatureView = new WaiterAppFeatureView(feature, individualStaffPermission.getPermissions().getOrDefault(feature, true));
            booleanCapabilities.add(waiterAppFeatureView);
        }
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public String getRoleReadableName() {
        return roleReadableName;
    }

    public void setRoleReadableName(String roleReadableName) {
        this.roleReadableName = roleReadableName;
    }

    public List<WaiterAppFeatureView> getBooleanCapabilities() {
        return booleanCapabilities;
    }

    public void setBooleanCapabilities(List<WaiterAppFeatureView> booleanCapabilities) {
        this.booleanCapabilities = booleanCapabilities;
    }
}
