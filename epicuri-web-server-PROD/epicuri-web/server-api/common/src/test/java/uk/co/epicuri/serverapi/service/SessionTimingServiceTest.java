package uk.co.epicuri.serverapi.service;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;
import uk.co.epicuri.serverapi.common.pojo.model.session.NotificationType;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SessionTimingServiceTest extends SessionSetupBaseIT {

    protected void setUpSession() throws Exception{
        super.setUpSession();
        session1.setStartTime(0L);
        sessionRepository.save(session1);
    }

    @Test
    public void testCalculateDelay() throws Exception {
        setUpSession();
        assertDelayEquals(-schedule1.getScheduledItems().get(0).getTimeAfterStart(), 0, notification1); //should be 60

        setUpSession();
        session1.setService(null);
        assertDelayEquals(0, 0, notification1);

        setUpSession();
        session1.getService().setSchedule(null);
        assertDelayEquals(0, 0, notification1);

        setUpSession();
        session1.getService().getSchedule().getScheduledItems().clear();
        assertDelayEquals(0, 0, notification1);

        setUpSession();
        assertDelayEquals(60, 120, notification1);

        notification1.setAcknowledged(60L);
        assertDelayEquals(0, 120, notification1);
        assertDelayEquals(0, 120, notification2);
        assertDelayEquals(-60, 60, notification2);

    }

    private void assertDelayEquals(long expectedDelay, long currentTime, Notification notification) {
        long delay = sessionTimingService.calculateDelay(session1, currentTime, notification);
        assertEquals(expectedDelay, delay);
    }

    @Test
    public void testDelay() throws Exception {
        setUpSession();
        long delay = 10;
        assert session1.getDelay() == 0;

        sessionTimingService.delay(session1.getId(), delay);
        session1 = sessionRepository.findOne(session1.getId());

        assertEquals(delay, session1.getDelay());
        assertEquals(delay, notificationRepository.findOne(notification1.getId()).getTime() - notification1.getTime());
        assertEquals(delay, notificationRepository.findOne(notification2.getId()).getTime() - notification2.getTime());
        assertEquals(delay, notificationRepository.findOne(notification3.getId()).getTime() - notification3.getTime());

        setUpSession();
        notification1.setAcknowledged(0L);
        notificationRepository.save(notification1);
        sessionTimingService.delay(session1.getId(), delay);
        session1 = sessionRepository.findOne(session1.getId());

        assertEquals(delay, session1.getDelay());
        assertEquals(0, notificationRepository.findOne(notification1.getId()).getTime() - notification1.getTime());
        assertEquals(delay, notificationRepository.findOne(notification2.getId()).getTime() - notification2.getTime());
        assertEquals(delay, notificationRepository.findOne(notification3.getId()).getTime() - notification3.getTime());
    }

    @Test
    public void testPostAcknowledge() throws Exception {
        setUpSession();

        assert notification1.getAcknowledged() == null;

        sessionTimingService.postAcknowledge(restaurant1.getId(), session2.getId(), notification1.getId());
        assertNull(notificationRepository.findOne(notification1.getId()).getAcknowledged());

        sessionTimingService.postAcknowledge(restaurant2.getId(), session1.getId(), notification1.getId());
        assertNull(notificationRepository.findOne(notification1.getId()).getAcknowledged());

        long ack = sessionTimingService.postAcknowledge(restaurant1.getId(), session1.getId(), notification1.getId());
        assertEquals(ack, notificationRepository.findOne(notification1.getId()).getAcknowledged().longValue());

        Thread.sleep(100);

        sessionTimingService.postAcknowledge(restaurant1.getId(), session1.getId(), notification1.getId());
        assertEquals(ack, notificationRepository.findOne(notification1.getId()).getAcknowledged().longValue());

        setUpSession();
        ScheduledItem recurring = new ScheduledItem();
        recurring.setInitialDelay(0);
        recurring.setNotificationType(NotificationType.RECURRING);
        recurring.setRecurring(10);
        recurring.setText("Some recurring task");
        recurring.setTimeAfterStart(1);
        recurring.setId(restaurant1.getId() + ".r1");
        schedule1.getRecurringItems().add(recurring);
        sessionRepository.save(session1);

        Notification recurringNotification = new Notification();
        recurringNotification.setSessionId(session1.getId());
        recurringNotification.setRestaurantId(restaurant1.getId());
        recurringNotification.setScheduledItemId(recurring.getId());
        recurringNotification.setNotificationType(NotificationType.RECURRING);
        recurringNotification = notificationRepository.save(recurringNotification);

        ack = sessionTimingService.postAcknowledge(restaurant1.getId(), session1.getId(), recurringNotification.getId());
        assertEquals(ack, notificationRepository.findOne(recurringNotification.getId()).getAcknowledged().longValue());
    }

    @Test
    public void testCreateNotifications() throws Exception {
        setUpSession();

        notificationRepository.deleteAll();
        sessionTimingService.createNotifications(session1);
        List<Notification> notifications = notificationRepository.findAll();

        assertEquals(session1.getService().getSchedule().getScheduledItems().size(), notifications.size());

        for(Notification notification : notifications) {
            assertEquals(session1.getId(), notification.getSessionId());
            assertTrue(session1.getService().getSchedule().getScheduledItems().stream().anyMatch(s -> s.getId().equals(notification.getScheduledItemId())));
        }

        setUpSession();
        ScheduledItem recurring = new ScheduledItem();
        recurring.setInitialDelay(0);
        recurring.setNotificationType(NotificationType.RECURRING);
        recurring.setRecurring(10);
        recurring.setText("Some recurring task");
        recurring.setTimeAfterStart(1);
        recurring.setId(restaurant1.getId() + ".r1");
        schedule1.getRecurringItems().add(recurring);
        sessionRepository.save(session1);
        notificationRepository.deleteAll();
        sessionTimingService.createNotifications(session1);

        notifications = notificationRepository.findAll();

        for(Notification notification : notifications) {
            assertEquals(session1.getId(), notification.getSessionId());
            assertTrue(session1.getService().getSchedule().getScheduledItems().stream().anyMatch(s -> s.getId().equals(notification.getScheduledItemId()))
                        || session1.getService().getSchedule().getRecurringItems().stream().anyMatch(s -> s.getId().equals(notification.getScheduledItemId()))) ;
        }
    }
}