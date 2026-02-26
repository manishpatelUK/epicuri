package uk.co.epicuri.serverapi.common.pojo.model;

import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;

@MgmtPojoModel
public class Course extends IDAble {
    @MgmtDisplayField
    private String name;
    private short ordering = 0;

    public Course(){}

    public Course(Service parent) {
        setId(generateId(parent, parent.getCourses()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getOrdering() {
        return ordering;
    }

    public void setOrdering(short ordering) {
        this.ordering = ordering;
    }
}
