package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface OrderRepository extends MongoRepository<Order,String>, CustomOrderRepository {
    List<Order> findBySessionId(String sessionId);
    List<Order> findBySessionIdIn(List<String> sessionIds);
}
