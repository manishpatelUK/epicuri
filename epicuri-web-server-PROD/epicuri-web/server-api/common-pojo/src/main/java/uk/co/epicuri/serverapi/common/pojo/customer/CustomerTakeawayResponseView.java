package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerTakeawayResponseView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ReadableSessionId")
    private String readableId;

    @JsonProperty("Restaurant")
    private CustomerRestaurantView restaurant;

    @JsonProperty("ExpectedTime")
    private long expectedTime;

    @JsonProperty("ClosedTime")
    private long closedTime;

    @JsonProperty("Accepted")
    private boolean accepted;

    @JsonProperty("Rejected")
    private boolean rejected;

    @JsonProperty("Delivery")
    private boolean delivery;

    @JsonProperty("RejectionNotice")
    private String rejectionNotice;

    @JsonProperty("TakeawayOrder")
    private TakeawayDetail takeawayDetail;

    @JsonProperty("isPaid")
    private boolean paid;

    public CustomerTakeawayResponseView(){};

    public CustomerTakeawayResponseView(Session session,
                                        Booking booking,
                                        List<Order> orders,
                                        Restaurant restaurant,
                                        Map<CalculationKey, Number> calculations,
                                        boolean paid) {
        id = booking.getId();
        readableId = session.getReadableId();
        this.restaurant = new CustomerRestaurantView(restaurant);
        expectedTime = booking.getTargetTime() / 1000;
        closedTime = session.getClosedTime() == null ? 0 : session.getClosedTime() / 1000;
        accepted = booking.isAccepted();
        rejected = booking.isRejected();
        delivery = booking.getTakeawayType() == TakeawayType.DELIVERY;
        rejectionNotice = booking.getRejectionNotice();

        takeawayDetail = new TakeawayDetail();
        takeawayDetail.setDeleted(booking.isCancelled());
        int totalItems = 0;
        for(Order order : orders) {
            if(!(order.isRemoveFromReports() || order.isVoided())) {
                totalItems += order.getQuantity();
            }
        }
        takeawayDetail.setItemCount(totalItems);
        takeawayDetail.setNotes(booking.getNotes());

        int subTotal = calculations.get(CalculationKey.SUB_TOTAL).intValue();
        int discountTotal = calculations.get(CalculationKey.DISCOUNT_TOTAL).intValue();
        int total = calculations.get(CalculationKey.TOTAL).intValue();
        takeawayDetail.setSubTotal(MoneyService.toMoneyRoundHalfUp(subTotal));
        takeawayDetail.setDiscounts(MoneyService.toMoneyRoundHalfUp(discountTotal));
        takeawayDetail.setTotal(MoneyService.toMoneyRoundHalfUp(total));
        if(session.getCalculatedDeliveryCost() != null) {
            takeawayDetail.setDeliveryCost(MoneyService.toMoneyRoundHalfUp(session.getCalculatedDeliveryCost()));
        } else {
            takeawayDetail.setDeliveryCost(0D);
        }
        this.paid = paid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomerRestaurantView getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(CustomerRestaurantView restaurant) {
        this.restaurant = restaurant;
    }

    public long getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(long expectedTime) {
        this.expectedTime = expectedTime;
    }

    public long getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(long closedTime) {
        this.closedTime = closedTime;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public String getRejectionNotice() {
        return rejectionNotice;
    }

    public void setRejectionNotice(String rejectionNotice) {
        this.rejectionNotice = rejectionNotice;
    }

    public TakeawayDetail getTakeawayDetail() {
        return takeawayDetail;
    }

    public void setTakeawayDetail(TakeawayDetail takeawayDetail) {
        this.takeawayDetail = takeawayDetail;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getReadableId() {
        return readableId;
    }

    public void setReadableId(String readableId) {
        this.readableId = readableId;
    }

    public static class TakeawayDetail {
        @JsonProperty("RestaurantId")
        private String restaurantId;

        @JsonProperty("Deleted") //i.e. cancelled
        private boolean deleted;

        @JsonProperty("ItemCount")
        private int itemCount;

        @JsonProperty("SubTotal")
        private Double subTotal;

        @JsonProperty("Discounts")
        private Double discounts;

        @JsonProperty("DeliveryCost")
        private Double deliveryCost;

        @JsonProperty("Total")
        private Double total;

        @JsonProperty("Notes")
        private String notes;

        public String getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(String restaurantId) {
            this.restaurantId = restaurantId;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public Double getSubTotal() {
            return subTotal;
        }

        public void setSubTotal(Double subTotal) {
            this.subTotal = subTotal;
        }

        public Double getDiscounts() {
            return discounts;
        }

        public void setDiscounts(Double discounts) {
            this.discounts = discounts;
        }

        public Double getDeliveryCost() {
            return deliveryCost;
        }

        public void setDeliveryCost(Double deliveryCost) {
            this.deliveryCost = deliveryCost;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
