package uk.co.epicuri.serverapi.common.pojo.model.restaurant.external;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReports;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.List;

@Document(collection = TableNames.PAYMENTSENSE_REPORTS)
public class PaymentSenseReport {
    @Indexed
    private String restaurantId;

    @Indexed
    private long time = System.currentTimeMillis();

    private List<PACReport> PACReports;

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<PACReport> getPACReports() {
        return PACReports;
    }

    public void setPACReports(List<PACReport> PACReports) {
        this.PACReports = PACReports;
    }
}
