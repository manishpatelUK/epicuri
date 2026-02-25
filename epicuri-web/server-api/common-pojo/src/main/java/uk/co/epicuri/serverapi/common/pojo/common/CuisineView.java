package uk.co.epicuri.serverapi.common.pojo.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Cuisine;

/**
 * Created by manish.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CuisineView {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Id")
    private String id;

    public CuisineView(){}

    public CuisineView(Cuisine cuisine){
        this.id = cuisine.getId();
        this.name = cuisine.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
