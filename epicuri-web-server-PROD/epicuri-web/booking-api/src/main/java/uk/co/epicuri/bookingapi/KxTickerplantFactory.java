package uk.co.epicuri.bookingapi;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QSynchronizedConnection;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 01/05/2015
 */
public class KxTickerplantFactory {
    @NotEmpty
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 1;

    @NotEmpty
    private String a;

    @NotEmpty
    private String b;

    @JsonProperty("reservationtphost")
    public String getHost() {
        return host;
    }

    @JsonProperty("reservationtphost")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("reservationtpport")
    public int getPort() {
        return port;
    }

    @JsonProperty("reservationtpport")
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getA() {
        return a;
    }

    @JsonProperty
    public void setA(String a) {
        this.a = a;
    }

    @JsonProperty
    public String getB() {
        return b;
    }

    @JsonProperty
    public void setB(String b) {
        this.b = b;
    }

    public QConnection build(Environment environment) {
        final QConnection connection = new QSynchronizedConnection(host,port,a,b);
        connection.setAttemptReconnect(true);
        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                connection.open();
            }

            @Override
            public void stop() throws Exception {
                connection.close();
            }
        });
        return connection;
    }
}
