package uk.co.epicuri.serverapi.common.pojo.host;

import org.springframework.data.annotation.Transient;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.AbsoluteBlackout;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HostClosuresView {
    @Transient
    private static final transient ZoneId UTC = ZoneId.of("UTC");

    @Transient
    private static final transient DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmVV");

    @Transient
    private static final transient DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<ClosureDatePair> closures = new ArrayList<>();

    public HostClosuresView(){}

    public HostClosuresView(String timeZone, List<AbsoluteBlackout> absoluteBlackouts) {
        absoluteBlackouts.forEach(a -> closures.add(getDatePair(timeZone, a)));
        Collections.sort(closures);
    }

    private ClosureDatePair getDatePair(String timeZone, AbsoluteBlackout absoluteBlackout) {
        ClosureDatePair closureDatePair = new ClosureDatePair();
        closureDatePair.setStartLong(absoluteBlackout.getStart());
        closureDatePair.setStart(toDateString(timeZone, absoluteBlackout.getStart()));
        closureDatePair.setEnd(toDateString(timeZone, absoluteBlackout.getEnd()));
        return closureDatePair;
    }

    private String toDateString(String timeZone, long t) {
        Instant instant = Instant.ofEpochMilli(t);
        ZonedDateTime utcDT = ZonedDateTime.ofInstant(instant, UTC);
        ZonedDateTime restaurantTz = utcDT.withZoneSameInstant(ZoneId.of(timeZone));
        return formatter2.format(restaurantTz);
    }

    public static long fromDateString(String timeZone, String time) {
        ZonedDateTime restaurantTz = ZonedDateTime.parse(time+ timeZone, formatter1).withZoneSameLocal(ZoneId.of(timeZone));
        ZonedDateTime utcDT = restaurantTz.withZoneSameInstant(UTC);
        return utcDT.toEpochSecond() * 1000;
    }

    public List<ClosureDatePair> getClosures() {
        return closures;
    }

    public void setClosures(List<ClosureDatePair> closures) {
        this.closures = closures;
    }
}
