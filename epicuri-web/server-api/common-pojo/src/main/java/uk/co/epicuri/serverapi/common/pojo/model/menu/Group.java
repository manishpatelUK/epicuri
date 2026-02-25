package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Group extends IDAble{

    private String name;
    private int order;
    private List<String> items = new ArrayList<>();

    public static final Comparator<Group> COMPARE_GROUP = (g1, g2) -> Integer.compare(g1.getOrder(), g2.getOrder());

    public Group(){}
    public Group(Category parentCategory, GroupView groupView) {
        setId(IDAble.generateId(parentCategory, parentCategory.getGroups()));
        name = groupView.getGroupName();
        order = groupView.getOrder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trimToEmpty(name);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public Group copyExcludingId() {
        Group group = new Group();
        group.setName(name);
        group.setOrder(order);
        group.getItems().addAll(items);
        return group;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
