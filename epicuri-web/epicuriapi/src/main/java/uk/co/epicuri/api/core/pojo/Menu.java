package uk.co.epicuri.api.core.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 11/11/2014
 */
public class Menu {
    private boolean Active;
    private int Id;
    private String MenuName;
    private List<MenuCategory> MenuCategories = new ArrayList<>();

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getMenuName() {
        return MenuName;
    }

    public void setMenuName(String menuName) {
        MenuName = menuName;
    }

    public List<MenuCategory> getMenuCategories() {
        return MenuCategories;
    }

    public void setMenuCategories(List<MenuCategory> menuCategories) {
        MenuCategories = menuCategories;
    }

    public static class MenuCategory {
        private int Id;
        private String CategoryName;
        private List<MenuGroup> MenuGroups = new ArrayList<>();
        private List<Course> DefaultCourses = new ArrayList<>();
        private int Order;

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public String getCategoryName() {
            return CategoryName;
        }

        public void setCategoryName(String categoryName) {
            CategoryName = categoryName;
        }

        public List<MenuGroup> getMenuGroups() {
            return MenuGroups;
        }

        public void setMenuGroups(List<MenuGroup> menuGroups) {
            MenuGroups = menuGroups;
        }

        public List<Course> getDefaultCourses() {
            return DefaultCourses;
        }

        public void setDefaultCourses(List<Course> defaultCourses) {
            DefaultCourses = defaultCourses;
        }

        public int getOrder() {
            return Order;
        }

        public void setOrder(int order) {
            Order = order;
        }

        public static class Course {
            private int Id;
            private String Name;
            private int Order;
            private int ServiceId;

            public int getId() {
                return Id;
            }

            public void setId(int id) {
                this.Id = id;
            }

            public String getName() {
                return Name;
            }

            public void setName(String name) {
                Name = name;
            }

            public int getOrder() {
                return Order;
            }

            public void setOrder(int order) {
                Order = order;
            }

            public int getServiceId() {
                return ServiceId;
            }

            public void setServiceId(int serviceId) {
                ServiceId = serviceId;
            }
        }

        public static class MenuGroup {
            private int Id;
            private String GroupName;
            private int MenuCategoryId;
            private int Order;
            private List<Integer> MenuItemIds = new ArrayList<>();

            public int getId() {
                return Id;
            }

            public void setId(int id) {
                Id = id;
            }

            public String getGroupName() {
                return GroupName;
            }

            public void setGroupName(String groupName) {
                GroupName = groupName;
            }

            public int getMenuCategoryId() {
                return MenuCategoryId;
            }

            public void setMenuCategoryId(int menuCategoryId) {
                MenuCategoryId = menuCategoryId;
            }

            public int getOrder() {
                return Order;
            }

            public void setOrder(int order) {
                Order = order;
            }

            public List<Integer> getMenuItemIds() {
                return MenuItemIds;
            }

            public void setMenuItemIds(List<Integer> menuItemIds) {
                MenuItemIds = menuItemIds;
            }
        }
    }
}
