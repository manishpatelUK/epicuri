package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("GroupName")
    private String groupName;

    @JsonProperty("MenuCategoryId")
    private String menuCategoryId;

    @JsonProperty("MenuItems")
    private List<MenuItemView> menuItems = new ArrayList<>();

    @JsonProperty("MenuItemIds")
    private List<String> menuItemIds = new ArrayList<>();

    @JsonProperty("Order")
    private int order;

    public GroupView(String categoryId, Group group, List<MenuItem> items, List<Course> allCategoryCourses) {
        this.id = group.getId();
        this.groupName = group.getName();
        this.menuCategoryId = categoryId;
        this.menuItems = items.stream()
                .map(x -> new MenuItemView(x,
                                allCategoryCourses,
                                group.getItems().indexOf(x.getId())))
                .collect(Collectors.toList());
        Collections.sort(menuItems);
        this.menuItemIds = menuItems.stream().map(MenuItemView::getId).collect(Collectors.toList());
        this.order = group.getOrder();
    }

    public GroupView() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMenuCategoryId() {
        return menuCategoryId;
    }

    public void setMenuCategoryId(String menuCategoryId) {
        this.menuCategoryId = menuCategoryId;
    }

    public List<MenuItemView> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemView> menuItems) {
        this.menuItems = menuItems;
    }

    public List<String> getMenuItemIds() {
        return menuItemIds;
    }

    public void setMenuItemIds(List<String> menuItemIds) {
        this.menuItemIds = menuItemIds;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
