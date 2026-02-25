package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifierGroupView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("GroupName")
    private String name;

    @JsonProperty("LowerLimit")
    private int lowerLimit;

    @JsonProperty("UpperLimit")
    private int upperLimit;

    @JsonProperty("Modifiers")
    private List<ModifierView> modifiers = new ArrayList<>();

    public ModifierGroupView(){}
    public ModifierGroupView(ModifierGroup modifierGroup) {
        this.id = modifierGroup.getId();
        this.name = modifierGroup.getName();
        this.lowerLimit = modifierGroup.getLowerLimit();
        this.upperLimit = modifierGroup.getUpperLimit();
        this.modifiers = modifierGroup.getModifiers().stream().map(m -> new ModifierView(m, modifierGroup)).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(int lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ModifierView> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierView> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
