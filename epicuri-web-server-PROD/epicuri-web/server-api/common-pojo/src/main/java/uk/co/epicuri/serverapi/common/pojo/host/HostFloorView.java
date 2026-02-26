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
public class HostFloorView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Capacity")
    private int capacity;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ImageURL")
    private String imageURL;

    @JsonProperty("Layout")
    private String layout;

    @JsonProperty("Layouts")
    private List<HostLayoutView> layouts = new ArrayList<>();

    public HostFloorView() {}
    public HostFloorView(Floor floor, boolean includeLayouts, List<Table> tables) {
        this.id = floor.getId();
        this.capacity = floor.getCapacity();
        this.name = floor.getName();
        this.imageURL = floor.getImageURL();
        layout = floor.getActiveLayout();

        if(includeLayouts) {
            layouts = floor.getLayouts().stream()
                    .map(l ->
                new HostLayoutView(floor,l,tables.stream().filter(t -> l.getTables().contains(t.getId())).collect(Collectors.toList()))
            ).collect(Collectors.toList());
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public List<HostLayoutView> getLayouts() {
        return layouts;
    }

    public void setLayouts(List<HostLayoutView> layouts) {
        this.layouts = layouts;
    }
}
