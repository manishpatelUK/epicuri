package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish.
 */
public class TerminalList {
    @JsonProperty("terminals")
    private List<Terminal> terminalList = new ArrayList<>();

    public List<Terminal> getTerminalList() {
        return terminalList;
    }

    public void setTerminalList(List<Terminal> terminalList) {
        this.terminalList = terminalList;
    }
}
