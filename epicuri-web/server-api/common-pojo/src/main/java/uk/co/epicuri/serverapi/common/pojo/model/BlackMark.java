package uk.co.epicuri.serverapi.common.pojo.model;

/**
 * Created by manish
 */
public class BlackMark {
    private String reason;
    private long time;

    public BlackMark() {}

    public BlackMark(String reason, long time) {
        this.reason = reason;
        this.time = time;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
