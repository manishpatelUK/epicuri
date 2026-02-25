package uk.co.epicuri.serverapi.common.pojo.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuStructure {
    private List<Menu> menus = new ArrayList<>();
    private List<ModifierGroup> modifierGroups = new ArrayList<>();
    private List<Modifier> modifiers = new ArrayList<>();
    private List<String> courseNames = new ArrayList<>();
    private List<MenuItem> items = new ArrayList<>();
    private Map<String,String> menuAndCategoryToCourseName = new HashMap<>();

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public List<ModifierGroup> getModifierGroups() {
        return modifierGroups;
    }

    public void setModifierGroups(List<ModifierGroup> modifierGroups) {
        this.modifierGroups = modifierGroups;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public List<String> getCourseNames() {
        return courseNames;
    }

    public void setCourseNames(List<String> courseNames) {
        this.courseNames = courseNames;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    public Map<String, String> getMenuAndCategoryToCourseName() {
        return menuAndCategoryToCourseName;
    }

    public void setMenuAndCategoryToCourseName(Map<String, String> menuAndCategoryToCourseName) {
        this.menuAndCategoryToCourseName = menuAndCategoryToCourseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuStructure that = (MenuStructure) o;

        if (menus != null ? !menus.equals(that.menus) : that.menus != null) return false;
        if (modifierGroups != null ? !modifierGroups.equals(that.modifierGroups) : that.modifierGroups != null)
            return false;
        if (modifiers != null ? !modifiers.equals(that.modifiers) : that.modifiers != null) return false;
        if (courseNames != null ? !courseNames.equals(that.courseNames) : that.courseNames != null) return false;
        if (items != null ? !items.equals(that.items) : that.items != null) return false;
        return menuAndCategoryToCourseName != null ? menuAndCategoryToCourseName.equals(that.menuAndCategoryToCourseName) : that.menuAndCategoryToCourseName == null;
    }

    @Override
    public int hashCode() {
        int result = menus != null ? menus.hashCode() : 0;
        result = 31 * result + (modifierGroups != null ? modifierGroups.hashCode() : 0);
        result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
        result = 31 * result + (courseNames != null ? courseNames.hashCode() : 0);
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (menuAndCategoryToCourseName != null ? menuAndCategoryToCourseName.hashCode() : 0);
        return result;
    }
}
