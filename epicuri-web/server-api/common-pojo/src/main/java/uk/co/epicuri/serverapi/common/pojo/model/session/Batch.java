package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.*;
import java.util.stream.Collectors;

@Document(collection = TableNames.BATCHES)
public class Batch extends Deletable {
    @Indexed
    private String sessionId;

    private Set<String> orderIds = new HashSet<>();

    private SessionType sessionType;
    private List<Long> spoolTime = new ArrayList<>();
    private Long printedTime;
    private String printerId;
    private long creationTime;
    private long intendedPrintTime;
    private boolean awaitingImmediatePrint = false;
    private boolean duplicate = false;

    public Batch(){}
    public Batch(Session session, Collection<Order> orders) {
        this.sessionId = session.getId();
        this.sessionType = session.getSessionType();
        this.orderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());
        this.printerId = orders.stream().map(o -> o.getMenuItem().getDefaultPrinter()).distinct().findFirst().orElse(null);

        if(session.getSessionType() == SessionType.TAKEAWAY) {
            intendedPrintTime = session.getStartTime();
        } else {
            intendedPrintTime = System.currentTimeMillis();
        }

        creationTime = System.currentTimeMillis();
    }

    public static Batch duplicateForPrinter(Batch batch, String printerId) {
        Batch dupe = new Batch();
        dupe.setSessionId(batch.getSessionId());
        dupe.setSessionType(batch.getSessionType());
        dupe.setOrderIds(batch.getOrderIds());
        dupe.setPrinterId(printerId);
        dupe.setIntendedPrintTime(batch.getIntendedPrintTime());
        dupe.setCreationTime(batch.getCreationTime());
        dupe.setDuplicate(true);
        dupe.setAwaitingImmediatePrint(batch.isAwaitingImmediatePrint());
        return dupe;
    }

    public Set<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public List<Long> getSpoolTime() {
        return spoolTime;
    }

    public void setSpoolTime(List<Long> spoolTime) {
        this.spoolTime = spoolTime;
    }

    public Long getPrintedTime() {
        return printedTime;
    }

    public void setPrintedTime(Long printedTime) {
        this.printedTime = printedTime;
    }

    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getIntendedPrintTime() {
        return intendedPrintTime;
    }

    public void setIntendedPrintTime(long intendedPrintTime) {
        this.intendedPrintTime = intendedPrintTime;
    }

    public boolean isAwaitingImmediatePrint() {
        return awaitingImmediatePrint;
    }

    public void setAwaitingImmediatePrint(boolean awaitingImmediatePrint) {
        this.awaitingImmediatePrint = awaitingImmediatePrint;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }
}
