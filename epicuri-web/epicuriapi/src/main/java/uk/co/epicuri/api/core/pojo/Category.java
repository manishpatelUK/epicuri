package uk.co.epicuri.api.core.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 12/11/2014
 */
public class Category {
    private String CategoryName;
    private int MenuId;
    private int Order;
    private List<Integer> DefaultCourseIds = new ArrayList<>();
    private List<Integer> MenuGroupIds = new ArrayList<>();

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public int getMenuId() {
        return MenuId;
    }

    public void setMenuId(int menuId) {
        MenuId = menuId;
    }

    public int getOrder() {
        return Order;
    }

    public void setOrder(int order) {
        Order = order;
    }

    public List<Integer> getDefaultCourseIds() {
        return DefaultCourseIds;
    }

    public void setDefaultCourseIds(List<Integer> defaultCourseIds) {
        DefaultCourseIds = defaultCourseIds;
    }

    public List<Integer> getMenuGroupIds() {
        return MenuGroupIds;
    }

    public void setMenuGroupIds(List<Integer> menuGroupIds) {
        MenuGroupIds = menuGroupIds;
    }
}
