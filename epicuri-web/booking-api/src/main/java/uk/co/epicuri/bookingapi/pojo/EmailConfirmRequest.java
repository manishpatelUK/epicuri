package uk.co.epicuri.bookingapi.pojo;

import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;

/**
 * Created by Manish on 11/06/2015.
 */
public class EmailConfirmRequest {
    private ReservationRequest reservationRequest;
    private Restaurant restaurant;
    private String language = "en";

    public ReservationRequest getReservationRequest() {
        return reservationRequest;
    }

    public void setReservationRequest(ReservationRequest reservationRequest) {
        this.reservationRequest = reservationRequest;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


}
