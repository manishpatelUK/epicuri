package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {
    @JsonProperty("MenuItemId")
    private String menuItemId;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("DinerId")
    private String dinerId;

    @JsonProperty("CourseId")
    private String courseId;

    @JsonProperty("Modifiers")
    private List<String> modifiers = new ArrayList<>();

    @JsonProperty("Note")
    private String note;

    @JsonProperty("PriceOverride")
    private Double priceOverride; //not currently used

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("InstantiatedFromId")
    private int instantiatedFromId;

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
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

    public Double getPriceOverride() {
        return priceOverride;
    }

    public void setPriceOverride(Double priceOverride) {
        this.priceOverride = priceOverride;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getInstantiatedFromId() {
        return instantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        this.instantiatedFromId = instantiatedFromId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "menuItemId='" + menuItemId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", dinerId='" + dinerId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", modifiers=" + modifiers +
                ", note='" + note + '\'' +
                ", priceOverride=" + priceOverride +
                ", quantity=" + quantity +
                ", instantiatedFromId=" + instantiatedFromId +
                '}';
    }
}
