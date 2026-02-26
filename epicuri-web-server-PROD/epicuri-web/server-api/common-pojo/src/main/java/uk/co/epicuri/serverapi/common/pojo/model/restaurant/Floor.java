package uk.co.epicuri.serverapi.common.pojo.model.restaurant;


import org.springframework.data.annotation.Transient;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtFileOpener;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.util.ArrayList;
import java.util.List;

@MgmtPojoModel
public class Floor extends IDAble {
    @Transient
    @MgmtIgnoreField
    public transient static final String FLOOR_IMAGE_ENDPOINT = "Restaurants/floorImage";

    @MgmtDisplayField
    private String name;
    private int capacity;

    @MgmtFileOpener(endpointHint = FLOOR_IMAGE_ENDPOINT)
    private String imageURL;
    @MgmtIgnoreField
    private String activeLayout;
    @MgmtIgnoreField
    private double scale;
    @MgmtIgnoreField
    private List<Layout> layouts = new ArrayList<>();

    public Floor() {}

    public Floor(String parentId) {
        setId(generateId(parentId));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getActiveLayout() {
        return activeLayout;
    }

    public void setActiveLayout(String activeLayout) {
        this.activeLayout = activeLayout;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public List<Layout> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<Layout> layouts) {
        this.layouts = layouts;
    }
}
