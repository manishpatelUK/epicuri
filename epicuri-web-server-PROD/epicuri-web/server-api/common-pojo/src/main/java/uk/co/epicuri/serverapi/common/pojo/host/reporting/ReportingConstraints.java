package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class ReportingConstraints {
    private long start;
    private long end;
    private String restaurantId;
    private String currency;
    private ZoneId zoneId;
    private Set<ExternalIntegration> integrations = new HashSet<>();
    private boolean aggregateByPLU;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Set<ExternalIntegration> getIntegrations() {
        return integrations;
    }

    public void setIntegrations(Set<ExternalIntegration> integrations) {
        this.integrations = integrations;
    }

    public boolean isAggregateByPLU() {
        return aggregateByPLU;
    }

    public void setAggregateByPLU(boolean aggregateByPLU) {
        this.aggregateByPLU = aggregateByPLU;
    }
}
