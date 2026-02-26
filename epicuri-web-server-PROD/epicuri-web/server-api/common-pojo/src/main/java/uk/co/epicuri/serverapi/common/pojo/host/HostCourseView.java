package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostCourseView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    public String name;

    @JsonProperty("Ordering")
    public Short ordering;

    @JsonProperty("ServiceId")
    public String serviceId;

    public HostCourseView(){}
    public HostCourseView(Course course) {
        id = course.getId();
        name = course.getName();
        ordering = course.getOrdering();
        serviceId = IDAble.extractParentId(course.getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getOrdering() {
        return ordering;
    }

    public void setOrdering(Short ordering) {
        this.ordering = ordering;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
