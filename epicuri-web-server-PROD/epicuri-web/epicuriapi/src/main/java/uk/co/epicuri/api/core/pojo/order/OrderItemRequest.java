package uk.co.epicuri.api.core.pojo.order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class OrderItemRequest {
    private int DinerId;
    private int Quantity;
    private int MenuItemId;
    private int InstantiatedFromId;
    private int CourseId;
    private List<Integer> modifiers = new ArrayList<>();
    private String Note;

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

    public int getCourseId() {
        return CourseId;
    }

    public void setCourseId(int courseId) {
        CourseId = courseId;
    }

    public List<Integer> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Integer> modifiers) {
        this.modifiers = modifiers;
    }

    public int getDinerId() {
        return DinerId;
    }

    public void setDinerId(int dinerId) {
        DinerId = dinerId;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public int getMenuItemId() {
        return MenuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        MenuItemId = menuItemId;
    }

    public int getInstantiatedFromId() {
        return InstantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        InstantiatedFromId = instantiatedFromId;
    }
}
