package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LocationAndCustomerView {
    @JsonProperty("nextReservation")
    private CustomerReservationView nextReservation;

    @JsonProperty("nextTakeaway")
    private CustomerTakeawayResponseView nextTakeaway;

    @JsonProperty("nearestRestaurants")
    private List<CustomerRestaurantView> nearestRestaurants;

    public CustomerReservationView getNextReservation() {
        return nextReservation;
    }

    public void setNextReservation(CustomerReservationView nextReservation) {
        this.nextReservation = nextReservation;
    }

    public CustomerTakeawayResponseView getNextTakeaway() {
        return nextTakeaway;
    }

    public void setNextTakeaway(CustomerTakeawayResponseView nextTakeaway) {
        this.nextTakeaway = nextTakeaway;
    }

    public List<CustomerRestaurantView> getNearestRestaurants() {
        return nearestRestaurants;
    }

    public void setNearestRestaurants(List<CustomerRestaurantView> nearestRestaurants) {
        this.nearestRestaurants = nearestRestaurants;
    }
}
