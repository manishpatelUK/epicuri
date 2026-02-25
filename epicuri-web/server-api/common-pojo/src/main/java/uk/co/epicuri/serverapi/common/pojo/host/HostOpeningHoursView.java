package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HostOpeningHoursView {
    @JsonProperty("hours")
    private Map<DayOfWeek, List<HourSpan>> hours = new LinkedHashMap<>();

    public HostOpeningHoursView(){}
    public HostOpeningHoursView(OpeningHours openingHours) {
        putInMap(openingHours, DayOfWeek.MONDAY);
        putInMap(openingHours, DayOfWeek.TUESDAY);
        putInMap(openingHours, DayOfWeek.WEDNESDAY);
        putInMap(openingHours, DayOfWeek.THURSDAY);
        putInMap(openingHours, DayOfWeek.FRIDAY);
        putInMap(openingHours, DayOfWeek.SATURDAY);
        putInMap(openingHours, DayOfWeek.SUNDAY);
    }

    private void putInMap(OpeningHours openingHours, DayOfWeek dayOfWeek) {
        hours.put(dayOfWeek, createHours(openingHours, dayOfWeek));
    }

    private List<HourSpan> createHours(OpeningHours openingHours, DayOfWeek dayOfWeek) {
        List<HourSpan> hours = openingHours.getHours().getOrDefault(dayOfWeek, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        return hours.size() == 0 ? OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS : hours;
    }

    public Map<DayOfWeek, List<HourSpan>> getHours() {
        return hours;
    }

    public void setHours(Map<DayOfWeek, List<HourSpan>> hours) {
        this.hours = hours;
    }
}
