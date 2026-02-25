package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteraction;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;

import java.util.List;

@Repository
public interface CustomerInteractionRepository extends MongoRepository<CustomerInteraction, String>, CustomCustomerInteractionRepository {
    List<CustomerInteraction> findByRestaurantId(String restaurantId);
}
