package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostTableView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Table", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class TableController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private MasterDataService masterDataService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getTables(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Table> tables = masterDataService.getTables(restaurantId);

        return ResponseEntity.ok(tables.stream().map(t -> new HostTableView(t, false)).collect(Collectors.toList()));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postTable(@RequestHeader(Params.AUTHORIZATION) String token,
                                       @NotNull @RequestBody HostTableView tableView) {
        String restaurantId = authenticationService.getRestaurantId(token);

        if(StringUtils.isBlank(tableView.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant != null && !restaurant.isTablesEnabled()) {
            Staff staff = masterDataService.getStaff(authenticationService.getStaffId(token));
            if(staff.getRole() != StaffRole.EPICURI_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        if(restaurant != null && restaurant.getTables().stream().anyMatch(t -> t.getName() != null && t.getName().equalsIgnoreCase(tableView.getName().trim()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Table table = new Table(restaurantId,tableView);
        masterDataService.addTable(restaurantId,table);

        return ResponseEntity.ok(new HostTableView(table, true));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putTable(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @PathVariable("id") String id,
                                      @NotNull @RequestBody HostTableView tableView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if(!restaurant.isTablesEnabled()) {
            Staff staff = masterDataService.getStaff(authenticationService.getStaffId(token));
            if(staff.getRole() != StaffRole.EPICURI_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Table original = restaurant.getTables().stream().filter(t -> t.getId() != null && t.getId().equals(id)).findAny().orElse(null);
        if(original == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Table not found");
        }

        Table table = new Table(tableView);
        if(tableView.getPosition() == null) {
            table.setPosition(original.getPosition());
        }
        table.setId(id);
        masterDataService.upsert(restaurantId, table);

        return ResponseEntity.ok(new HostTableView(table, true));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteTable(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<String> tables = liveDataService.tablesInUse(restaurantId);
        if(tables.contains(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Table in use");
        }

        masterDataService.deleteTable(restaurantId,id);

        return ResponseEntity.noContent().build();
    }
}
