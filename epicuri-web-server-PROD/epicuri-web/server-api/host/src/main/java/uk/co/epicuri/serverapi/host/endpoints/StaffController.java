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
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.auth.HostLevelCheckRequired;
import uk.co.epicuri.serverapi.common.pojo.host.StaffPermissionView;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataCreationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.StaffPermissionsService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Staff", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class StaffController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private StaffPermissionsService staffPermissionsService;

    @Autowired
    private AuthenticationService authenticationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(StaffController.class);

    public StaffController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<StaffView>> getStaff(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Staff> staffs = masterDataService.getAllStaff(restaurantId).stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());

        return ResponseEntity.ok(staffs.stream().map(StaffView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postStaff(@RequestHeader(Params.AUTHORIZATION) String token,
                                       @NotNull @RequestBody StaffView staffView) {
        if(!validateOnNew(staffView)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid model");
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        List<Staff> current = masterDataService.getAllStaff(restaurantId).stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());
        if(current.stream().anyMatch(s -> s.getUserName() != null && s.getUserName().equalsIgnoreCase(staffView.getUsername()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff username already exists");
        }

        Staff staff = new Staff(staffView, restaurantId);
        staff.setMash(AuthenticationUtil.getPasswordMash(staff,staffView.getPassword()));

        staff = masterDataService.upsert(staff);

        return ResponseEntity.created(URI.create("/Staff/"+staff.getId())).build(); //waiter app doesn't expect staff but cpe returns it
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putStaff(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @PathVariable("id") String id,
                                      @NotNull @RequestBody StaffView staffView) {
        if(!validateOnEdit(staffView)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid model");
        }

        Staff staff = masterDataService.getStaff(id);
        if(staff == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found");
        }

        if(!staffView.getUsername().equalsIgnoreCase(staff.getUserName())) {
            String restaurantId = authenticationService.getRestaurantId(token);
            List<Staff> current = masterDataService.getAllStaff(restaurantId).stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());
            if(current.stream().anyMatch(s -> s.getUserName() != null && s.getUserName().equalsIgnoreCase(staffView.getUsername()))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Staff username already exists");
            }
        }

        staff.setName(staffView.getName());
        staff.setUserName(staffView.getUsername());
        if(StringUtils.isNotBlank(staffView.getPassword())) {
            staff.setMash(AuthenticationUtil.getPasswordMash(staff, staffView.getPassword()));
        }

        StaffRole securityLevelOfCaller = StaffRole.UNKNOWN;
        try {
            securityLevelOfCaller = authenticationService.getStaffSecurityLevel(token);
        } catch (Exception e) {
            //squelch
            LOGGER.warn("Could not get security level of token: {}", token);
        }

        if(StringUtils.isNotBlank(staffView.getRole())) {
            StaffRole staffRole = StaffRole.valueOf(staffView.getRole());
            if(staffRole != StaffRole.UNKNOWN && securityLevelOfCaller.isHigherOrEqualSecurityLevelThan(staffRole)) {
                staff.setRole(staffRole);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You do not have clearance to upgrade Role");
            }
        } else {
            //legacy
            if(staffView.isManager() && StaffRole.MANAGER.isHigherSecurityLevelThan(securityLevelOfCaller)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You do not have clearance to upgrade Role");
            } else {
                staff.setRole(staffView.isManager() ? StaffRole.MANAGER : StaffRole.WAIT_STAFF);
            }
        }
        if(StringUtils.isNotBlank(staffView.getPin())) {
            staff.setPin(staffView.getPin());
        }

        masterDataService.upsert(staff);

        return ResponseEntity.ok().build(); //waiter app doesn't expect staff but cpe returns it
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteStaff(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id) {
        Staff staffToDelete = masterDataService.getStaff(id);
        if(staffToDelete == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found");
        }

        //don't delete yourself
        if(id.equals(authenticationService.getStaffId(token))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete this user (user is logged in)");
        }

        //can't delete a person higher than you
        try {
            if(staffToDelete.getRole().isHigherSecurityLevelThan(authenticationService.getStaffSecurityLevel(token))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete this user (permission denied)");
            }
        } catch (Exception e) {
            LOGGER.warn("Could not obtain security level for token: {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Staff not found (internal error)");
        }

        masterDataService.softDeleteStaff(id);
        authenticationService.invalidateStaffLogin(id);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/permissions", method = RequestMethod.GET)
    public ResponseEntity<?> getStaffPermissions(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(restaurantId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        StaffPermissions permissions = staffPermissionsService.getPermissions(restaurantId);

        List<StaffPermissionView> response = new ArrayList<>();
        for(IndividualStaffPermission individualStaffPermission : permissions.getPermissions()) {
            if(individualStaffPermission.getRole() == StaffRole.EPICURI_ADMIN || individualStaffPermission.getRole() == StaffRole.UNKNOWN) {
                continue;
            }
            response.add(new StaffPermissionView(individualStaffPermission));
        }
        return ResponseEntity.ok(response);
    }

    @HostAuthRequired
    @RequestMapping(value = "/permissions", method = RequestMethod.PUT)
    public ResponseEntity<?> putStaffPermissions(@RequestHeader(Params.AUTHORIZATION) String token,
                                                 @RequestBody List<StaffPermissionView> permissionViews) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(restaurantId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        StaffPermissions existingPerms = staffPermissionsService.getPermissions(restaurantId);
        if(existingPerms == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        StaffPermissions staffPermissions = StaffPermissions.fromView(permissionViews);
        staffPermissions.setRestaurantId(restaurantId);
        //ensure epicuriadmin always has perms
        IndividualStaffPermission epicuriAdminPerms = staffPermissions.getPermissions().stream().filter(i -> i.getRole() == StaffRole.EPICURI_ADMIN).findFirst().orElse(null);
        if(epicuriAdminPerms == null) {
            epicuriAdminPerms = new IndividualStaffPermission();
            epicuriAdminPerms.setRole(StaffRole.EPICURI_ADMIN);
            epicuriAdminPerms.getPermissions().clear();
            StaffPermissions.setPermissionsTrue(epicuriAdminPerms);
            staffPermissions.getPermissions().add(0, epicuriAdminPerms);
        } else {
            StaffPermissions.setPermissionsTrue(epicuriAdminPerms);
        }

        //todo add an audit trail

        staffPermissionsService.upsert(staffPermissions);
        return ResponseEntity.ok().build();
    }

    private boolean validateOnNew(StaffView staffView) {
        return StringUtils.isNotBlank(staffView.getName())
                && StringUtils.isNotBlank(staffView.getPassword())
                && StringUtils.isNotBlank(staffView.getPin())
                && StringUtils.isNotBlank(staffView.getUsername());
    }

    private boolean validateOnEdit(StaffView staffView) {
        return StringUtils.isNotBlank(staffView.getName())
                && StringUtils.isNotBlank(staffView.getUsername());
    }

}
