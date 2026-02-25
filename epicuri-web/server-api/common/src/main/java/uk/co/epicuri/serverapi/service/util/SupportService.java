package uk.co.epicuri.serverapi.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.List;

@Service
public class SupportService {
    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    public void sendLogFile(String restautantId, List<String> logLines) {
        StringBuilder builder = new StringBuilder();
        for(String string : logLines) {
            builder.append(string).append("\\r");
        }

        Restaurant restaurant = masterDataService.getRestaurant(restautantId);
        String email = restaurant.getInternalEmailAddress();
        if(StringUtils.isBlank(restaurant.getInternalEmailAddress())) {
            email = restaurant.getPublicEmailAddress();
        }

        asyncCommunicationsService.sendInternalSupportEmail(email, "LOG FILE DUMP: " + restaurant.getId() + ": " + restaurant.getName(), builder.toString());
    }
}
