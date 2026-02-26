package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by manish
 */
public class PartyResponse {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("LeadCustomerId")
    private String leadCustomer;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("Tables")
    private List<String> tables;

    @JsonProperty("CreateSession")
    private boolean createSession;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("IsAdHoc")
    private boolean adHoc;

    @JsonProperty("NumberOfPeople")
    private int numberOfPeople;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Created")
    private long created;

    public PartyResponse(){}

    public PartyResponse(Party party) {
        this(party, null);
    }

    public PartyResponse(Party party, Session session) {
        this.id = party.getId();
        this.leadCustomer = party.getCustomerId();
        this.created = party.getTime() / 1000;
        this.numberOfPeople = party.getNumberOfPeople();
        this.name = party.getName();
        if(session != null) {
            this.sessionId = session.getId();
            this.tables = session.getTables();
            this.serviceId = session.getService().getId();
            this.adHoc = session.getSessionType() == SessionType.ADHOC;
            this.createSession = true;
        } else {
            this.sessionId = "0";
            this.serviceId = "0";
            this.createSession = false;
            this.adHoc = false;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeadCustomer() {
        return leadCustomer;
    }

    public void setLeadCustomer(String leadCustomer) {
        this.leadCustomer = leadCustomer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public boolean isCreateSession() {
        return createSession;
    }

    public void setCreateSession(boolean createSession) {
        this.createSession = createSession;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isAdHoc() {
        return adHoc;
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
