package uk.co.epicuri.serverapi.common.pojo.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.host.DeviceRequest;

/**
 * Created by manish
 */
@Document(collection = TableNames.DEVICE_DETAIL)
public class DeviceDetail extends Deletable {
    @Indexed
    private String restaurantId;

    private String hash;
    private String note;
    private String os;
    private String waiterAppVersionId;
    private String waiterAppVersion;
    private String languageSetting;
    private String timezoneSetting;
    private boolean autoUpdating;

    public DeviceDetail(){}
    public DeviceDetail(DeviceRequest request) {
        this.restaurantId = request.getRestaurantId();
        this.note = request.getNote();
        this.os = request.getOs();
        this.waiterAppVersionId = request.getWaiterAppVersionId();
        this.waiterAppVersion = request.getWaiterAppVersion();
        this.languageSetting = request.getLanguageSetting();
        this.timezoneSetting = request.getTimezoneSetting();
        this.autoUpdating = request.isAutoUpdating();
        this.hash = request.getHash();
    }


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
