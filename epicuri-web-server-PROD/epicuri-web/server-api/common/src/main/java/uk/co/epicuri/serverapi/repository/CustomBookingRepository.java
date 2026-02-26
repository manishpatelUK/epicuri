package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import java.util.List;
import java.util.Map;

/**
 * Created by manish.
 */
@Repository
public interface CustomBookingRepository  extends DeletableRepository{
    void pushAccept(String id, boolean accept);
    void pushReject(String id, boolean reject, String reason);
    void pushCancel(String id, boolean reject);
    void pushCustomerId(String id, String customerId);
    List<Booking> find(String restaurantId, List<String> customerIds, long lower, long upper, boolean includeRejected);
    List<Booking> find(String restaurantId, long lower, long upper, BookingType bookingType, boolean includeRejected);
    List<Booking> find(String restaurantId, long lower, long upper, boolean includeRejected);
    List<Booking> find(String customerId, long lower, long upper, BookingType bookingType);
    Booking findOne(String id, boolean includeDeleted, boolean includeCancelled);
}
