package uk.co.epicuri.serverapi.common.pojo.model.authentication;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.Date;

@Document(collection = TableNames.STAFF_AUTHENTICATIONS)
public class StaffAuthentications extends IDAble{
    @Indexed(expireAfterSeconds = AUTHORIZATION_TIME_SECONDS)
    private Date createdTime;

    @Indexed
    private String staffId;

    private String authenticationKey;
    private String restaurantId;

    @Transient
    public transient static final long AUTHORIZATION_TIME = 7884000000L; //3 months

    @Transient
    public transient static final int AUTHORIZATION_TIME_SECONDS = (int)AUTHORIZATION_TIME/1000; //3 months

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
