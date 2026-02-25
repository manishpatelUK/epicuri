package uk.co.epicuri.bookingapi.endpoints.booking;

import com.exxeleron.qjava.QConnection;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Reservation;
import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;
import uk.co.epicuri.bookingapi.email.SendReservationEmailThread;
import uk.co.epicuri.bookingapi.endpoints.auth.AbstractSecurityConnectingResource;
import uk.co.epicuri.bookingapi.pojo.BookingRequest;
import uk.co.epicuri.bookingapi.pojo.EmailConfirmRequest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("booking/{id}")
public class Reserve extends AbstractSecurityConnectingResource {
    private final EpicuriAPI api;
    private final QConnection tickerplantConnection;
    private final QConnection staticsConnection;
    private final ExecutorService executorService;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy.MM.dd");

    public Reserve(EpicuriAPI api, QConnection securityConnection,QConnection tickerplantConnection, QConnection staticsConnection)  {
        super(securityConnection);
        this.api = api;
        this.tickerplantConnection = tickerplantConnection;
        this.staticsConnection = staticsConnection;

        this.executorService = Executors.newCachedThreadPool();
    }

    @POST
    @Path("/reserve")
    public BookingRequest reserve(@NotNull @PathParam("id") String id,
                                  BookingRequest request,
                                  @QueryParam("tz") String timezone) {

        return tokeniseAndReserve(id, request, timezone, true);
    }

    @POST
    @Path("/reservecheck")
    public BookingRequest reservecheck(@NotNull @PathParam("id") String id,
                                  BookingRequest request,
                                  @QueryParam("tz") String timezone) {

        return tokeniseAndReserve(id, request, timezone, false);
    }

    private BookingRequest tokeniseAndReserve(String id, BookingRequest request, String timezone, boolean commitReservation) {
        Restaurant restaurant;
        String token;
        try {
            restaurant = api.getRestaurant(Integer.parseInt(id));
            token = getToken(request.getToken());
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        catch (NumberFormatException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        if(restaurant == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if(StringUtils.isBlank(token)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        String[] bits = request.getTime().split(":");
        int hours = Integer.parseInt(bits[0]);
        int minutes = Integer.parseInt(bits[1]);

        DateTimeZone tz = restaurant.getTimezone() == null ? DateTimeZone.forID(timezone.replaceAll("\"","")) : DateTimeZone.forID(restaurant.getTimezone());

        DateTime requestedDate = DTF.parseDateTime(request.getDate());

        DateTime requestedLocalTime = new DateTime(tz).withDate(requestedDate.getYear(),requestedDate.getMonthOfYear(),requestedDate.getDayOfMonth()).withTime(hours, minutes,0,0);
        DateTime requestedTimeUTC = requestedLocalTime.toDateTime(DateTimeZone.UTC);
        DateTime nowLocal = new DateTime(tz);

        if (requestedLocalTime.isBefore(nowLocal)) {
            request.setAccepted(false);
            throw new WebApplicationException("time in past", Response.Status.BAD_REQUEST);
        }

        if (requestedLocalTime.isBefore(nowLocal.plusHours(2))) {
            request.setAccepted(false);
            throw new WebApplicationException("time too soon", Response.Status.BAD_REQUEST);
        }


        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setReservationTime(requestedTimeUTC.getMillis() / 1000);
        reservationRequest.setEmail(request.getEmail());
        if(commitReservation) {
            reservationRequest.setName(request.getName());
            reservationRequest.setNotes(request.getNotes());
            reservationRequest.setNumberOfPeople(request.getNumberOfPeople());
            reservationRequest.setTelephone(request.getTelephone());
        }
        else {
            reservationRequest.setName("test");
            reservationRequest.setNotes("");
            reservationRequest.setNumberOfPeople(2);
            reservationRequest.setTelephone("0");
        }

        Reservation try1 = api.createReservation(reservationRequest, token, id, false);
        if(try1.isRejected() || !try1.isAccepted() || StringUtils.isNotBlank(try1.getRejectionNotice())) {
            throw new WebApplicationException("tolerance breach",Response.Status.BAD_REQUEST);
        }

        if(commitReservation) {
            try {
                Reservation reservation = api.createReservation(reservationRequest, token, String.valueOf(id), true);
                if (reservation.isAccepted()) {
                    executorService.submit(new SendReservationEmailThread(requestedLocalTime, reservationRequest, restaurant,
                                            tickerplantConnection, staticsConnection, request.getLanguage()));
                    request.setAccepted(true);
                    return request;
                } else {
                    request.setAccepted(false);
                    request.setMessage(reservation.getRejectionNotice());
                }
            } catch (Exception ex) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        return request;
    }

    @POST
    @Path("/confirm")
    public void emailConfirmation(@NotNull @PathParam("id") String id, EmailConfirmRequest emailConfirmRequest){
        DateTime requestedTimeUTC = new DateTime(DateTimeZone.UTC).withMillis(emailConfirmRequest.getReservationRequest().getReservationTime());
        DateTime requestedLocalTime = requestedTimeUTC.toDateTime(DateTimeZone.forID(emailConfirmRequest.getRestaurant().getTimezone().replaceAll("\"", "")));
        /*executorService.submit(new SendReservationEmailThread(requestedLocalTime, emailConfirmRequest.getReservationRequest(), emailConfirmRequest.getRestaurant(),
                                tickerplantConnection, staticsConnection, emailConfirmRequest.getLanguage()));*/
        new Thread(new SendReservationEmailThread(requestedLocalTime, emailConfirmRequest.getReservationRequest(), emailConfirmRequest.getRestaurant(),
                tickerplantConnection, staticsConnection, emailConfirmRequest.getLanguage())).start();
    }
}
