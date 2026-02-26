package uk.co.epicuri.api.core.pojo.floor;

import uk.co.epicuri.api.core.pojo.session.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class Layout {
    private List<Table> Tables = new ArrayList<>();
    private int Id;
    private String Name;
    private long Updated;
    private int Floor;
    private boolean Temporary;

    public List<Table> getTables() {
        return Tables;
    }

    public void setTables(List<Table> tables) {
        Tables = tables;
    }

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

    public long getUpdated() {
        return Updated;
    }

    public void setUpdated(long updated) {
        Updated = updated;
    }

    public int getFloor() {
        return Floor;
    }

    public void setFloor(int floor) {
        Floor = floor;
    }

    public boolean isTemporary() {
        return Temporary;
    }

    public void setTemporary(boolean temporary) {
        Temporary = temporary;
    }
}
