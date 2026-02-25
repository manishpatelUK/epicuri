package uk.co.epicuri.bookingapi;

import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QSynchronizedConnection;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


public class KxAurusitDBFactory {
    @NotEmpty
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 1;

    @JsonProperty("kxaurusithost")
    public String getHost() {
        return host;
    }

    @JsonProperty("kxaurusithost")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("kxaurusitport")
    public int getPort() {
        return port;
    }

    @JsonProperty("kxaurusitport")
    public void setPort(int port) {
        this.port = port;
    }

    public QConnection build(Environment environment) {
        final QConnection connection = new QSynchronizedConnection(host,port,null,null);
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
