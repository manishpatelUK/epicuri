package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostDinerRequest {

    @JsonProperty("EpicuriUser")
    private IdPojo epicuriUser;
    private String guestName;

    public IdPojo getEpicuriUser() {
        return epicuriUser;
    }

    public void setEpicuriUser(IdPojo epicuriUser) {
        this.epicuriUser = epicuriUser;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
}
