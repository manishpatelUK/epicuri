package uk.co.epicuri.serverapi.common.pojo.host;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish.
 */
public class PrinterTicketsResponse implements Comparable<PrinterTicketsResponse> {
    private String sessionId;
    private String batchId;
    private String timePrepend = "Opened";
    private String sessionTime;
    private boolean done;
    private String title;
    private String tableName;
    private String partyName;
    private Integer covers;
    private List<PrinterTicketsCourseView> courses = new ArrayList<>();
    private SessionType sessionType;
    private TakeawayType takeawayType;

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
    private static final ZoneId UTC = ZoneId.of("UTC");

    public PrinterTicketsResponse(){}
    public PrinterTicketsResponse(Restaurant restaurant, Session session) {
        this.sessionId = session.getId();

        if(session.getSessionType() == SessionType.TAKEAWAY) {
            Booking booking = session.getOriginalBooking();
            if(booking == null) {
                amendSessionTime(session.getStartTime(), restaurant.getIANATimezone());
            } else {
                amendSessionTime(booking.getTargetTime(), restaurant.getIANATimezone());
            }
        } else {
            amendSessionTime(session.getStartTime(), restaurant.getIANATimezone());
        }
        if(session.getSessionType() == SessionType.SEATED) {
            List<String> tables = restaurant.getTables().stream().filter(t -> session.getTables().contains(t.getId())).map(Table::getName).collect(Collectors.toList());
            this.tableName = StringUtils.join(tables, ", ").trim();
            this.title = "Dine in - " + tableName;
            this.covers = session.getDiners().size()-1;
            if(session.getClosedTime() != null) {
                timePrepend = "Closed";
            }
        } else if(session.getSessionType() == SessionType.TAKEAWAY) {
            this.title = "Takeaway (" + session.getTakeawayType() + ") - " + session.getName();
            this.timePrepend = "Due at";
        } else if(session.getSessionType() == SessionType.TAB) {
            this.title = "Tab";
            if(session.getDiners().size() > 1) {
                this.covers = session.getDiners().size()-1;
            }
            this.partyName = session.getName();
        } else if(session.getSessionType() == SessionType.ADHOC) {
            this.title = "Quick Order";
        }
    }

    public void amendSessionTime(long time, String timeZone) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime utcStart = ZonedDateTime.ofInstant(instant, UTC);
        ZoneId zoneId = ZoneId.of(timeZone);
        sessionTime = DATE_FORMATTER.format(utcStart.withZoneSameInstant(zoneId));
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(String sessionTime) {
        this.sessionTime = sessionTime;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public Integer getCovers() {
        return covers;
    }

    public void setCovers(Integer covers) {
        this.covers = covers;
    }

    public List<PrinterTicketsCourseView> getCourses() {
        return courses;
    }

    public void setCourses(List<PrinterTicketsCourseView> courses) {
        this.courses = courses;
    }

    @Override
    public int compareTo(PrinterTicketsResponse o) {
        //sort by ticket done first -- "more than"
        if(this.done && !o.isDone()) {
            return 1;
        } else if (!this.done && o.isDone()) {
            return -1;
        }

        return compareToTimes(o);
    }

    private int compareToTimes(PrinterTicketsResponse o) {
        long thisMaxTime = this.courses.stream().flatMapToLong(p -> p.getItems().stream().filter(i -> i.getDoneTime() == null).mapToLong(PrinterTicketView::getCreationTime)).max().orElse(Long.MIN_VALUE);
        long oMaxTime = o.getCourses().stream().flatMapToLong(p -> p.getItems().stream().filter(i -> i.getDoneTime() == null).mapToLong(PrinterTicketView::getCreationTime)).max().orElse(Long.MIN_VALUE);
        return Long.compare(oMaxTime,thisMaxTime);
    }

    public TakeawayType getTakeawayType() {
        return takeawayType;
    }

    public void setTakeawayType(TakeawayType takeawayType) {
        this.takeawayType = takeawayType;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getTimePrepend() {
        return timePrepend;
    }

    public void setTimePrepend(String timePrepend) {
        this.timePrepend = timePrepend;
    }
}
