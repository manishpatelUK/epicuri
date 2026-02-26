package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerModifierGroupView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("GroupName")
    private String groupName;

    @JsonProperty("LowerLimit")
    private int lowerLimit;

    @JsonProperty("UpperLimit")
    private int upperLimit;

    @JsonProperty("Modifiers")
    private List<ModifierView> modifiers = new ArrayList<>();

    public CustomerModifierGroupView(){}

    public CustomerModifierGroupView(ModifierGroup modifierGroup) {
        this.id = modifierGroup.getId();
        this.groupName = modifierGroup.getName();
        this.lowerLimit = modifierGroup.getLowerLimit();
        this.upperLimit = modifierGroup.getUpperLimit();
        this.modifiers = modifierGroup.getModifiers().stream().map(x -> new ModifierView(
                                                                    x,modifierGroup)).collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public List<ModifierView> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierView> modifiers) {
        this.modifiers = modifiers;
    }
}
