package uk.co.epicuri.serverapi.common.pojo.customer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.CUSTOMER_INTERACTIONS)
public class CustomerInteraction extends IDAble {
    private long creationTime = System.currentTimeMillis();
    private Long archiveTime = null;

    @Indexed
    private String customerId;

    @Indexed
    private String restaurantId;

    public CustomerInteraction(){}
    public CustomerInteraction(String customerId, String restaurantId) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getArchiveTime() {
        return archiveTime;
    }

    public void setArchiveTime(Long archiveTime) {
        this.archiveTime = archiveTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CustomerInteraction that = (CustomerInteraction) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(creationTime, that.creationTime)
                .append(archiveTime, that.archiveTime)
                .append(customerId, that.customerId)
                .append(restaurantId, that.restaurantId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(creationTime)
                .append(archiveTime)
                .append(customerId)
                .append(restaurantId)
                .toHashCode();
    }
}
