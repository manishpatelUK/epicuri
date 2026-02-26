package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.BaseIT;

/**
 * Created by manish.
 */
public abstract class MenuBaseIT extends BaseIT{
    @Before
    public void setUp() throws Exception {
        super.setUp();

        restaurant1.getServices().get(0).getCourses().add(course1);
        restaurant1.getServices().get(0).getCourses().add(course2);
        restaurant1.getServices().get(0).getCourses().add(course3);
        restaurantRepository.save(restaurant1);

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem1.setDescription(RandomStringUtils.randomAlphanumeric(4));
        menuItem1.setImageURL(RandomStringUtils.randomAlphanumeric(4));
        menuItem1.getModifierGroupIds().add(modifierGroup1.getId());
        menuItem1.setTaxTypeId(tax1.getId());
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem2.setDefaultPrinter(printer2.getId());
        menuItem2.setDescription(RandomStringUtils.randomAlphanumeric(4));
        menuItem2.setImageURL(RandomStringUtils.randomAlphanumeric(4));
        menuItem2.setTaxTypeId(tax2.getId());
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItem3.setDefaultPrinter(printer2.getId());
        menuItem3.setDescription(RandomStringUtils.randomAlphanumeric(4));
        menuItem3.setImageURL(RandomStringUtils.randomAlphanumeric(4));
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItem3.setTaxTypeId(tax3.getId());

        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        modifierGroup1.setRestaurantId(restaurant1.getId());
        /*modifierGroup1.getModifiers().add(modifier1);
        modifierGroup1.getModifiers().add(modifier2);
        modifierGroup1.getModifiers().add(modifier3);*/
        modifierGroup1.setLowerLimit(0);
        modifierGroup1.setUpperLimit(3);
        modifierGroup1 = modifierGroupRepository.save(modifierGroup1);

        menu1.setRestaurantId(restaurant2.getId());
        menu2.setRestaurantId(restaurant1.getId());
        menu2.setName("menu2");
        menu3.setRestaurantId(restaurant3.getId());

        menu2.getCategories().add(category1);
        category1.setId(IDAble.generateId(menu2.getId()));
        menu2.getCategories().add(category2);
        category2.setId(IDAble.generateId(menu2.getId()));
        menu2.getCategories().add(category3);
        category3.setId(IDAble.generateId(menu2.getId()));

        category1.getCourseIds().add(course1.getId());
        category1.getCourseIds().add(course2.getId());
        category1.getGroups().add(group1);
        group1.setId(IDAble.generateId(category1.getId()));
        category1.getGroups().add(group2);
        group2.setId(IDAble.generateId(category1.getId()));
        category1.getGroups().add(group3);
        group3.setId(IDAble.generateId(category1.getId()));
        category2.getCourseIds().add(course3.getId());
        category3.getCourseIds().add(course3.getId());

        group1.getItems().add(menuItem1.getId());
        group1.getItems().add(menuItem2.getId());
        group1.getItems().add(menuItem3.getId());

        group2.getItems().add(menuItem1.getId());
        group2.getItems().add(menuItem2.getId());

        group3.getItems().add(menuItem3.getId());

        menuRepository.save(menu1);
        menuRepository.save(menu2);
        menuRepository.save(menu3);

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);
    }
}
