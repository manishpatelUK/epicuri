package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.mongodb.core.index.Indexed;

public class AbsoluteBlackout {

    @Indexed
    private long start;
    private long end;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
