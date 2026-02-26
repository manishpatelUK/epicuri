package uk.co.epicuri.serverapi.common.pojo.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.Country;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryView {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Acronym")
    private String acronym;

    public CountryView(Country country) {
        this.name = country.getName();
        this.acronym = country.getAcronym();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }
}
