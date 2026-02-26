package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public class StaffRepositoryImpl implements CustomStaffRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public StaffRole getStaffRole(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        query.fields().include("role");
        Staff staff = operations.findOne(query, Staff.class);
        if(staff != null) {
            return staff.getRole();
        }

        return StaffRole.UNKNOWN;
    }

    @Override
    public <T extends Deletable> void markDeleted(String id, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations,id,clazz);
    }

    @Override
    public <T extends Deletable> void markDeleted(List<String> ids, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations,ids,clazz);
    }

    @Override
    public <T extends Deletable> T findOneNotDeleted(String id, Class<T> clazz) {
        return DeletableRepositoryImpl.findOneNotDeleted(operations,id,clazz);
    }
}
