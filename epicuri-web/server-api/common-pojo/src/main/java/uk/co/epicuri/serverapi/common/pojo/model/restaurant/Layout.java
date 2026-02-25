package uk.co.epicuri.serverapi.common.pojo.model.restaurant;


import org.apache.commons.lang3.builder.EqualsBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.util.ArrayList;
import java.util.List;

public class Layout extends IDAble {
    private String name;

    private List<String> tables = new ArrayList<>();

    private long updated; // was DateTime
    private String floor;
    private boolean temporary;

    public Layout(){}

    public Layout(Floor parent) {
        setId(generateId(parent, parent.getLayouts()));
        this.floor = parent.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }
}
