package uk.co.epicuri.bookingapi.endpoints.booking;

import com.codahale.metrics.annotation.Timed;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QException;
import com.exxeleron.qjava.QTable;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Restaurant;
import uk.co.epicuri.bookingapi.pojo.BookingStatics;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("booking/{id}")
public class Statics {

    private final EpicuriAPI api;
    private final QConnection staticsDBConnection;
    private final QConnection securityDBConnection;

    public Statics(EpicuriAPI api, QConnection securityDBConnection, QConnection staticsDBConnection) {
        this.api = api;
        this.staticsDBConnection = staticsDBConnection;
        this.securityDBConnection = securityDBConnection;
    }

    @POST
    @Path("/statics")
    public BookingStatics statics(@NotNull @PathParam("id") String id, BookingStatics request) {
        return tokeniseAndGetStatics(id, request);
    }

    private BookingStatics tokeniseAndGetStatics(String id, BookingStatics request) {
        // check if there's a valid token
        String internalToken = "";
        try {
            internalToken = new String((char[])securityDBConnection.sync("getInternalTokenById", id));
            if(StringUtils.isBlank(internalToken)) {
                request.setToken(authenticate(id));
                return getBookingStatics(id,request);
            }
            else {
                request.setToken(internalToken);
                return getBookingStatics(id, request);
            }
        } catch (QException | IOException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String authenticate(String id) throws IOException, QException {
        Authentication authentication = api.login(id, "epicuriadmin", "keshavroshan");
        if (authentication == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        else {
            String remoteToken = authentication.getAuthKey();
            return new String ((char[])securityDBConnection.sync("insertPass", id, remoteToken.toCharArray()));
        }
    }

    private BookingStatics getBookingStatics(String id, BookingStatics request) {
        BookingStatics statics = new BookingStatics();

        try {
            Restaurant restaurant = api.getRestaurant(Integer.parseInt(id));
            statics.setEntityName(restaurant.getName());
            statics.setEntityNumber(restaurant.getPhoneNumber());
            statics.setEntityEmail(restaurant.getEmail());
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        try {
            String lang = "en"; //default
            if(request != null && StringUtils.isNotBlank(request.getLanguage())) {
                lang = request.getLanguage();
            }
            QTable table = (QTable) staticsDBConnection.sync("getStatics",lang);
            QTable.Row row = table.get(0);

            statics.setLanguage((String)row.get(0));
            statics.setBackLabel(new String((char[]) row.get(1)));
            statics.setDateStepMessage(new String((char[]) row.get(2)));
            statics.setDateStepButtonLabel(new String((char[]) row.get(3)));
            statics.setTimeStepMessage(new String((char[]) row.get(4)));
            statics.setTimeStepButtonLabel(new String((char[]) row.get(5)));
            statics.setDetailsStepMessage(new String((char[]) row.get(6)));
            statics.setDetailsStepButtonLabel(new String((char[]) row.get(7)));
            statics.setNameLabel(new String((char[]) row.get(8)));
            statics.setNumberLabel(new String((char[]) row.get(9)));
            statics.setEmailLabel(new String((char[]) row.get(10)));
            statics.setNotesLabel(new String((char[]) row.get(11)));
            statics.setConfirmationMessage(new String((char[]) row.get(12)));
            statics.setTelephoneValidationError(new String((char[]) row.get(13)));
            statics.setNameValidationError(new String((char[]) row.get(14)));
            statics.setTitle(new String((char[]) row.get(15)));
            statics.setMarketingMessage(new String((char[]) row.get(16)));
            statics.setConfirmationStepMessage(new String((char[]) row.get(17)));
            statics.setBiggerPartiesLabel(new String((char[]) row.get(18)));
            statics.setToleranceBreachMessage(new String((char[]) row.get(19)));
            statics.setTimeInPastMessage(new String((char[]) row.get(20)));
            statics.setNoTimeSlotsMessage(new String((char[]) row.get(21)));
            statics.setNumberGuestsLabel(new String((char[]) row.get(22)));

        } catch (QException | IOException | ClassCastException e) {
            e.printStackTrace();
            appendDefaultEnglish(statics);
        }

        assert request != null;
        statics.setToken(request.getToken());
        return statics;
    }

    private void appendDefaultEnglish(BookingStatics statics) {
        statics.setBackLabel("Back");
        statics.setDateStepMessage("Date of booking and size of party");
        statics.setDateStepButtonLabel("Next");
        statics.setTimeStepMessage("What time would you like to book?");
        statics.setTimeStepButtonLabel("Next");
        statics.setDetailsStepMessage("Just a few more details...");
        statics.setDetailsStepButtonLabel("Book!");
        statics.setNameLabel("Name");
        statics.setNumberLabel("Number");
        statics.setEmailLabel("Email");
        statics.setNotesLabel("Notes");
        statics.setConfirmationMessage("Thanks! See you soon!");
        statics.setTelephoneValidationError("Valid telephone number is required!");
        statics.setNameValidationError("Name is required!");
        statics.setTitle("Book your table");
    }
}
