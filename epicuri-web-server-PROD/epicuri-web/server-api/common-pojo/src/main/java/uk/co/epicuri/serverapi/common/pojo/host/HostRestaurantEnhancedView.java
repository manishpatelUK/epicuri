package uk.co.epicuri.serverapi.common.pojo.host;

import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.WaiterAppFeature;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;

import java.util.Map;

public class HostRestaurantEnhancedView extends HostRestaurantView {
    private String staffFacingId;
    private String internalEmailAddress;

    public HostRestaurantEnhancedView(){}

    public HostRestaurantEnhancedView(Restaurant restaurant, Map<String,AdjustmentType> adjustmentTypeMap, String apiURL, Map<WaiterAppFeature,Boolean> permissions){
        super(restaurant,adjustmentTypeMap,apiURL,permissions);
        this.staffFacingId = restaurant.getStaffFacingId();
        this.internalEmailAddress = restaurant.getInternalEmailAddress();
    }
}
