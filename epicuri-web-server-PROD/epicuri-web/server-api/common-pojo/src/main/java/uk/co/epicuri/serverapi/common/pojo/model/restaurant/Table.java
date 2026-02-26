package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import uk.co.epicuri.serverapi.common.pojo.host.HostTableView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

/**
 * Created by Manish
 */
public class Table extends IDAble {
    private String name;
    private Position position;
    private short defaultCovers;
    private TableShape shape;

    public Table(){}

    public Table(HostTableView tableView){
        setId(tableView.getId());
        if(tableView.getPosition() != null) {
            this.position = new Position(tableView.getPosition());
        }
        this.shape = TableShape.fromInt(tableView.getShape());
        this.name = tableView.getName();
    }

    public Table(String restaurantId, HostTableView tableView){
        setId(generateId(restaurantId));
        if(tableView.getPosition() != null) {
            this.position = new Position(tableView.getPosition());
        }
        this.shape = TableShape.fromInt(tableView.getShape());
        this.name = tableView.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public short getDefaultCovers() {
        return defaultCovers;
    }

    public void setDefaultCovers(short defaultCovers) {
        this.defaultCovers = defaultCovers;
    }

    public TableShape getShape() {
        return shape;
    }

    public void setShape(TableShape shape) {
        this.shape = shape;
    }

    @Override
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object);
    }
}
