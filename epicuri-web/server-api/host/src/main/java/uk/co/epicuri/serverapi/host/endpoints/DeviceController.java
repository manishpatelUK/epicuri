package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.AndroidLogPojo;
import uk.co.epicuri.serverapi.common.pojo.host.DeviceRequest;
import uk.co.epicuri.serverapi.common.pojo.model.DeviceDetail;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.util.SupportService;

import javax.validation.constraints.NotNull;

/**
 * Created by manish
 */
@RestController
@RequestMapping(value = "device", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class DeviceController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SupportService supportService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postUpdateDevice(@RequestHeader(Params.AUTHORIZATION) String token,
                                              @NotNull @RequestBody DeviceRequest request) {
        String restaurantId = StringUtils.isBlank(request.getRestaurantId()) ?
                authenticationService.getRestaurantId(token) : request.getRestaurantId();
        request.setRestaurantId(restaurantId);

        liveDataService.upsert(new DeviceDetail(request));

        return ResponseEntity.accepted().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "logs", method = RequestMethod.POST)
    public ResponseEntity<?> postLogs(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @NotNull @RequestBody AndroidLogPojo request) {
        if(request.getLogs() == null || request.getLogs().size() == 0) {
            return ResponseEntity.ok().build();
        }

        supportService.sendLogFile(authenticationService.getRestaurantId(token), request.getLogs());

        return ResponseEntity.ok().build();
    }
}
