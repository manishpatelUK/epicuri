package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaitingPartyOrderPayload {

    @JsonProperty("Party")
    private WaitingPartyPayload party;

    @JsonProperty("Order")
    private List<OrderPayload> order = new ArrayList<>();

    @JsonProperty("OrderLocation")
    private String orderLocation;

    public WaitingPartyPayload getParty() {
        return party;
    }

    public void setParty(WaitingPartyPayload party) {
        this.party = party;
    }

    public List<OrderPayload> getOrder() {
        return order;
    }

    public void setOrder(List<OrderPayload> order) {
        this.order = order;
    }

    public String getOrderLocation() {
        return orderLocation;
    }

    public void setOrderLocation(String orderLocation) {
        this.orderLocation = orderLocation;
    }

    public static class OrderPayload {
        @JsonProperty("Quantity")
        private int quantity = 0;

        @JsonProperty("MenuItemId")
        private String menuItemId;

        @JsonProperty("InstantiatedFromId")
        private int instantiatedFromId;

        @JsonProperty("Modifiers")
        private List<String> modifiers = new ArrayList<>();

        @JsonProperty("Price")
        private Double price;

        @JsonProperty("DinerId")
        private String dinerId;

        @JsonProperty("CourseId")
        private String courseId;

        @JsonProperty("Note")
        private String note;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(String menuItemId) {
            this.menuItemId = menuItemId;
        }

        public int getInstantiatedFromId() {
            return instantiatedFromId;
        }

        public void setInstantiatedFromId(int instantiatedFromId) {
            this.instantiatedFromId = instantiatedFromId;
        }

        public List<String> getModifiers() {
            return modifiers;
        }

        public void setModifiers(List<String> modifiers) {
            this.modifiers = modifiers;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
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

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

}
