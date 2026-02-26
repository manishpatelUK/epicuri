package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.STOCK_LEVELS)
public class StockLevel extends Deletable {
    @Indexed
    private String restaurantId;

    private String plu;
    private int level = 0;
    private boolean trackable = true;

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if(level < 0) {
            level = 0;
        }
        this.level = level;
    }

    public boolean isTrackable() {
        return trackable;
    }

    public void setTrackable(boolean trackable) {
        this.trackable = trackable;
    }
}
