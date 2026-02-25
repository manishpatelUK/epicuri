package uk.co.epicuri.bookingapi.endpoints.booking;

import com.exxeleron.qjava.QConnection;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Restaurant;
import uk.co.epicuri.bookingapi.endpoints.auth.AbstractSecurityConnectingResource;
import uk.co.epicuri.bookingapi.pojo.TimeSlotsRequest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("booking/{id}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TimeSlots extends AbstractSecurityConnectingResource {
    private final EpicuriAPI api;
    private final QConnection staticsDBConnection;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy.MM.dd");
    private static final Map<Integer,String> ALL_TIME_SLOTS = generateAllSlots();

    private static Map<Integer,String> generateAllSlots() {
        Map<Integer,String> allSlots = new HashMap<>();
        allSlots.put(1000,"10:00");
        allSlots.put(1030,"10:30");
        allSlots.put(1100,"11:00");
        allSlots.put(1130,"11:30");
        allSlots.put(1200,"12:00");
        allSlots.put(1230,"12:30");
        allSlots.put(1300,"13:00");
        allSlots.put(1330,"13:30");
        allSlots.put(1400,"14:00");
        allSlots.put(1430,"14:30");
        allSlots.put(1500,"15:00");
        allSlots.put(1530,"15:30");
        allSlots.put(1600,"16:00");
        allSlots.put(1630,"16:30");
        allSlots.put(1700,"17:00");
        allSlots.put(1730,"17:30");
        allSlots.put(1800,"18:00");
        allSlots.put(1830,"18:30");
        allSlots.put(1930,"19:30");
        allSlots.put(2000,"20:00");
        allSlots.put(2030,"20:30");
        allSlots.put(2100,"21:00");
        allSlots.put(2130,"21:30");
        allSlots.put(2200,"22:00");
        allSlots.put(2230,"22:30");

        return Collections.unmodifiableMap(allSlots);
    }

    public TimeSlots(EpicuriAPI api, QConnection securityConnection, QConnection staticsDBConnection) {
        super(securityConnection);
        this.api = api;
        this.staticsDBConnection = staticsDBConnection;
    }

    @POST
    @Path("/timeslots")
    public TimeSlotsRequest timeSlots(@NotNull @PathParam("id") String id,
                                      @NotNull TimeSlotsRequest request,
                                      @QueryParam("tz") String timezone) {
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

        DateTime requestedDate = DTF.parseDateTime(request.getDate());
        DateTimeZone tz = restaurant.getTimezone() == null ? DateTimeZone.forID(timezone.replaceAll("\"","")) : DateTimeZone.forID(restaurant.getTimezone());
        // get the current UTC time
        DateTime utcTime = new DateTime(DateTimeZone.UTC);
        // convert it to local time of restaurant
        DateTime localTime = utcTime.toDateTime(tz);

        //if requested date is today, show a partial list, else show all
        if(requestedDate.toLocalDate().isEqual(localTime.withZone(requestedDate.getZone()).toLocalDate())) {
            // omit times from beginning of day to now + 1 hour
            DateTime localTimePlus1 = localTime.plusHours(2).plusMinutes(2);
            int contrivedTime = Integer.parseInt(String.valueOf(localTimePlus1.getHourOfDay())
                                +
                                (localTimePlus1.getMinuteOfHour() < 10 ? "00" : String.valueOf(localTimePlus1.getMinuteOfHour())));

            TreeSet<String> set = new TreeSet<>();
            for(int key : ALL_TIME_SLOTS.keySet()) {
                if(key >= contrivedTime) {
                    set.add(ALL_TIME_SLOTS.get(key));
                }
            }
            request.setTimes(new ArrayList<>(set));
        }
        // if time is before today, error
        else if(requestedDate.toLocalDate().isBefore(localTime.withZone(requestedDate.getZone()).toLocalDate())) {
            throw new WebApplicationException("time in past", Response.Status.BAD_REQUEST);
        }
        else {
            request.setTimes(new ArrayList<>(new TreeSet<>(ALL_TIME_SLOTS.values())));
        }

        return request;
    }
}
