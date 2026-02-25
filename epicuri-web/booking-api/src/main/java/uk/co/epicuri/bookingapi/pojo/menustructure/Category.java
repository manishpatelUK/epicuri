package uk.co.epicuri.bookingapi.pojo.menustructure;

import java.util.ArrayList;
import java.util.List;

/**
 * 27/05/2015
 */
public class Category implements Comparable<Category>{
    private String name;
    private List<Group> groups = new ArrayList<>();
    private transient int order;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public int compareTo(Category o) {
        return Integer.compare(order,o.getOrder());
    }
}
