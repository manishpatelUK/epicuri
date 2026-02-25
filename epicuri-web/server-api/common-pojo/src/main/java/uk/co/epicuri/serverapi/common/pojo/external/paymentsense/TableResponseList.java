package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 30/07/2017.
 */
public class TableResponseList {
    private List<TableResponse> tables = new ArrayList<>();

    public List<TableResponse> getTables() {
        return tables;
    }

    public void setTables(List<TableResponse> tables) {
        this.tables = tables;
    }
}
