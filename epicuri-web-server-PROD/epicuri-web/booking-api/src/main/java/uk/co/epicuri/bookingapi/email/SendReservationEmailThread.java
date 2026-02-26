package uk.co.epicuri.bookingapi.email;

import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QException;
import com.exxeleron.qjava.QTable;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import uk.co.epicuri.bookingapi.email.HTMLEmailBuilder;

import java.io.IOException;

/**
 * 01/05/2015
 */
public class SendReservationEmailThread implements Runnable{
    private final String restaurantId;
    private final DateTime requestedTime;
    private final ReservationRequest reservationRequest;
    private final Restaurant restaurant;
    private final String dinerEmail;
    private final String dinerPhoneNumber;
    private final QConnection tickerplantConnection;
    private final QConnection staticsConnection;
    private final String language;
    private static final DateTimeFormatter dtf1 = DateTimeFormat.forPattern("dd MMM yyyy");
    private static final DateTimeFormatter dtf2 = DateTimeFormat.forPattern("HH:mm");

    public SendReservationEmailThread(DateTime requestedTime,
                                      ReservationRequest reservationRequest,
                                      Restaurant restaurant,
                                      QConnection tickerplantConnection,
                                      QConnection staticsConnection,
                                      String language) {
        this.restaurantId = String.valueOf(restaurant.getId());
        this.requestedTime = requestedTime;
        this.reservationRequest = reservationRequest;
        this.restaurant = restaurant;
        this.dinerEmail = reservationRequest.getEmail();
        this.dinerPhoneNumber = reservationRequest.getTelephone();
        this.tickerplantConnection = tickerplantConnection;
        this.staticsConnection = staticsConnection;
        this.language = language;
    }

    public void run() {
        if(StringUtils.isNotBlank(dinerEmail)) {
            try {
                sendEmailToDiner(restaurant, requestedTime, reservationRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                int rId = Integer.parseInt(restaurantId);
                tickerplantConnection.async(".u.upd", "reservationRequests", new Object[]{"dineremail", restaurantId, dinerEmail, reservationRequest.getName(), reservationRequest.getTelephone(),
                        reservationRequest.getNotes(), reservationRequest.getReservationTime(), reservationRequest.getNumberOfPeople(),
                        reservationRequest.getLeadCustomerId()});
            } catch (QException | IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        try {
            if (StringUtils.isNotBlank(restaurant.getEmail())) {
                sendEmailToRestaurant(restaurant, restaurant.getEmail(), requestedTime, reservationRequest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void sendEmailToDiner(Restaurant restaurant, DateTime requestTime, ReservationRequest request) {
        String fromEmail = StringUtils.isBlank(restaurant.getEmail()) ? restaurant.getEmail() : "no-reply@epicuri.email";

        HTMLEmailBuilder emailBuilder = HTMLEmailBuilder.newInstance()
                .recipient(request.getName() + " <" + dinerEmail + ">")
                .sender(restaurant.getName() + " <" + fromEmail + ">");

        try {
            QTable table = (QTable) staticsConnection.sync("getEmailStatics",language,"diner");
            for(int i = 0; i < table.getRowsCount(); i++) {
                QTable.Row row = table.get(i);
                String text = String.valueOf((char[]) row.get(table.getColumnIndex("text")));
                String identifier = (String) row.get(table.getColumnIndex("identifier"));

                if(identifier.equals("rep")) {
                    emailBuilder = emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("misc")) {
                    emailBuilder = emailBuilder.line(text);
                }
                else if(identifier.equals("subject")) {
                    emailBuilder.subject(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("address1") && StringUtils.isNotBlank(restaurant.getAddress().getStreet())) {
                    emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("address2") && StringUtils.isNotBlank(restaurant.getAddress().getTown())) {
                    emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("address3") && StringUtils.isNotBlank(restaurant.getAddress().getCity())) {
                    emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("address4") && StringUtils.isNotBlank(restaurant.getAddress().getPostCode())) {
                    emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
            }
        } catch (QException | IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            emailBuilder.send();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String replace(Restaurant restaurant, DateTime requestTime, ReservationRequest request, String original) {
        return original.replace("$dinerName$",request.getName())
                    .replace("$numberOfPeople$",String.valueOf(request.getNumberOfPeople()))
                    .replace("$restaurantName$",restaurant.getName())
                    .replace("$date$", dtf1.print(requestTime))
                    .replace("$time$",dtf2.print(requestTime))
                    .replace("$addressLine1$",StringUtils.isNotBlank(restaurant.getAddress().getStreet()) ? restaurant.getAddress().getStreet() : "")
                    .replace("$addressLine2$",StringUtils.isNotBlank(restaurant.getAddress().getTown()) ? restaurant.getAddress().getTown() : "")
                    .replace("$addressLine3$",StringUtils.isNotBlank(restaurant.getAddress().getCity()) ? restaurant.getAddress().getCity() : "")
                    .replace("$addressLine4$",StringUtils.isNotBlank(restaurant.getAddress().getPostCode()) ? restaurant.getAddress().getPostCode() : "")
                    .replace("$restaurantNumber$",StringUtils.isNotBlank(restaurant.getPhoneNumber()) ? restaurant.getPhoneNumber() : "")
                    .replace("$restaurantEmail$",StringUtils.isNotBlank(restaurant.getEmail()) ? restaurant.getEmail() : "")
                    .replace("$notes$", StringUtils.isNotBlank(request.getNotes()) ? request.getNotes() : "")
                    .replace("$dinerEmail$",StringUtils.isNotBlank(dinerEmail) ? dinerEmail : "")
                    .replace("$dinerNumber$",dinerPhoneNumber);
    }

    private void sendEmailToRestaurant(Restaurant restaurant, String restaurantEmailAddress, DateTime requestTime, ReservationRequest request) {
        String fromEmail = "no-reply@epicuri.email";
        HTMLEmailBuilder emailBuilder = HTMLEmailBuilder.newInstance()
                .recipient(request.getName() + " <" + restaurantEmailAddress + ">")
                .sender("Epicuri Booking Manager <" + fromEmail + ">");

        try {
            QTable table = (QTable) staticsConnection.sync("getEmailStatics",language,"restaurant");
            for(int i = 0; i < table.getRowsCount(); i++) {
                QTable.Row row = table.get(i);
                String text = String.valueOf((char[]) row.get(table.getColumnIndex("text")));
                String identifier = (String) row.get(table.getColumnIndex("identifier"));

                if(identifier.equals("rep")) {
                    emailBuilder.line(replace(restaurant, requestTime, request, text));
                }
                else if(identifier.equals("misc")) {
                    emailBuilder.line(text);
                }
                else if(identifier.equals("subject")) {
                    emailBuilder.subject(replace(restaurant, requestTime, request, text));
                }
            }
        } catch (QException | IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            emailBuilder.send();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
