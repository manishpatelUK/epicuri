package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import uk.co.epicuri.serverapi.common.pojo.host.StaffPermissionView;
import uk.co.epicuri.serverapi.common.pojo.host.WaiterAppFeatureView;

import java.util.ArrayList;
import java.util.List;

public class StaffPermissions {
    private String restaurantId;

    private List<IndividualStaffPermission> permissions = new ArrayList<>();

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<IndividualStaffPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<IndividualStaffPermission> permissions) {
        this.permissions = permissions;
    }

    public static StaffPermissions fromView(List<StaffPermissionView> staffPermissionViews) {
        StaffPermissions permissions = new StaffPermissions();
        for(StaffPermissionView view : staffPermissionViews) {
            IndividualStaffPermission individualStaffPermission = new IndividualStaffPermission();
            individualStaffPermission.setRole(view.getRole());
            for(WaiterAppFeatureView waiterAppFeatureView : view.getBooleanCapabilities()) {
                individualStaffPermission.getPermissions().put(waiterAppFeatureView.getCapability(), waiterAppFeatureView.isEnabled());
            }
            permissions.getPermissions().add(individualStaffPermission);
        }

        //check for missing permissions - default to false
        for(StaffRole role : StaffRole.values()) {
            boolean found = false;
            for(StaffPermissionView view : staffPermissionViews) {
                if(view.getRole() == role) {
                    found = true;
                    break;
                }
            }

            if(!found) {
                IndividualStaffPermission individualStaffPermission = new IndividualStaffPermission();
                individualStaffPermission.setRole(role);
                setPermissionsTrue(individualStaffPermission);
                permissions.getPermissions().add(individualStaffPermission);
            }
        }


        return permissions;
    }

    public static void setPermissionsTrue(IndividualStaffPermission epicuriAdminPerms) {
        for(WaiterAppFeature waiterAppFeature : WaiterAppFeature.values()) {
            epicuriAdminPerms.getPermissions().put(waiterAppFeature, true);
        }
    }
}
