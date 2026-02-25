package uk.co.epicuri.serverapi.common.pojo.model.session;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintBatchRequest {
    @JsonProperty("batchId")
    private List<String> batchId = new ArrayList<>(); //naming inconsistency

    public List<String> getBatchId() {
        return batchId;
    }

    public void setBatchId(List<String> batchId) {
        this.batchId = batchId;
    }
}
