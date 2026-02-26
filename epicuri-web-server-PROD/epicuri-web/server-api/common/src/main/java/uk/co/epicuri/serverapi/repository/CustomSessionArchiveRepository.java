package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionArchive;

import java.util.List;

/**
 * Created by manish on 13/01/2018.
 */
@Repository
public interface CustomSessionArchiveRepository {
    List<SessionArchive> findByRestaurantIdAndClosedTimeBetween(String restaurantId, long start, long end);
}
