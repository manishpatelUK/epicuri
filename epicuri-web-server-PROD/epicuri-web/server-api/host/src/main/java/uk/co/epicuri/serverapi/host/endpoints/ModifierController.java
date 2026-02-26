package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.auth.HostLevelCheckRequired;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = "/Modifier", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ModifierController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AuthenticationService authenticationService;

    public ModifierController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postModifier(@NotNull @RequestBody ModifierView modifierView) {
        ModifierGroup modifierGroup = masterDataService.getModifierGroup(modifierView.getModifierGroupId());
        if(modifierGroup == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Modifier Group Not Found");
        }

        Modifier modifier = new Modifier(modifierView);
        modifier = masterDataService.upsert(modifier);
        modifierGroup.getModifiers().add(modifier);
        masterDataService.upsert(modifierGroup);

        modifierView.setId(modifier.getId());
        return ResponseEntity.ok(modifierView);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putModifier(@PathVariable("id") String id,
                                         @NotNull @RequestBody ModifierView modifierView) {
        Modifier modifier = masterDataService.getModifier(id);
        if(modifier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Modifier Not Found");
        }

        modifier.setModifierValue(modifierView.getModifierValue());
        modifier.setPrice(MoneyService.toPenniesRoundNearest(modifierView.getPrice()));
        modifier.setPriceOverride(modifier.getPrice());
        modifier.setTaxTypeId(modifierView.getTaxTypeId());
        if(modifierView.getPlu() != null && modifierView.getPlu().trim().length() > 0) {
            modifier.setPlu(modifierView.getPlu());
        }

        masterDataService.upsert(modifier);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteModifier(@RequestHeader(Params.AUTHORIZATION) String token,
                                   @PathVariable("id") String id) {
        Modifier modifier = masterDataService.getModifier(id);
        if(modifier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Modifier Group Not Found");
        }

        modifier.setDeleted(System.currentTimeMillis());
        masterDataService.upsert(modifier);

        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(authenticationService.getRestaurantId(token));
        modifierGroups.stream().filter(modifierGroup -> modifierGroup.getModifiers().removeIf(m -> m.getId().equals(id))).forEach(modifierGroup -> masterDataService.upsert(modifierGroup));

        return ResponseEntity.ok().build();
    }


}
