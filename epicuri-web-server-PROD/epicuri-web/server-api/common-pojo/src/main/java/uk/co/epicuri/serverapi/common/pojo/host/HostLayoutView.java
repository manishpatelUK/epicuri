package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Floor;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostLayoutView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Temporary")
    private boolean temporary;

    @JsonProperty("Floor")
    private String floor;

    @JsonProperty("Tables")
    private List<HostTableView> tables = new ArrayList<>();

    @JsonProperty("Updated")
    private long updated;

    public HostLayoutView(){}
    public HostLayoutView(Floor parent, Layout layout, List<Table> tables) {
        this.id = layout.getId();
        this.name = layout.getName();
        this.floor = parent.getId();
        this.tables = tables.stream().map(t -> new HostTableView(t, true)).collect(Collectors.toList());
        this.updated = layout.getUpdated() / 1000;
        this.temporary = layout.isTemporary();
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

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public List<HostTableView> getTables() {
        return tables;
    }

    public void setTables(List<HostTableView> tables) {
        this.tables = tables;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
