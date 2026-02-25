package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("CategoryName")
    private String categoryName;

    @JsonProperty("DefaultCourseIds")
    private List<String> defaultCourseIds;

    @JsonProperty("DefaultCourses")
    private List<CourseView> defaultCourses;

    @JsonProperty("MenuGroups")
    private List<GroupView> menuGroups;

    @JsonProperty("MenuGroupIds")
    private List<String> menuGroupsIds;

    @JsonProperty("Order")
    private int order;

    @JsonProperty("MenuId")
    private String menuId;

    @JsonProperty("imageURL")
    private String imageURL;

    public CategoryView(String categoryId, String menuId, Category category, List<Course> defaultCourses, List<MenuItem> items) {
        this.id = categoryId;
        this.menuId = menuId;
        this.order = category.getOrder();
        this.categoryName = category.getName();
        if(category.getCourseIds() != null) {
            this.defaultCourseIds = category.getCourseIds();
        } else {
            this.defaultCourseIds = new ArrayList<>();
        }
        this.defaultCourses = defaultCourses.stream().map(CourseView::new).collect(Collectors.toList());
        if(category.getGroups() != null) {
            this.menuGroups = category.getGroups().stream().map((x) ->
                    new GroupView(id, x,
                            items.stream().filter(y -> x.getItems().contains(y.getId())).collect(Collectors.toList()),
                            defaultCourses))
                    .collect(Collectors.toList());
            this.menuGroupsIds = menuGroups.stream().map(GroupView::getId).collect(Collectors.toList());
        } else {
            this.menuGroups = new ArrayList<>();
            this.menuGroupsIds = new ArrayList<>();
        }
        this.imageURL = category.getImageURL();
    }

    public CategoryView(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getDefaultCourseIds() {
        return defaultCourseIds;
    }

    public void setDefaultCourseIds(List<String> defaultCourseIds) {
        this.defaultCourseIds = defaultCourseIds;
    }

    public List<CourseView> getDefaultCourses() {
        return defaultCourses;
    }

    public void setDefaultCourses(List<CourseView> defaultCourses) {
        this.defaultCourses = defaultCourses;
    }

    public List<GroupView> getMenuGroups() {
        return menuGroups;
    }

    public void setMenuGroups(List<GroupView> menuGroups) {
        this.menuGroups = menuGroups;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public List<String> getMenuGroupsIds() {
        return menuGroupsIds;
    }

    public void setMenuGroupsIds(List<String> menuGroupsIds) {
        this.menuGroupsIds = menuGroupsIds;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
