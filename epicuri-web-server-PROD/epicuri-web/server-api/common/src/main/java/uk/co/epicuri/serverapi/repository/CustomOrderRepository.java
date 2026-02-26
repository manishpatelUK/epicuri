package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface CustomOrderRepository {
    void updateVoid(List<String> ids);
    void pushSelfServiceParameters(List<String> ids, String publicFacingId, String location);
}
