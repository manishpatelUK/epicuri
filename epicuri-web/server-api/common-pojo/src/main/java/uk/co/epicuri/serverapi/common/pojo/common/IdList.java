package uk.co.epicuri.serverapi.common.pojo.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdList {
    private List<String> ids = new ArrayList<>();

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
