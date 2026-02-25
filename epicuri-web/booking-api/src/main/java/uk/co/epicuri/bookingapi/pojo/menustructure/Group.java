package uk.co.epicuri.bookingapi.pojo.menustructure;

import uk.co.epicuri.api.core.pojo.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 27/05/2015
 */
public class Group implements Comparable<Group>{
    private List<MenuItem> items = new ArrayList<>();
    private String name;
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

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }

    @Override
    public int compareTo(Group o) {
        return Integer.compare(order,o.getOrder());
    }
}
