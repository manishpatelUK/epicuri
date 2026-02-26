package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.menu.CourseView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierValueView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostOrderView {

    @JsonProperty("DinerId")
    private String dinerId;

    @JsonProperty("dinerId") //necessary because of stupidity in EpicuriOrderItem in waiter app
    private String dinerIdCopy;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("MenuItem")
    private MenuItemView item;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("Course")
    private CourseView course;

    @JsonProperty("DiscountReason")
    private String discountReason;

    private HostAdjustmentView adjustment;

    @JsonProperty("PriceOverride")
    private Double priceOverride;

    @JsonProperty("Completed")
    private Long completed; //in seconds

    @JsonProperty("Note")
    private String note;

    @JsonProperty("Modifiers")
    private List<ModifierValueView> modifiers = new ArrayList<>();

    @JsonProperty("deliveryLocation")
    private String deliveryLocation;

    // in update order payload
    @JsonProperty("CourseId")
    private String courseId;

    public HostOrderView(){}
    public HostOrderView(Order order,
                         Service service) {
        setDinerId(order.getDinerId());
        this.id = order.getId();

        Course courseInService = null;
        if(service != null) {
            courseInService = service.getCourses().stream().filter(c -> c.getId().equals(order.getCourseId())).findFirst().orElse(null);
        }
        if (courseInService == null) {
            this.item = new MenuItemView(order.getMenuItem(), 0);
            this.course = new CourseView(RestaurantConstants.FALLBACK_COURSE);
        } else {
            this.item = new MenuItemView(order.getMenuItem(), courseInService, service, 0);
            this.course = new CourseView(courseInService);
            this.courseId = course.getId();
        }

        if(service != null) {
            this.item.setServiceId(service.getId());
        }

        this.quantity = order.getQuantity();
        if(order.getAdjustment() != null) {
            this.discountReason = order.getAdjustment().getAdjustmentType().getName();
            if(order.getAdjustment().getAdjustmentType().getType() == AdjustmentTypeType.DISCOUNT) {
                priceOverride = 0D;
            }
            adjustment = new HostAdjustmentView(order.getAdjustment());
        } else if(order.getPriceOverride() != order.getMenuItem().getPrice()) {
            priceOverride = MoneyService.toMoneyRoundNearest(order.getPriceOverride());
        }
        if(order.getCompleted() != null) {
            completed = order.getCompleted() / 1000;
        }
        this.note = order.getNote();
        this.modifiers = order.getModifiers().stream().map(ModifierValueView::new).collect(Collectors.toList());
        this.deliveryLocation = order.getDeliveryLocation();
    }

    public String getDinerId() {
        return dinerId;
    }

    public void setDinerId(String dinerId) {
        this.dinerId = dinerId;
        this.dinerIdCopy = dinerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MenuItemView getItem() {
        return item;
    }

    public void setItem(MenuItemView item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CourseView getCourse() {
        return course;
    }

    public void setCourse(CourseView course) {
        this.course = course;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }

    public Double getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(Double priceOverride) {
        this.priceOverride = priceOverride;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<ModifierValueView> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierValueView> modifiers) {
        this.modifiers = modifiers;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getDinerIdCopy() {
        return dinerId; //divert to dinerId
    }

    public void setDinerIdCopy(String dinerIdCopy) {
        this.dinerIdCopy = dinerIdCopy;
        this.dinerId = dinerIdCopy;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public HostAdjustmentView getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(HostAdjustmentView adjustment) {
        this.adjustment = adjustment;
    }
}
