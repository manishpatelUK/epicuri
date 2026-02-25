package uk.co.epicuri.serverapi.client.endpoints;

import org.junit.Ignore;
import uk.co.epicuri.serverapi.repository.BaseIT;

@Ignore
public class MenuSetupBaseIT extends BaseIT {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        menuItem1.setPrice(10);
        menuItem1.setTaxTypeId(tax1.getId());
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem1.getModifierGroupIds().clear();
        menuItem1.getModifierGroupIds().add(modifierGroup1.getId());
        menuItemRepository.save(menuItem1);

        modifierGroup1.setRestaurantId(restaurant1.getId());
        modifierGroup1.getModifiers().clear();
        modifierGroup1.getModifiers().add(modifier1);
        modifierGroup1.setLowerLimit(1);
        modifierGroup1.setUpperLimit(1);
        modifierGroupRepository.save(modifierGroup1);

        modifier1.setModifierValue("foo");
        modifier1.setPrice(10);
        modifier1.setPriceOverride(10);
        modifier1.setTaxTypeId(tax2.getId());
        modifierRepository.save(modifier1);

        printer1.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer1);
    }
}
