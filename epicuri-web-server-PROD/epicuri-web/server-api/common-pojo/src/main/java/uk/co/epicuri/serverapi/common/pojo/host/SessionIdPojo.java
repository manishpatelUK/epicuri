package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by manish
 */
public class SessionIdPojo {

    @JsonProperty("Id")
    private String id;

    public SessionIdPojo(String sessionId) {
        this.id = sessionId;
    }

    public SessionIdPojo() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
