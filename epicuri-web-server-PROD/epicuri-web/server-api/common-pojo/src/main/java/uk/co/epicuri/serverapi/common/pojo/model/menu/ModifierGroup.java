package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierGroupView;

import java.util.ArrayList;
import java.util.List;

@Document(collection = TableNames.MENU_MODIFIER_GROUPS)
public class ModifierGroup extends Deletable {
    private String name;
    private int upperLimit;
    private int lowerLimit;

    @DBRef
    private List<Modifier> modifiers = new ArrayList<>();

    @Indexed
    private String restaurantId;

    public ModifierGroup(){}

    public ModifierGroup(ModifierGroupView modifierGroupView) {
        this.setId(modifierGroupView.getId());
        this.name = modifierGroupView.getName();
        this.upperLimit = modifierGroupView.getUpperLimit();
        this.lowerLimit = modifierGroupView.getLowerLimit();
    }

    public ModifierGroup(ModifierGroupView modifierGroupView, String restaurantId) {
        this(modifierGroupView);
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    public int getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(int lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }
}
