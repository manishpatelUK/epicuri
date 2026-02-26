package uk.co.epicuri.serverapi.repository;

/**
 * Created by manish
 */
public interface CustomCheckInRepository extends DeletableRepository {
    void updatePartyId(String id, String partyId);
    void updateSessionId(String id, String sessionId);
    void updateSessionIdAndPartyId(String id, String sessionId, String partyId);
    void updateCustomerId(String id, String customerId);
}
