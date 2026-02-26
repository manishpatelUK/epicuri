package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostTableView {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("Shape")
    private int shape;

    @JsonProperty("Position")
    private HostTablePositionView position;

    public HostTableView(){}
    public HostTableView(Table table, boolean includePosition){
        this.id = table.getId();
        if(table.getName() != null) {
            this.name = table.getName();
        }
        this.shape = table.getShape().getOrdinal();
        if(includePosition && table.getPosition() != null) {
            position = new HostTablePositionView(table.getPosition());
        }
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

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public HostTablePositionView getPosition() {
        return position;
    }

    public void setPosition(HostTablePositionView position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
