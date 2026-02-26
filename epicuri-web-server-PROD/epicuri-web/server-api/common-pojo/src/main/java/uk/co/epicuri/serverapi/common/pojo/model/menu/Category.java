package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Category extends IDAble{
    private String name;
    private int order;
    private List<Group> groups = new ArrayList<>();
    private List<String> courseIds = new ArrayList<>();
    private String imageURL;

    public static final Comparator<Category> COMPARE_CATEGORY = Comparator.comparingInt(Category::getOrder);

    public Category(){}
    public Category(Menu parentMenu, CategoryView categoryView) {
        setId(generateId(parentMenu, parentMenu.getCategories()));
        this.name = categoryView.getCategoryName();
        this.order = categoryView.getOrder();
        this.courseIds = categoryView.getDefaultCourseIds();
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

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        Collections.sort(this.groups, Group.COMPARE_GROUP);
    }

    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Category copyExcludingId() {
        Category category = new Category();
        category.setName(name);
        category.setOrder(order);
        category.getCourseIds().addAll(courseIds);
        category.setImageURL(imageURL);

        for(Group group : groups) {
            category.getGroups().add(group.copyExcludingId());
        }
        return category;
    }
}
