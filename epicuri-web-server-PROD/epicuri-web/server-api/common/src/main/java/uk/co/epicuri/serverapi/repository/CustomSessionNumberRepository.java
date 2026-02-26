package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionNumber;

/**
 * Created by manish on 23/05/2017.
 */
@Repository
public interface CustomSessionNumberRepository {
    SessionNumber incrementAndGet(String restaurantId);
}
