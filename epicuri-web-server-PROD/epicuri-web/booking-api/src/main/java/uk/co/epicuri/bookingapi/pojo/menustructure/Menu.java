package uk.co.epicuri.bookingapi.pojo.menustructure;

import java.util.ArrayList;
import java.util.List;

/**
 * 27/05/2015
 */
public class Menu {
    private String name;
    private List<Category> categories = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

}
