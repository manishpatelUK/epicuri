package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface PrinterRepository extends MongoRepository<Printer,String> {
    List<Printer> findByRestaurantId(String restaurantId);
}
