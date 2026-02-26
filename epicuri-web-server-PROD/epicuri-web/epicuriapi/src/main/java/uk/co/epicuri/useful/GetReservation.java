package uk.co.epicuri.useful;

import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Reservation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GetReservation {
    public static void main(String[] args) {
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Authentication authentication = api.login("11","epicuriadmin","keshavroshan");
        System.out.println(authentication.getAuthKey());
        List<Reservation> reservations = api.getReservations(authentication.getAuthKey(), "1456790400", "1477958400", true);
        reservations.addAll(api.getReservations(authentication.getAuthKey(), "1456790400", "1477958400", true));
        Set<Reservation> set = new TreeSet<>(reservations);
        DateFormat format = new SimpleDateFormat("YYYY.MM.dd HH:mm");
        for(Reservation reservation : set) {
            if (reservation.isDeleted()) {
                continue;
            }

            long actualMillis = (long)reservation.getReservationTime() * 1000;
            Date date = new Date(actualMillis);
            System.out.println(reservation.getName()
                    + "|" + format.format(date)
                    + "|" + reservation.getNumberOfPeople()
                    + "|" + reservation.getTelephone()
                    + "|" + reservation.getNotes());
        }

    }
}
