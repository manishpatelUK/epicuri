package uk.co.epicuri.serverapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommonDataViewsConversionService {

    @Autowired
    private MasterDataService masterDataService;

    public MenuView getMenuView(String restaurantId, String menuId) {
        return getMenuView(restaurantId, masterDataService.getMenu(menuId));
    }

    public MenuView getMenuView(String restaurantId, Menu menu) {
        List<Course> courses = masterDataService.getCoursesByRestaurantId(restaurantId);
        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(restaurantId);

        List<String> ids = menu.getCategories().stream()
                .map(Category::getGroups)
                .flatMap(List::stream)
                .map(Group::getItems)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<MenuItem> items = masterDataService.getMenuItems(ids);
        return new MenuView(menu, modifierGroups, courses, items);
    }

    //todo there's probably more that can be added here
}
