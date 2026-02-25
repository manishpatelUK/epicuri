package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.StaffAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.IndividualStaffPermission;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.WaiterAppFeature;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = "/Authentication", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    public AuthenticationController() { }

    @CrossOrigin
    @RequestMapping(value = "/Login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestHeader(value = Params.HEAD_IP, required = false) String ip,
                                   @RequestHeader(Params.HEADER_EPICURI_API) String api,
                                   @RequestHeader(value = Params.HEADER_MAC, required = false) String mac,
                                   @RequestHeader(value = Params.HEADER_SSID, required = false) String ssid,
                                   @RequestHeader(value = Params.HEADER_PORTAL, required = false) String portal,
                                   @NotNull @RequestBody StaffAuthPayload staffAuthPayload) {
        LOGGER.trace("Attempt login {}", staffAuthPayload);
        if(!validate(staffAuthPayload)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid model");
        }

        Restaurant restaurant = masterDataService.getRestaurantByStaffFacingId(staffAuthPayload.getRestaurantId());
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if(portal != null && portal.equals("true")) {
            Staff staff = masterDataService.getStaff(staffAuthPayload.getUsername(), staffAuthPayload.getRestaurantId());
            if(staff == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            List<IndividualStaffPermission> permissions = restaurant.getStaffPermissions().getPermissions();
            if(permissions != null) {
                IndividualStaffPermission portalPermission = permissions.stream().filter(p -> p.getRole() == staff.getRole()).findFirst().orElse(null);
                if(portalPermission != null) {
                    if(!portalPermission.getPermissions().get(WaiterAppFeature.PORTAL)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User account does not allow access to Portal");
                    }
                }
            }
        }

        StaffView staff = authenticationService.staffLogin(staffAuthPayload);
        LOGGER.trace("Got staff {}", staff);
        if(staff != null) {
            authenticationService.updateAuditLog(ip, api, mac, ssid, staffAuthPayload.getRestaurantId());
            return ResponseEntity.ok(staff);
        }
        else {
            LOGGER.trace("Staff was null, throw NOT_FOUND");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/LoginAdmin", method = RequestMethod.POST)
    public ResponseEntity<?> loginAdmin(@NotNull @RequestHeader(Params.ADMIN_U) String username,
                                        @NotNull @RequestHeader(Params.AUTHORIZATION) String password) {
        IdPojo idPojo = authenticationService.adminLogin(username, password);
        if(idPojo != null) {
            return ResponseEntity.ok(idPojo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authenticated");
        }
    }

    private boolean validate(StaffAuthPayload payload) {
        return StringUtils.isNotBlank(payload.getPassword())
                && StringUtils.isNotBlank(payload.getRestaurantId())
                && StringUtils.isNotBlank(payload.getUsername());
    }
}
