package uk.co.epicuri.api.core.pojo.session;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


public class NewPartyRequest {
    private String Name;
    private int NumberOfPeople;
    private int ServiceId;
    private boolean CreateSession;
    private List<Integer> Tables = new ArrayList<>();

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getNumberOfPeople() {
        return NumberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        NumberOfPeople = numberOfPeople;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public boolean isCreateSession() {
        return CreateSession;
    }

    public void setCreateSession(boolean createSession) {
        CreateSession = createSession;
    }

    public List<Integer> getTables() {
        return Tables;
    }

    public void setTables(List<Integer> tables) {
        Tables = tables;
    }

    @Override
    public String toString() {
        return "NewPartyRequest{" +
                "Name='" + Name + '\'' +
                ", NumberOfPeople=" + NumberOfPeople +
                ", ServiceId=" + ServiceId +
                ", CreateSession=" + CreateSession +
                ", Tables=" + ArrayUtils.toString(Tables.toArray()) +
                '}';
    }
}
