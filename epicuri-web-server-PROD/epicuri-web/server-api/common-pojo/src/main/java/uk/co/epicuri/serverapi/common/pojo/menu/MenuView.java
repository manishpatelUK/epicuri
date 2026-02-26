package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("MenuName")
    private String menuName;

    @JsonProperty("RestaurantId")
    private String restaurantId;

    @JsonProperty("Active")
    private boolean active = true;

    @JsonProperty("LastUpdated")
    private long lastUpdated; //was double

    @JsonProperty("MenuCategories")
    private List<CategoryView> menuCategories;

    @JsonProperty("ModifierGroups")
    private List<CustomerModifierGroupView> menuModifierGroups;

    private int order = Integer.MAX_VALUE;

    public MenuView(Menu menu,
                    List<ModifierGroup> modifierGroups,
                    List<Course> courses,
                    List<MenuItem> items) {
        id = menu.getId();
        menuName = menu.getName();
        restaurantId = menu.getRestaurantId();
        active = menu.isActive();
        lastUpdated = menu.getLastUpdate();
        order = menu.getOrder();

        menuCategories = new ArrayList<>();
        for(Category category : menu.getCategories()) {
            List<Course> courseList = courses.stream().filter(course -> category.getCourseIds().contains(course.getId())).collect(Collectors.toList());
            menuCategories.add(new CategoryView(category.getId(), id, category, courseList, items));
        }

        menuModifierGroups = modifierGroups.stream().map(CustomerModifierGroupView::new).collect(Collectors.toList());
    }

    public MenuView(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<CategoryView> getMenuCategories() {
        return menuCategories;
    }

    public void setMenuCategories(List<CategoryView> menuCategories) {
        this.menuCategories = menuCategories;
    }

    public List<CustomerModifierGroupView> getMenuModifierGroups() {
        return menuModifierGroups;
    }

    public void setMenuModifierGroups(List<CustomerModifierGroupView> menuModifierGroups) {
        this.menuModifierGroups = menuModifierGroups;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
