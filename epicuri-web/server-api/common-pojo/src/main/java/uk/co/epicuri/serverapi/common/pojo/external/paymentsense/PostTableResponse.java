package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

/**
 * Created by manish on 29/07/2017.
 */
public class PostTableResponse {
    private String location;
    private String tableName;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
