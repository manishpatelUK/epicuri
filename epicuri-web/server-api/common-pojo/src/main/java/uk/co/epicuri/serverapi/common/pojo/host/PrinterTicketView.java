package uk.co.epicuri.serverapi.common.pojo.host;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish.
 */
public class PrinterTicketView implements Comparable<PrinterTicketView>{
    private String orderId;
    private String menuItemId;
    private String menuItemName;
    private int quantity = 1;
    private boolean done;
    private Long doneTime;
    private String note;
    private List<String> modifiers;
    private long creationTime;

    public PrinterTicketView() {}

    public PrinterTicketView(Order order, MenuItem menuItem) {
        this.orderId = order.getId();
        this.menuItemId = order.getMenuItemId();
        this.menuItemName = StringUtils.isNotBlank(menuItem.getShortCode()) ? menuItem.getShortCode() : menuItem.getName();
        if(order.getPublicFacingOrderId() != null) {
            this.menuItemName += " [" + order.getPublicFacingOrderId() + "]";
        }
        this.quantity = order.getQuantity();
        this.done = order.getDoneTime() != null;
        if(done) {
            this.doneTime = order.getDoneTime();
        }
        this.note = order.getNote();
        if(order.getDeliveryLocation() != null && order.getInstantiatedFrom() != null && ActivityInstantiationConstant.isSelfService(order.getInstantiatedFrom())) {
            if(this.note == null) {
                this.note = "";
            }
            this.note += " -- Deliver to: " + order.getDeliveryLocation();
        }
        if(order.getModifiers().size() > 0) {
            modifiers = new ArrayList<>();
            order.getModifiers().forEach(m -> modifiers.add(m.getModifierValue()));
        }
        this.creationTime = order.getTime();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Long getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(Long doneTime) {
        this.doneTime = doneTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public int compareTo(PrinterTicketView o) {
        //sort by ticket done first -- "more than"
        if(this.done && !o.isDone()) {
            return 1;
        } else if (!this.done && o.isDone()) {
            return -1;
        }

        return Long.compare(creationTime, o.creationTime);
    }
}
