package uk.co.epicuri.serverapi.common.pojo.model.authentication;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.Date;

@Document(collection = TableNames.BOOKING_WIDGET_AUTHENTICATIONS)
public class BookingWidgetAuthentications extends IDAble{
    @Indexed(expireAfterSeconds = AUTHORIZATION_TIME_SECONDS)
    private Date createdTime;

    @Transient
    public transient static final long AUTHORIZATION_TIME = 604800000L; //1 week

    @Transient
    public transient static final int AUTHORIZATION_TIME_SECONDS = (int)AUTHORIZATION_TIME/1000; //1 week

    private String token;

    @Indexed(unique = true)
    private String restaurantId;

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
