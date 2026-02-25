package uk.co.epicuri.serverapi.common.pojo.model.session;

/**
 * Created by manish
 */
public class VoidReason {
    private String description;
    private long time;
    private String staffId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
}
