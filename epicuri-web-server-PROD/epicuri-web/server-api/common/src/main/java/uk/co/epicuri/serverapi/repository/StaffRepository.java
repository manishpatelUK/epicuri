package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface StaffRepository extends MongoRepository<Staff, String>, CustomStaffRepository{
    List<Staff> findByUserNameAndRestaurantId(String userName, String restaurantId);
    List<Staff> findByRestaurantId(String restaurantId);
}
