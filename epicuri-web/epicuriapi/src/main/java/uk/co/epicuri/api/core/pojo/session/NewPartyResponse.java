package uk.co.epicuri.api.core.pojo.session;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class NewPartyResponse {
    private int SessionId;
    private List<Integer> Tables = new ArrayList<>();
    private boolean CreateSession;
    private int ServiceId;
    private int Id;
    private int NumberOfPeople;
    private String Name;
    private long Created;

    public int getSessionId() {
        return SessionId;
    }

    public void setSessionId(int sessionId) {
        SessionId = sessionId;
    }

    public List<Integer> getTables() {
        return Tables;
    }

    public void setTables(List<Integer> tables) {
        Tables = tables;
    }

    public boolean isCreateSession() {
        return CreateSession;
    }

    public void setCreateSession(boolean createSession) {
        CreateSession = createSession;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getNumberOfPeople() {
        return NumberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        NumberOfPeople = numberOfPeople;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public long getCreated() {
        return Created;
    }

    public void setCreated(long created) {
        Created = created;
    }

    @Override
    public String toString() {
        return "NewPartyResponse{" +
                "SessionId=" + SessionId +
                ", Tables=" + ArrayUtils.toString(Tables.toArray()) +
                ", CreateSession=" + CreateSession +
                ", ServiceId=" + ServiceId +
                ", Id=" + Id +
                ", NumberOfPeople=" + NumberOfPeople +
                ", Name='" + Name + '\'' +
                ", Created=" + Created +
                '}';
    }
}
