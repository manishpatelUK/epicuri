package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtExternalId;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.ArrayList;
import java.util.List;

@MgmtPojoModel
public class Service extends IDAble {
    @MgmtExternalId(externalClass = Menu.class, endpoint = "Menus", restrictOnParentId = true, listView = false, traverseToParent = 1)
    private String defaultMenuId;
    @MgmtDisplayField
    private String name;
    private String notes;
    private SessionType sessionType;
    private boolean active;
    @MgmtExternalId(externalClass = Menu.class, endpoint = "Menus", restrictOnParentId = true, listView = false, traverseToParent = 1)
    private String selfServiceMenuId;
    private Schedule schedule;
    private List<Course> courses = new ArrayList<>();

    private boolean isDefaultService = false;

    public Service(){}

    public Service(Restaurant restaurant) {
        setId(generateId(restaurant, restaurant.getServices()));
    }

    public String getDefaultMenuId() {
        return defaultMenuId;
    }

    public void setDefaultMenuId(String defaultMenuId) {
        this.defaultMenuId = defaultMenuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSelfServiceMenuId() {
        return selfServiceMenuId;
    }

    public void setSelfServiceMenuId(String selfServiceMenuId) {
        this.selfServiceMenuId = selfServiceMenuId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public boolean isDefaultService() {
        return isDefaultService;
    }

    public void setDefaultService(boolean defaultService) {
        isDefaultService = defaultService;
    }
}
