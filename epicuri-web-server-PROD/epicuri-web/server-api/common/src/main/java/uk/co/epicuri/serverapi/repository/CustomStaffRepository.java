package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;

/**
 * Created by manish
 */
@Repository
public interface CustomStaffRepository extends DeletableRepository {
    StaffRole getStaffRole(String id);
}
