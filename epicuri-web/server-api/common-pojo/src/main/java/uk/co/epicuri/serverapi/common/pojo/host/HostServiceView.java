package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.menu.CourseView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostServiceView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("MenuId")
    private String menuId;

    @JsonProperty("ServiceName")
    private String name;

    @JsonProperty("sessionType")
    private SessionType sessionType;

    @JsonProperty("courses")
    private List<CourseView> courses;

    public HostServiceView(){}
    public HostServiceView(Service service){
        this.id = service.getId();
        this.menuId = service.getDefaultMenuId();
        this.name = service.getName();
        this.sessionType = service.getSessionType();
        this.courses = service.getCourses().stream().map(CourseView::new).collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public List<CourseView> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseView> courses) {
        this.courses = courses;
    }
}
