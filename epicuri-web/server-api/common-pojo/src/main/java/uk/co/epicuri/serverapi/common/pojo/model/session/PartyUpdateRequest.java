package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyUpdateRequest {
    @JsonProperty("PartyUpdate")
    private Update update;

    @JsonProperty("SessionId")
    private String sessionId;

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public static class Update {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("NumberOfPeople")
        private int numberOfPeople;

        @JsonProperty("CreateSession")
        private boolean createSession;

        @JsonProperty("Tables")
        private List<String> tables = new ArrayList<>();

        @JsonProperty("ServiceId")
        private String serviceId;

        @JsonProperty("LeadCustomer")
        private CustomerIdView leadCustomer;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumberOfPeople() {
            return numberOfPeople;
        }

        public void setNumberOfPeople(int numberOfPeople) {
            this.numberOfPeople = numberOfPeople;
        }

        public boolean isCreateSession() {
            return createSession;
        }

        public void setCreateSession(boolean createSession) {
            this.createSession = createSession;
        }

        public List<String> getTables() {
            return tables;
        }

        public void setTables(List<String> tables) {
            this.tables = tables;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public CustomerIdView getLeadCustomer() {
            return leadCustomer;
        }

        public void setLeadCustomer(CustomerIdView leadCustomer) {
            this.leadCustomer = leadCustomer;
        }
    }

    public static class CustomerIdView {
        @JsonProperty("Id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
