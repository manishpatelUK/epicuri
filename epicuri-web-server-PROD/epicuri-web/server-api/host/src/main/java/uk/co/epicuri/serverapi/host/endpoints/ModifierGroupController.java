package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierGroupView;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/ModifierGroup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ModifierGroupController {
    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AuthenticationService authenticationService;

    public ModifierGroupController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ModifierGroupView>> getModifierGroups(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(restaurantId);

        return ResponseEntity.ok(modifierGroups.stream().map(ModifierGroupView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getModifierGroup(@PathVariable("id") String id) {
        ModifierGroup modifierGroup = masterDataService.getModifierGroup(id);
        if(modifierGroup == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ModifierGroup not found");
        }

        return ResponseEntity.ok(new ModifierGroupView(modifierGroup));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postModifierGroup(@RequestHeader(Params.AUTHORIZATION) String token,
                                               @NotNull @RequestBody ModifierGroupView modifierGroupView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        ModifierGroup modifierGroup = new ModifierGroup(modifierGroupView);
        modifierGroup.setRestaurantId(restaurantId);
        modifierGroup = masterDataService.upsert(modifierGroup);

        return ResponseEntity.ok(new ModifierGroupView(modifierGroup));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putModifierGroup(@PathVariable("id") String id,
                                              @NotNull @RequestBody ModifierGroupView modifierGroupView) {
        final ModifierGroup existing = masterDataService.getModifierGroup(id);
        if(existing == null) {
            return ResponseEntity.notFound().build();
        }

        modifierGroupView.setId(id); //might not be set on payload
        ModifierGroup modifierGroup = new ModifierGroup(modifierGroupView, existing.getRestaurantId());
        modifierGroup.setModifiers(existing.getModifiers());
        modifierGroup = masterDataService.upsert(modifierGroup);
        modifierGroupView = new ModifierGroupView(modifierGroup);
        modifierGroupView.setModifiers(modifierGroup.getModifiers().stream().map(m -> new ModifierView(m,existing)).collect(Collectors.toList()));

        return ResponseEntity.ok(modifierGroupView);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteModifierGroup(@RequestHeader(Params.AUTHORIZATION) String token,
                                                 @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<MenuItem> items = masterDataService.getAllMenuItems(restaurantId);
        items = items.stream().filter(m -> m.getModifierGroupIds().contains(id)).collect(Collectors.toList());
        items.forEach(m -> m.getModifierGroupIds().remove(id));

        masterDataService.upsertMenuItems(items);
        masterDataService.deleteModifierGroup(id);

        return ResponseEntity.ok().build();
    }
}
