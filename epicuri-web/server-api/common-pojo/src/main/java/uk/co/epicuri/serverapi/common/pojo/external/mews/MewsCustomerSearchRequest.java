package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MewsCustomerSearchRequest extends MewsAPIRequest{
    @JsonProperty("Name")
    private String name;

    @JsonProperty("RoomNumber")
    private String roomNumber;

    @JsonProperty("Client")
    private String client = "Epicuri";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
