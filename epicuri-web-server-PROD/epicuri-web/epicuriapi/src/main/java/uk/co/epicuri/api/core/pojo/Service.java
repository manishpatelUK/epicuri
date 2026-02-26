package uk.co.epicuri.api.core.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 15/11/2014
 */
public class Service {
    private int Id;
    private int MenuId;
    private int SelfServiceMenuId;
    private String MenuName, ServiceName, Notes;
    private List<Course> Courses = new ArrayList<>();

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getMenuId() {
        return MenuId;
    }

    public void setMenuId(int menuId) {
        MenuId = menuId;
    }

    public int getSelfServiceMenuId() {
        return SelfServiceMenuId;
    }

    public void setSelfServiceMenuId(int selfServiceMenuId) {
        SelfServiceMenuId = selfServiceMenuId;
    }

    public String getMenuName() {
        return MenuName;
    }

    public void setMenuName(String menuName) {
        MenuName = menuName;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public void setServiceName(String serviceName) {
        ServiceName = serviceName;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public List<Course> getCourses() {
        return Courses;
    }

    public void setCourses(List<Course> courses) {
        Courses = courses;
    }

    public static class Course {
        private int Id;
        private String Name;
        private int Ordering;
        private int ServiceId;

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            Id = id;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public int getOrdering() {
            return Ordering;
        }

        public void setOrdering(int ordering) {
            Ordering = ordering;
        }

        public int getServiceId() {
            return ServiceId;
        }

        public void setServiceId(int serviceId) {
            ServiceId = serviceId;
        }
    }
}
