package uk.co.epicuri.bookingapi.endpoints.auth;

import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * 03/05/2015
 */
public abstract class AbstractSecurityConnectingResource {

    private final QConnection securityConnection;

    public AbstractSecurityConnectingResource(QConnection securityConnection) {
        this.securityConnection = securityConnection;
    }

    protected synchronized String getToken(String key) {
        try {
            return new String((char[])securityConnection.sync("getToken", key.toCharArray()));
        } catch (QException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected synchronized String getInternalToken(String id) {
        try {
            return new String((char[])securityConnection.sync("getInternalTokenById", id));
        } catch (QException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected synchronized void protractExpiry(String id) {
        try {
            securityConnection.async("protractExpiry", id);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
