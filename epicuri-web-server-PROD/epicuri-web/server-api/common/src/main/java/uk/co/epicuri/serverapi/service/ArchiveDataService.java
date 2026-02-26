package uk.co.epicuri.serverapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReports;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@Service
public class ArchiveDataService {
    @Autowired
    private CashUpRepository cashUpRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ModifierGroupRepository modifierGroupRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OpeningHoursRepository openingHoursRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PrinterRepository printerRepository;

    @Autowired
    private RestaurantImageRepository restaurantImageRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffAuthenticationsRepository staffAuthenticationsRepository;

    @Autowired
    private SessionArchiveRepository sessionArchiveRepository;

    @Autowired
    private PaymentSenseReportRepository paymentSenseReportRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SessionNumberRepository sessionNumberRepository;

    public CashUp getLastCashUp(String restaurantId) {
        PageRequest request = new PageRequest(0,1,new Sort(Sort.Direction.DESC, "endTime"));
        Page<CashUp> allCashUps = cashUpRepository.findLastCashUp(restaurantId, request);
        if(allCashUps.getContent().size() > 0) {
            return allCashUps.getContent().get(0);
        } else {
            return null;
        }
    }

    public List<CashUp> getLastCashUps(String restaurantId, long endTimeMinimum) {
        return cashUpRepository.findByRestaurantIdAndEndTimeGreaterThanEqual(restaurantId,endTimeMinimum);
    }

    public List<CashUp> getCashUpsBetween(String restaurantId, long start, long end) {
        return cashUpRepository.findByRestaurantIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(restaurantId, start, end);
    }

    public CashUp addCashUp(CashUp cashUp) {
        return cashUpRepository.insert(cashUp);
    }

    public CashUp getCashUp(String id) {
        return cashUpRepository.findOne(id);
    }

    public List<CashUp> addCashUps(List<CashUp> cashUps) {
        return cashUpRepository.insert(cashUps);
    }

