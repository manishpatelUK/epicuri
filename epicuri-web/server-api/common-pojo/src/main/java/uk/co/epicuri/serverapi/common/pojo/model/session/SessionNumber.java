package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

/**
 * Created by manish on 22/05/2017.
 */
@Document(collection = TableNames.SESSION_NUMBERS)
public class SessionNumber extends IDAble {
    private int totalSessionsCreated;

    @Indexed
    private String restaurantId;

    public int getTotalSessionsCreated() {
        return totalSessionsCreated;
    }

    public void setTotalSessionsCreated(int totalSessionsCreated) {
        this.totalSessionsCreated = totalSessionsCreated;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
