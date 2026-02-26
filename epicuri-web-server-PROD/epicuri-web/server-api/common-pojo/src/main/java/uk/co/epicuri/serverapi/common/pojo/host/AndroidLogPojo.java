package uk.co.epicuri.serverapi.common.pojo.host;

import java.util.ArrayList;
import java.util.List;

public class AndroidLogPojo {
    private List<String> logs = new ArrayList<>();

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
