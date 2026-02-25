package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.menu.CourseView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerOrderView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("MenuItemId")
    private String menuItemId;

    @JsonProperty("Modifiers")
    private List<String> modifiers = new ArrayList<>();

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("PriceOverride")
    private Double priceOverride;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("Item")
    private MenuItemView item;

    @JsonProperty("ModifierDescriptions")
    private List<ModifierView> modifierDescriptions = new ArrayList<>();

    @JsonProperty("Course")
    private CourseView course;

    @JsonProperty("InstantiatedFromId")
    private int instantiatedFromId;

    // in update order payload
    @JsonProperty("DinerId")
    private String dinerId;

    // in update order payload
    @JsonProperty("CourseId")
    private String courseId;

    private String publicOrderId;

    public CustomerOrderView(){}
    public CustomerOrderView(Order order, Course course, Service service) {
        id = order.getId();
        menuItemId = order.getMenuItemId();
        modifiers = order.getModifiers().stream().map(Modifier::getId).collect(Collectors.toList());
        modifierDescriptions = order.getModifiers().stream().map(ModifierView::new).collect(Collectors.toList());
        note = order.getNote();
        quantity = order.getQuantity();


        price = MoneyService.toMoneyRoundHalfDown(order.getPriceOverride());
        priceOverride = price;

        if(course == null) {
            course = RestaurantConstants.FALLBACK_COURSE;
        }

        item = new MenuItemView(order.getMenuItem(), course, service, 0);
        this.course = new CourseView(course);
        instantiatedFromId = order.getInstantiatedFrom() == null ? ActivityInstantiationConstant.UNKNOWN.getId() : order.getInstantiatedFrom().getId();
        dinerId = order.getDinerId();
        courseId = course.getId();
        publicOrderId = order.getPublicFacingOrderId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
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

    public Double getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(Double priceOverride) {
        this.priceOverride = priceOverride;
    }

    public MenuItemView getItem() {
        return item;
    }

    public void setItem(MenuItemView item) {
        this.item = item;
    }

    public List<ModifierView> getModifierDescriptions() {
        return modifierDescriptions;
    }

    public void setModifierDescriptions(List<ModifierView> modifierDescriptions) {
        this.modifierDescriptions = modifierDescriptions;
    }

    public CourseView getCourse() {
        return course;
    }

    public void setCourse(CourseView course) {
        this.course = course;
    }

    public int getInstantiatedFromId() {
        return instantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        this.instantiatedFromId = instantiatedFromId;
    }

    public String getDinerId() {
        return dinerId;
    }

    public void setDinerId(String dinerId) {
        this.dinerId = dinerId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPublicOrderId() {
        return publicOrderId;
    }

    public void setPublicOrderId(String publicOrderId) {
        this.publicOrderId = publicOrderId;
    }
}
