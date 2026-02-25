package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.StaffAuthentications;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface StaffAuthenticationsRepository extends MongoRepository<StaffAuthentications, String> {
    StaffAuthentications findByStaffId(String staffId);
    List<StaffAuthentications> findByRestaurantId(String restaurantId);
}
