package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;

import java.util.List;

@Repository
public interface CustomCustomerInteractionRepository {
    void setPaid(String sessionId, String settlementSessionId, boolean paid);
    List<CustomerInteractionDeferredSession> findByRestaurantIdAndPaid(String restaurantId, boolean paid);
    CustomerInteractionDeferredSession findByRestaurantIdAndSessionId(String restaurantId, String sessionId);
}
