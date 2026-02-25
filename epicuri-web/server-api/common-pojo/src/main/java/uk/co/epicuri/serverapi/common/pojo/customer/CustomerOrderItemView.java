package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerOrderItemView {
    @JsonProperty("MenuItemId")
    private String menuItemId;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("Modifiers")
    private List<String> modifiers = new ArrayList<>();

    @JsonProperty("InstantiatedFromId")
    private int instantiatedFromId = ActivityInstantiationConstant.UNKNOWN.getId();

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public int getInstantiatedFromId() {
        return instantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        this.instantiatedFromId = instantiatedFromId;
    }
}
