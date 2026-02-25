package uk.co.epicuri.serverapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;
import uk.co.epicuri.serverapi.common.pojo.model.session.NotificationType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@Service
public class SessionTimingService {

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    public long calculateDelay(Session session, long referenceTime, Notification unacknowledgedNotification) {
        if(session.getService() == null
                || session.getService().getSchedule() == null
                || session.getService().getSchedule().getScheduledItems() == null
                || session.getService().getSchedule().getScheduledItems().size() == 0
                || unacknowledgedNotification == null
                || unacknowledgedNotification.getAcknowledged() != null) {
            return 0;
        }

        List<ScheduledItem> expectedTimeline = session.getService().getSchedule().getScheduledItems();
        ScheduledItem currentStage = expectedTimeline.stream().filter(s -> s.getId().contains(unacknowledgedNotification.getScheduledItemId())).findFirst().orElse(null);
        if(currentStage == null) {
            return 0;
        }

        return calculateDelay(session, referenceTime, currentStage.getTimeAfterStart());
    }

    private long calculateDelay(Session session, long referenceTime, long idealTimeAfterStart) {
        long startTime = session.getStartTime();
        //might already be delayed due to late fulfillment of booking
        if(session.getOriginalBooking() != null && session.getOriginalBooking().getTargetTime() > startTime) {
            startTime = session.getOriginalBooking().getTargetTime();
        }

        long idealTime = startTime + idealTimeAfterStart; // the time it should be if everything was on time

        return referenceTime - idealTime; //will be negative if ahead of schedule, positive if behind schedule
    }

    public void delay(String sessionId, long delay) {
        Session session = sessionService.getSession(sessionId);
        if(session == null
                || session.getService() == null
                || session.getService().getSchedule() == null
                || session.getService().getSchedule().getScheduledItems() == null
                || session.getService().getSchedule().getScheduledItems().size() == 0) {
            return;
        }

        List<Notification> notifications = liveDataService.getUnacknowledgedNotifications(session.getRestaurantId(), sessionId);
        long newDelayTime = session.getDelay() + delay;

        adjustDownstreamNotifications(notifications, delay);
        sessionService.updateDelay(sessionId, newDelayTime);
    }

    private void adjustDownstreamNotifications(List<Notification> notifications, long delay) {
        notifications.forEach(n -> n.setTime(n.getTime() + delay));
        liveDataService.upsertNotifications(notifications);
    }

    public long postAcknowledge(String restaurantId, String sessionId, String notificationId) {
        List<Notification> notifications = liveDataService.getUnacknowledgedNotifications(restaurantId, sessionId);
        Notification notification = notifications.stream().filter(n -> n.getId().equals(notificationId)).findFirst().orElse(null);

        long ackTime = System.currentTimeMillis();
        if(notification != null) {
            notification.setAcknowledged(ackTime);
            long delay = ackTime - notification.getTime();
            adjustDownstreamNotifications(notifications, delay);
            sessionService.incrementDelay(sessionId, delay);

            //if recurring, post a new one
            if(notification.getNotificationType() == NotificationType.RECURRING) {
                notification.setId(null);
                notification.setTime(System.currentTimeMillis() + notification.getRecurrence());
                notification.setAcknowledged(null);
            }
            liveDataService.upsert(notification);
        }

        return ackTime;
    }

    public void createNotifications(Session session) {
        if(session.getService() == null
                || session.getService().getSchedule() == null) {
            return;
        }

        List<Notification> all = new ArrayList<>();

        List<ScheduledItem> items = session.getService().getSchedule().getScheduledItems();
        if(items != null && items.size() > 0) {
            List<Notification> notifications = items.stream().map(s -> new Notification(s, session)).collect(Collectors.toList());
            all.addAll(notifications);
        }

        List<ScheduledItem> recurring = session.getService().getSchedule().getRecurringItems();
        if(recurring != null && recurring.size() > 0) {
            List<Notification> notifications = recurring.stream().map(s -> new Notification(s, session)).collect(Collectors.toList());
            all.addAll(notifications);
        }

        if(all.size() > 0) {
            liveDataService.upsertNotifications(all);
        }
    }
}
