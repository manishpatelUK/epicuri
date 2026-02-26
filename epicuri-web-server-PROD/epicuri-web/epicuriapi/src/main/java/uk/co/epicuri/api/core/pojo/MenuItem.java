package uk.co.epicuri.api.core.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 28/08/2014
 */
public class MenuItem {
    private int Id;
    private String Name, Description;
    private double Price;
    private int DefaultPrinter, TaxTypeId, MenuItemTypeId;
    private boolean Unavailable = false;
    private List<String> ModifierGroups = new ArrayList<>();
    //private List<Tag> TagIds = new ArrayList<>();
    List<String> TagIds = new ArrayList<>(); // REDUNDANT
    private List<Integer> MenuGroups = new ArrayList<>();

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getTaxTypeId() {
        return TaxTypeId;
    }

    public void setTaxTypeId(int taxTypeId) {
        TaxTypeId = taxTypeId;
    }

    public int getDefaultPrinter() {
        return DefaultPrinter;
    }

    public void setDefaultPrinter(int defaultPrinter) {
        DefaultPrinter = defaultPrinter;
    }

    public int getMenuItemTypeId() {
        return MenuItemTypeId;
    }

    public void setMenuItemTypeId(int menuItemTypeId) {
        MenuItemTypeId = menuItemTypeId;
    }

    public boolean isUnavailable() {
        return Unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        Unavailable = unavailable;
    }

    public List<Integer> getMenuGroups() {
        return MenuGroups;
    }

    public void setMenuGroups(List<Integer> menuGroups) {
        MenuGroups = menuGroups;
    }

    public List<String> getModifierGroups() {
        return ModifierGroups;
    }

    public void setModifierGroups(List<String> modifierGroups) {
        ModifierGroups = modifierGroups;
    }

    public List<String> getTagIds() {
        return TagIds;
    }

    public void setTagIds(List<String> tagIds) {
        TagIds = tagIds;
    }

    @Override
    public String toString() {
        return "{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", Description='" + Description + '\'' +
                ", Price=" + Price +
                ", DefaultPrinter=" + DefaultPrinter +
                ", TaxTypeId=" + TaxTypeId +
                ", MenuItemTypeId=" + MenuItemTypeId +
                ", Unavailable=" + Unavailable +
                ", ModifierGroups=" + ModifierGroups +
                ", TagIds=" + TagIds +
                ", MenuGroups=" + MenuGroups +
                '}';
    }

    /*public static class Tag {
        private String Tag;
        private int Id;

        public String getTag() {
            return Tag;
        }

        public void setTag(String tag) {
            this.Tag = tag;
        }

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            this.Id = id;
        }
    }*/

}