    //for now we're doing deletion, but really we want to archive it
    public void deleteRestaurant(String restaurantId) {
        restaurantRepository.delete(restaurantId);
        menuItemRepository.delete(menuItemRepository.findByRestaurantId(restaurantId));
        menuRepository.delete(menuRepository.findByRestaurantId(restaurantId));
        modifierGroupRepository.delete(modifierGroupRepository.findByRestaurantId(restaurantId));
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurantId));
        printerRepository.delete(printerRepository.findByRestaurantId(restaurantId));
        restaurantImageRepository.delete(restaurantImageRepository.findByRestaurantId(restaurantId));
        staffRepository.delete(staffRepository.findByRestaurantId(restaurantId));
        staffAuthenticationsRepository.delete(staffAuthenticationsRepository.findByRestaurantId(restaurantId));

        clearOrders(restaurantId);
    }

    public void clearOrders(String restaurantId) {
        List<Session> sessions = sessionRepository.findByRestaurantId(restaurantId);
        sessionRepository.delete(sessions);
        List<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        batchRepository.delete(batchRepository.findBySessionIdIn(sessionIds));
        bookingRepository.delete(bookingRepository.findByRestaurantId(restaurantId));
        cashUpRepository.delete(cashUpRepository.findByRestaurantId(restaurantId));
        checkInRepository.delete(checkInRepository.findByRestaurantId(restaurantId));
        notificationRepository.delete(notificationRepository.findByRestaurantId(restaurantId));
        orderRepository.delete(orderRepository.findBySessionIdIn(sessionIds));
        partyRepository.delete(partyRepository.findByRestaurantId(restaurantId));
        sessionArchiveRepository.delete(sessionArchiveRepository.findByRestaurantId(restaurantId));
        SessionNumber sessionNumber = sessionNumberRepository.findByRestaurantId(restaurantId);
        if(sessionNumber != null) {
            sessionNumberRepository.delete(sessionNumber);
        }
        List<PaymentSenseReport> psList = paymentSenseReportRepository.findByRestaurantId(restaurantId);
        if(psList != null && psList.size() > 0) { //not sure why this could be null
            paymentSenseReportRepository.delete(psList);
        }
    }

    public void archiveSessionOrders(Session session, List<Order> orders) {
        SessionArchive sessionArchive = createOrFindSessionArchive(session);
        if(sessionArchive.getOrders() == null) {
            sessionArchive.setOrders(new ArrayList<>());
        }
        filterList(sessionArchive.getOrders(), orders);
        sessionArchive.getOrders().addAll(orders);
        updateSession(session, sessionArchive);
    }

    public void archiveSessionBatches(Session session, List<Batch> batches) {
        SessionArchive sessionArchive = createOrFindSessionArchive(session);
        if(sessionArchive.getBatches() == null) {
            sessionArchive.setBatches(new ArrayList<>());
        }
        filterList(sessionArchive.getBatches(), batches);
        sessionArchive.getBatches().addAll(batches);
        updateSession(session, sessionArchive);
    }

    public void archiveSessionNotifications(Session session, List<Notification> notifications) {
        SessionArchive sessionArchive = createOrFindSessionArchive(session);
        if(sessionArchive.getNotifications() == null) {
            sessionArchive.setNotifications(new ArrayList<>());
        }
        filterList(sessionArchive.getNotifications(), notifications);
        sessionArchive.getNotifications().addAll(notifications);
        updateSession(session, sessionArchive);
    }

    public void archiveCheckIns(Session session, List<CheckIn> checkIns) {
        SessionArchive sessionArchive = createOrFindSessionArchive(session);
        archiveCheckIns(sessionArchive, checkIns);
    }

    public void archiveCheckIns(SessionArchive sessionArchive, List<CheckIn> checkIns) {
        Session session = sessionArchive.getSession();
        if(sessionArchive.getCheckIns() == null) {
            sessionArchive.setCheckIns(new ArrayList<>());
        }
        filterList(sessionArchive.getCheckIns(), checkIns);
        sessionArchive.getCheckIns().addAll(checkIns);
        updateSession(session, sessionArchive);
    }

    public SessionArchive getSessionArchive(String sessionId) {
        return sessionArchiveRepository.findBySessionId(sessionId);
    }

    public List<SessionArchive> getSessionArchives(Iterable<String> sessionIds) {
        return sessionArchiveRepository.findBySessionIdIn(sessionIds);
    }

    public void archiveParty(Session session, Party party) {
        SessionArchive sessionArchive = createOrFindSessionArchive(session);
        sessionArchive.setParty(party);
        updateSession(session, sessionArchive);
    }

    public void unarchiveParty(String sessionId) {
        SessionArchive sessionArchive = sessionArchiveRepository.findBySessionId(sessionId);
        if(sessionArchive == null || sessionArchive.getParty() == null) {
            return;
        }
        partyRepository.save(sessionArchive.getParty());
    }

    public List<SessionArchive> getSessionArchivesByClosedTime(String restaurantId, long start, long end) {
        return sessionArchiveRepository.findByRestaurantIdAndClosedTimeBetween(restaurantId, start, end);
    }

    public void updateSession(Session session) {
        SessionArchive archive = createOrFindSessionArchive(session);
        archive.setSession(session);
        sessionArchiveRepository.save(archive);
    }

    public void pushReport(List<PACReport> report, String restaurantId) {
        PaymentSenseReport paymentSenseReport = new PaymentSenseReport();
        paymentSenseReport.setRestaurantId(restaurantId);
        paymentSenseReport.setPACReports(report);
        paymentSenseReportRepository.insert(paymentSenseReport);
    }

    private void updateSession(Session session, SessionArchive sessionArchive) {
        sessionArchive.setSession(session);
        sessionArchive.setService(session.getService());
        sessionArchiveRepository.save(sessionArchive);
    }

    private void filterList(List<? extends Deletable> existingList, List<? extends Deletable> newList) {
        List<String> ids = existingList.stream().map(Deletable::getId).collect(Collectors.toList());
        newList.removeIf(d -> ids.contains(d.getId()));
    }

    private SessionArchive createOrFindSessionArchive(Session session) {
        SessionArchive sessionArchive = sessionArchiveRepository.findBySessionId(session.getId());
        if(sessionArchive == null) {
            sessionArchive = sessionArchiveRepository.insert(new SessionArchive(session));
        }

        return sessionArchive;
    }
}
