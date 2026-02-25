package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.sun.org.apache.xpath.internal.operations.Or;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderPayload;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Document(collection = TableNames.ORDERS)
public class Order extends Deletable {
    @Indexed
    private String sessionId;

    @Indexed
    private long time;

    private String menuItemId;
    private List<Modifier> modifiers = new ArrayList<>();
    private String note;
    private Long completed;
    private String courseId;
    private int itemPrice; //for convenience, original price of menu item
    private int priceOverride; //should always use this for all calculations. Usually == itemPrice
    private int quantity = 1;

    private ActivityInstantiationConstant instantiatedFrom;
    private Adjustment adjustment;
    private boolean voided;
    private String staffId;
    private String dinerId;
    private boolean removeFromReports;
    private MenuItem menuItem;

    private Long doneTime;
    private TaxRate taxRate;

    private String publicFacingOrderId;
    private String deliveryLocation;

    public Order(){}

    public Order(MenuItem item, TaxRate taxRate, int quantity, List<Modifier> modifiers) {
        this.time = System.currentTimeMillis();
        this.menuItem = item;
        this.priceOverride = menuItem.getPrice();
        this.itemPrice = menuItem.getPrice();
        this.quantity = quantity;
        this.modifiers = modifiers;
        this.taxRate = taxRate;
    }

    public Order(Session session,
                 CustomerOrderItemView orderItemView,
                 List<Modifier> allModifiers,
                 TaxRate taxRate,
                 MenuItem item,
                 Diner diner,
                 String staffId) {
        this.sessionId = session.getId();
        this.time = session.getOriginalBooking() == null ? System.currentTimeMillis() : session.getOriginalBooking().getTargetTime();
        this.menuItemId = orderItemView.getMenuItemId();
        this.modifiers = allModifiers;
        this.note = orderItemView.getNote() == null ? "" : orderItemView.getNote();
        this.itemPrice = item.getPrice();
        this.priceOverride = itemPrice;
        this.instantiatedFrom = ActivityInstantiationConstant.valueOf(orderItemView.getInstantiatedFromId());
        this.dinerId = diner.getId();
        this.menuItem = item;
        this.taxRate = taxRate;
        if(orderItemView.getQuantity() > 0) {
            this.quantity = orderItemView.getQuantity();
        }
        if(session.getService() != null && session.getService().getCourses() != null) {
            Course course = session.getService().getCourses().stream().filter(c -> c.getName().equals(RestaurantConstants.IMMEDIATE_COURSE_NAME)).findFirst().orElse(null);
            if(course != null) {
                this.courseId = course.getId();
            }
        }
        this.staffId = staffId;
    }

    public Order(Session session, MenuItem menuItem, TaxRate taxRate, WaitingPartyOrderPayload.OrderPayload orderPayload, List<Modifier> modifiers, Integer priceOverride, String staffId) {
        this.sessionId = session.getId();
        this.time = System.currentTimeMillis();
        this.menuItemId = menuItem.getId();
        this.menuItem = menuItem;
        this.modifiers = modifiers;
        this.quantity = orderPayload.getQuantity();
        this.itemPrice = menuItem.getPrice();
        this.taxRate = taxRate;
        if(priceOverride != null) {
            this.priceOverride = priceOverride;
        } else {
            this.priceOverride = menuItem.getPrice();
        }
        if(StringUtils.isBlank(orderPayload.getDinerId()) || (orderPayload.getDinerId() != null && orderPayload.getDinerId().equals("-1"))) {
            this.dinerId = session.getDiners().get(0).getId();
        } else{
            this.dinerId = orderPayload.getDinerId();
        }
        if(StringUtils.isBlank(orderPayload.getCourseId()) || (orderPayload.getCourseId() != null && orderPayload.getCourseId().equals("-1"))) {
            this.courseId = session.getService().getCourses().get(0).getId();
        } else {
            this.courseId = orderPayload.getCourseId();
        }
        this.note = orderPayload.getNote();
        this.instantiatedFrom = ActivityInstantiationConstant.WAITER;
        this.staffId = staffId;
    }

