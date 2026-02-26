package uk.co.epicuri.serverapi.host.endpoints;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.HostLayoutView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTableView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "Layout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class LayoutController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getLayout(@PathVariable("id") String id,
                                       @RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Floor> floors = masterDataService.getFloors(restaurantId);

        for(Floor floor : floors) {
            for(Layout layout : floor.getLayouts()) {
                if(layout.getId().equals(id)) {
                    return ResponseEntity.ok(new HostLayoutView(floor, layout, masterDataService.getTables(restaurantId, layout.getTables())));
                }
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postLayout(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @RequestBody @NotNull HostLayoutView layout) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        List<Floor> floors = restaurant.getFloors();
        Floor floor = floors.stream().filter(f -> f.getId().equals(layout.getFloor())).findFirst().orElse(null);
        if(floor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Floor not found");
        }

        Layout newLayout = new Layout(floor);
        newLayout.setName(layout.getName());
        newLayout.setTemporary(layout.isTemporary());
        newLayout.setUpdated(System.currentTimeMillis());

        List<Table> tables = restaurant.getTables();
        Map<String,Table> currentTablesById = tables.stream().collect(Collectors.toMap(Table::getId, Function.identity()));

        //copy the tables
        layout.getTables().forEach(t -> {
            Table newTable = new Table(t);
            newTable.setId(IDAble.generateId(restaurantId));

            if(currentTablesById.containsKey(t.getId())) {
                newLayout.getTables().add(newTable.getId());
                if(t.getPosition() != null) {
                    Position position = new Position(t.getPosition());
                    newTable.setPosition(position);
                }
                tables.add(newTable);
            }
        });

        floor.getLayouts().add(newLayout);
        masterDataService.upsert(restaurant);

        return ResponseEntity.created(URI.create("/Layout/"+newLayout.getId())).body(new IdPojo(newLayout.getId()));
    }



    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putLayout(@PathVariable("id") String id,
                                       @RequestHeader(Params.AUTHORIZATION) String token,
                                       @RequestBody @NotNull HostLayoutView newLayout) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        List<Floor> floors = restaurant.getFloors();
        Floor floor = floors.stream().filter(f -> f.getId().equals(newLayout.getFloor())).findFirst().orElse(null);
        if(floor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Floor not found");
        }
        Layout layout = floor.getLayouts().stream().filter(l -> l.getId().equals(id)).findFirst().orElse(null);
        if(layout == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Layout not found");
        }

        layout.setName(newLayout.getName());
        layout.setUpdated(System.currentTimeMillis());
        layout.setTemporary(newLayout.isTemporary());

        List<Table> tableUpdates = new ArrayList<>();
        Map<String,Table> currentTables = restaurant.getTables().stream().collect(Collectors.toMap(Table::getId, Function.identity()));
        layout.getTables().clear();

        newLayout.getTables().stream().filter(t -> StringUtils.isNotBlank(t.getId())).forEach(t -> {
            Table table = new Table();
            table.setId(t.getId());
            table.setName(t.getName());
            table.setPosition(t.getPosition() == null ? new Position() : new Position(t.getPosition()));
            table.setShape(TableShape.fromInt(t.getShape()));
            if(!layout.getTables().contains(t.getId())) {
                layout.getTables().add(t.getId());
                tableUpdates.add(table);
            }
        });

        tableUpdates.forEach(updatedOrNewTable -> currentTables.put(updatedOrNewTable.getId(), updatedOrNewTable));
        restaurant.setTables(new ArrayList<>(currentTables.values()));
        masterDataService.upsert(restaurant);

        return ResponseEntity.created(URI.create("/Layout/"+id)).build();
    }

}
