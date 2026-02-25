package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish.
 */
@Repository
public class BookingRepositoryImpl implements CustomBookingRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public void pushAccept(String id, boolean accept) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("accepted", accept), Booking.class);
    }

    @Override
    public void pushReject(String id, boolean reject, String reason) {
        Update update = new Update();
        update.set("rejected", reject);
        update.set("rejectionNotice", reason);
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                update, Booking.class);
    }

    @Override
    public void pushCancel(String id, boolean cancel) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("cancelled", cancel), Booking.class);
    }

    @Override
    public void pushCustomerId(String id, String customerId) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("customerId", customerId), Booking.class);
    }

    @Override
    public List<Booking> find(String restaurantId, List<String> customerIds, long lower, long upper, boolean includeRejected) {
        Query query = Query.query(
                Criteria.where("restaurantId").is(restaurantId)
                        .and("customerId").in(customerIds)
                        .andOperator(
                                Criteria.where("targetTime").gte(lower),
                                Criteria.where("targetTime").lte(upper)));
        List<Booking> list = operations.find(query, Booking.class);
        if(!includeRejected) {
            return list.stream().filter(b -> !b.isRejected()).collect(Collectors.toList());
        }

        return list;
    }

    @Override
    public List<Booking> find(String restaurantId, long lower, long upper, BookingType bookingType, boolean includeRejected) {
        Query query = Query.query(
                Criteria.where("restaurantId").is(restaurantId)
                        .and("bookingType").is(bookingType)
                        .andOperator(
                                Criteria.where("targetTime").gte(lower),
                                Criteria.where("targetTime").lte(upper)));
        List<Booking> list = operations.find(query, Booking.class);
        if(!includeRejected) {
            return list.stream().filter(b -> !b.isRejected()).collect(Collectors.toList());
        }

        return list;
    }

    @Override
    public List<Booking> find(String restaurantId, long lower, long upper, boolean includeRejected) {
        Query query = Query.query(
                Criteria.where("restaurantId").is(restaurantId)
                        .andOperator(
                                Criteria.where("targetTime").gte(lower),
                                Criteria.where("targetTime").lte(upper)));
        List<Booking> list = operations.find(query, Booking.class);
        if(!includeRejected) {
            return list.stream().filter(b -> !b.isRejected()).collect(Collectors.toList());
        }

        return list;
    }

    @Override
    public List<Booking> find(String customerId, long lower, long upper, BookingType bookingType) {
        Criteria criteria = Criteria.where("customerId").is(customerId)
                .andOperator(
                        Criteria.where("targetTime").gte(lower),
                        Criteria.where("targetTime").lte(upper));
        if(bookingType != null) {
            criteria = criteria.and("bookingType").is(bookingType);
        }

        Query query = Query.query(criteria);
        return operations.find(query, Booking.class);
    }

    @Override
    public Booking findOne(String id, boolean includeDeleted, boolean includeCancelled) {
        Query query = createQuery(id, includeDeleted, includeCancelled);
        return operations.findOne(query, Booking.class);
    }

    private Query createQuery(String id, boolean includeDeleted, boolean includeCancelled) {
        Criteria criteria = Criteria.where("_id").is(id);

        if(!includeDeleted) {
            criteria = criteria.and("deleted").exists(false);
        }
        if(!includeCancelled) {
            criteria = criteria.and("cancelled").is(false);
        }

        return Query.query(criteria);
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