    public Order(String sessionId,
                 OrderRequest orderRequest,
                 List<Modifier> modifiers,
                 MenuItem item,
                 TaxRate taxRate,
                 String staffId) {
        this.sessionId = sessionId;
        this.time = System.currentTimeMillis();
        this.menuItemId = orderRequest.getMenuItemId();
        this.modifiers = modifiers;
        this.courseId = orderRequest.getCourseId();
        this.menuItem = item;
        this.itemPrice = menuItem.getPrice();
        this.priceOverride = orderRequest.getPriceOverride() == null ? menuItem.getPrice() : MoneyService.toPenniesRoundNearest(orderRequest.getPriceOverride());
        this.instantiatedFrom = ActivityInstantiationConstant.valueOf(orderRequest.getInstantiatedFromId());
        this.dinerId = orderRequest.getDinerId();
        this.note = orderRequest.getNote() == null ? "" : orderRequest.getNote();
        this.taxRate = taxRate;
        if(orderRequest.getQuantity() > 0) {
            this.quantity = orderRequest.getQuantity();
        }
        this.staffId = staffId;
    }

    public Order(Order order) {
        setId(order.getId());
        setDeleted(order.getDeleted());
        this.sessionId = order.getSessionId();
        this.time = order.getTime();
        this.menuItemId = order.getMenuItemId();
        this.modifiers.addAll(order.getModifiers());
        this.note = order.getNote();
        this.completed = order.getCompleted();
        this.courseId = order.getCourseId();
        this.itemPrice = order.getItemPrice();
        this.priceOverride = order.getPriceOverride();
        this.quantity = order.getQuantity();
        this.instantiatedFrom = order.getInstantiatedFrom();
        this.adjustment = order.getAdjustment();
        this.voided = order.isVoided();
        this.staffId = order.getStaffId();
        this.dinerId = order.getDinerId();
        this.removeFromReports = order.isRemoveFromReports();
        this.menuItem = order.getMenuItem();
        this.doneTime = order.getDoneTime();
        this.taxRate = order.getTaxRate();
        this.publicFacingOrderId = order.getPublicFacingOrderId();
        this.deliveryLocation = order.getDeliveryLocation();
    }

    public Order copy() {
        Order order = new Order();
        order.setDeleted(getDeleted());
        order.setSessionId(sessionId);
        order.setTime(time);
        order.setMenuItemId(menuItemId);
        order.setModifiers(modifiers);
        order.setNote(note);
        order.setCourseId(courseId);
        order.setCompleted(completed);
        order.setItemPrice(itemPrice);
        order.setPriceOverride(priceOverride);
        order.setQuantity(quantity);
        order.setInstantiatedFrom(instantiatedFrom);
        order.setAdjustment(adjustment);
        order.setVoided(voided);
        order.setStaffId(staffId);
        order.setDinerId(dinerId);
        order.setRemoveFromReports(removeFromReports);
        order.setMenuItem(menuItem);
        order.setDoneTime(doneTime);
        order.setTaxRate(taxRate);
        order.setPublicFacingOrderId(publicFacingOrderId);
        order.setDeliveryLocation(deliveryLocation);
        return order;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(int priceOverride) {
        this.priceOverride = priceOverride;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ActivityInstantiationConstant getInstantiatedFrom() {
        return instantiatedFrom;
    }

    public void setInstantiatedFrom(ActivityInstantiationConstant instantiatedFrom) {
        this.instantiatedFrom = instantiatedFrom;
    }

    public Adjustment getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Adjustment adjustment) {
        this.adjustment = adjustment;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public boolean isRemoveFromReports() {
        return removeFromReports;
    }

    public void setRemoveFromReports(boolean removeFromReports) {
        this.removeFromReports = removeFromReports;
    }

    public String getDinerId() {
        return dinerId;
    }

    public void setDinerId(String dinerId) {
        this.dinerId = dinerId;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public Long getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(Long doneTime) {
        this.doneTime = doneTime;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public String getPublicFacingOrderId() {
        return publicFacingOrderId;
    }

    public void setPublicFacingOrderId(String publicFacingOrderId) {
        this.publicFacingOrderId = publicFacingOrderId;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public int hashCode(String... exclude) {
        if(Arrays.asList(exclude).contains("modifiers")) {
            exclude = Arrays.copyOf(exclude, exclude.length+1);
            exclude[exclude.length-1] = "modifiers";
        }
        int code = HashCodeBuilder.reflectionHashCode(this, exclude);
        if(modifiers != null) {
            for (Modifier modifier : modifiers) {
                code += modifier.hashCode("id");
            }
        }

        return code;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
