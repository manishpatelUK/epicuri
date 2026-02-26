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
}
