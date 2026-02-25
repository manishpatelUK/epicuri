package uk.co.epicuri.api.core.pojo.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class Session {

    private List<Diners> Diners = new ArrayList<>();
    private List<Table> Tables = new ArrayList<>();
    private int Id;
    private long StartTime;
    private long ClosedTime;
    private String PartyName;
    private String SessionType;

    public String getSessionType() {
        return SessionType;
    }

    public void setSessionType(String sessionType) {
        SessionType = sessionType;
    }

    public String getPartyName() {
        return PartyName;
    }

    public void setPartyName(String partyName) {
        PartyName = partyName;
    }

    public List<Diners> getDiners() {
        return Diners;
    }

    public void setDiners(List<Diners> diners) {
        this.Diners = diners;
    }

    public List<Table> getTables() {
        return Tables;
    }

    public void setTables(List<Table> tables) {
        this.Tables = tables;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public long getStartTime() {
        return StartTime;
    }

    public void setStartTime(long startTime) {
        this.StartTime = startTime;
    }

    public long getClosedTime() {
        return ClosedTime;
    }

    public void setClosedTime(long closedTime) {
        this.ClosedTime = closedTime;
    }

    public class Diners {
        private int Id;
        private int SessionId;
        private List<Integer> Orders = new ArrayList<>();

        public int getId() {
            return Id;
        }

        public void setId(int id) {
            this.Id = id;
        }

        public int getSessionId() {
            return SessionId;
        }

        public void setSessionId(int sessionId) {
            this.SessionId = sessionId;
        }

        public List<Integer> getOrders() {
            return Orders;
        }

        public void setOrders(List<Integer> orders) {
            this.Orders = orders;
        }
    }
}
