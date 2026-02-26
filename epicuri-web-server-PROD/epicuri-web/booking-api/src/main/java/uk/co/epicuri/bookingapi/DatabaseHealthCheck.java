package uk.co.epicuri.bookingapi;

import com.codahale.metrics.health.HealthCheck;
import com.exxeleron.qjava.QConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Manish on 07/06/2015.
 */
public class DatabaseHealthCheck extends HealthCheck {

    List<QConnection> connections = Collections.synchronizedList(new ArrayList<QConnection>());

    public DatabaseHealthCheck() {

    }

    public void add(QConnection qConnection) {
        connections.add(qConnection);
    }

    @Override
    protected Result check() throws Exception {
        for(QConnection qConnection : connections) {
            try {
                qConnection.sync("");
            } catch (Exception ex) {
                return Result.unhealthy("Database connection broken? " + qConnection.getHost() + ":" + qConnection.getPort());
            }
        }

        return Result.healthy();
    }
}
