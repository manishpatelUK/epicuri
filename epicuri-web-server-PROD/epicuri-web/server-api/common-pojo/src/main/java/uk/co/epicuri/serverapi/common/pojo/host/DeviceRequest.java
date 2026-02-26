package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceRequest {
    @JsonProperty("Hash")
    private String hash;

    @JsonProperty("Note")
    private String note;

    @JsonProperty("OS")
    private String os;

    @JsonProperty("WaiterAppVersionId")
    private String waiterAppVersionId;

    @JsonProperty("WaiterAppVersion")
    private String waiterAppVersion;

    @JsonProperty("LanguageSetting")
    private String languageSetting;

    @JsonProperty("TimezoneSetting")
    private String timezoneSetting;

    @JsonProperty("IsAutoUpdating")
    private boolean autoUpdating;

    @JsonProperty("RestaurantId")
    private String restaurantId;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getWaiterAppVersionId() {
        return waiterAppVersionId;
    }

    public void setWaiterAppVersionId(String waiterAppVersionId) {
        this.waiterAppVersionId = waiterAppVersionId;
    }

    public String getWaiterAppVersion() {
        return waiterAppVersion;
    }

    public void setWaiterAppVersion(String waiterAppVersion) {
        this.waiterAppVersion = waiterAppVersion;
    }

    public String getLanguageSetting() {
        return languageSetting;
    }

    public void setLanguageSetting(String languageSetting) {
        this.languageSetting = languageSetting;
    }

    public String getTimezoneSetting() {
        return timezoneSetting;
    }

    public void setTimezoneSetting(String timezoneSetting) {
        this.timezoneSetting = timezoneSetting;
    }

    public boolean isAutoUpdating() {
        return autoUpdating;
    }

    public void setAutoUpdating(boolean autoUpdating) {
        this.autoUpdating = autoUpdating;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
