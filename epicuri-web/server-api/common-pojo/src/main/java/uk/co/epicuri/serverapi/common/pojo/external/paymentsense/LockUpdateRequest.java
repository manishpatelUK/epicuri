package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

public class LockUpdateRequest extends SequentialTableRequest {
    private boolean locked;
    private boolean override;
    private String lockedBy;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
}
