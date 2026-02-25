package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClosureDatePair implements Comparable<ClosureDatePair>{
    private transient long startLong;

    @JsonProperty("start")
    private String start;

    @JsonProperty("end")
    private String end;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public long getStartLong() {
        return startLong;
    }

    public void setStartLong(long startLong) {
        this.startLong = startLong;
    }

    @Override
    public int compareTo(ClosureDatePair o) {
        return Long.compare(o.startLong, startLong);
    }
}
