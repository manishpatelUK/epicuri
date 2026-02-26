package uk.co.epicuri.bookingapi.endpoints.auth;

import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QException;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.bookingapi.pojo.LogonResponse;
import uk.co.epicuri.bookingapi.pojo.auth.LoginRequest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("{id}")
public class LogonController {
    private final EpicuriAPI api;
    private final QConnection securityDBConnection;

    public LogonController(EpicuriAPI api, QConnection securityDBConnection) {
        this.api = api;
        this.securityDBConnection = securityDBConnection;
    }

    @GET
    @Path("/logon")
    public LogonResponse logon(@NotNull @PathParam("id") String id) {
        // check if there's a valid token
        try {
            String internalToken = new String((char[])securityDBConnection.sync("getInternalTokenById", id));
            if(StringUtils.isBlank(internalToken)) {
                return authenticate(id, "epicuriadmin", "keshavroshan");
            }
            else {
                LogonResponse response = new LogonResponse();
                response.setToken(internalToken);
                return response;
            }
        } catch (QException | IOException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/authentication")
    public LogonResponse authentication(@PathParam("id") String id, @NotNull LoginRequest login) {
        return authenticate(id,login.getUsername(),login.getPassword());
    }


    private LogonResponse authenticate(String id, String username, String password) {
        Authentication authentication = api.login(id, username, password);
        if (authentication == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        else {
            LogonResponse response = new LogonResponse();
            String remoteToken = authentication.getAuthKey();
            try {
                response.setToken(new String ((char[])securityDBConnection.sync("insertPass", id, remoteToken.toCharArray())));
            } catch (QException | IOException e) {
                e.printStackTrace();
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
            return response;
        }
    }
}
