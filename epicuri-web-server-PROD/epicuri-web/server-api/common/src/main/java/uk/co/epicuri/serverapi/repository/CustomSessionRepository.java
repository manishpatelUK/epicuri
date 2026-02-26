package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.ChairData;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface CustomSessionRepository extends DeletableRepository {
    List<Session> findByStartTime(String restaurantId, long start, long end);
    List<Session> findByCloseTime(String restaurantId, long start, long end);
    List<Session> findTakeawaySessions(String restaurantId, List<String> bookingIds);
    List<Session> findCurrentLiveSessions(String restaurantId, long startTimeLimit);
    List<Session> findCurrentSeatedSessions(String restaurantId);
    void pushDiners(String sessionId, List<Diner> diners);
    void setService(String sessionId, Service service);
    void setName(String sessionId, String name);
    void pushTables(String sessionId, List<String> tables);
    void setTables(String sessionId, List<String> tables);
    void setBillRequested(String sessionId, boolean billRequested);
    void setRemoveFromReports(String sessionId, boolean remove);
    void setClosed(String sessionId, Long time);
    void setClosedBy(String sessionId, String staffId);
    void setStartTime(String sessionId, long time);
    void setVoid(String sessionId, VoidReason reason);
    void setChairData(String sessionId, List<ChairData> data);
    void setTipPercentage(String sessionId, Double tip);
    void setCalculatedDeliveryCost(String sessionId, Integer cost);
    void setSessionType(String sessionId, SessionType type);
    void updateDiner(String sessionId, Diner diner);
    void pushDiner(String sessionId, Diner diner);
    void removeDiner(String sessionId, String dinerId);
    void pushAdjustment(String sessionId, Adjustment adjustment);
    void pushAdjustments(String sessionId, List<Adjustment> adjustments);
    void removeAdjustment(String sessionId, String adjustmentId);
    void setDelay(String sessionId, long delay);
    void incrementDelay(String sessionId, long increment);
    void updateCashUpId(List<String> sessionId, String cashUpId);
    void setPaid(String sessionId, boolean paid);
}
