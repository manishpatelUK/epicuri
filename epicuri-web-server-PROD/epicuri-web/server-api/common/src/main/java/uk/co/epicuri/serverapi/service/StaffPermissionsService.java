package uk.co.epicuri.serverapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class StaffPermissionsService {

    @Autowired
    private MasterDataService masterDataService;

    public StaffPermissions getPermissions(String restaurantId) {
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant == null) {
            return null;
        }

        return getPermissions(restaurant);
    }

    public StaffPermissions getPermissions(Restaurant restaurant) {
        StaffPermissions permissions = restaurant.getStaffPermissions();
        if(permissions == null) {
            permissions = MasterDataCreationService.createDefaultPermissions(restaurant.getId());
            restaurant.setStaffPermissions(permissions);
            masterDataService.upsert(restaurant);
        }

        return permissions;
    }

    public static Map<WaiterAppFeature,Boolean> getIndividualPermissions(StaffPermissions staffPermissions, StaffRole staffRole) {
        Map<WaiterAppFeature,Boolean> permissions = new HashMap<>();
        for(IndividualStaffPermission individualStaffPermission : staffPermissions.getPermissions()) {
            if(individualStaffPermission.getRole() == staffRole) {
                permissions = new HashMap<>(individualStaffPermission.getPermissions());
                break;
            }
        }

        return permissions;
    }

    public void upsert(StaffPermissions staffPermissions) {
        if(staffPermissions.getRestaurantId() == null) {
            return;
        }

        Restaurant restaurant = masterDataService.getRestaurant(staffPermissions.getRestaurantId());
        restaurant.setStaffPermissions(staffPermissions);
        masterDataService.upsert(restaurant);
    }
}
