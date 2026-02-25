package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
public abstract class SessionSetupBaseIT extends BaseIT {
    /**
     * set up orders so that session 1 contains
     *
     * 1 x menu item 1 (tax @ 0%, DRINK, 10) -> 10 (VAT = 0)
     * 2 x menu item 2 (tax @ 20%, FOOD, 20) + Modifiers (113, tax @ 20%) -> 40 + 226 (VAT = 44)
     * 3 x menu item 3 (tax @ 17.5%, OTHER, 30) -> 90 (VAT=13 or 14 if you round up)
     *
     * session does not have a tip assigned
     */
    protected void setUpSession() throws Exception{
        super.setUp();

        setUpOrders();

        List<Modifier> modifiers = new ArrayList<>();
        modifiers.add(modifier1);
        modifier1.setPrice(113);
        modifier1.setPriceOverride(113);
        modifier1.setTaxTypeId(tax2.getId());
        modifier1.setTaxRate(tax2);
        modifierRepository.save(modifier1);

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItem1 = menuItemRepository.save(menuItem1);
        menuItem2 = menuItemRepository.save(menuItem2);
        menuItem3 = menuItemRepository.save(menuItem3);

        session1.getDiners().clear();
        diner1 = new Diner(session1);
        diner1.setDefaultDiner(true);
        diner2 = new Diner(session1);
        diner3 = new Diner(session1);
        session1.getDiners().clear();
        session1.getDiners().add(diner1);
        session1.getDiners().add(diner2);
        session1.getDiners().add(diner3);

        order1.setSessionId(session1.getId());
        order1.setQuantity(1);
        order1.setItemPrice(menuItem1.getPrice());
        order1.setDinerId(diner1.getId());
        order2.setModifiers(modifiers);
        order2.setSessionId(session1.getId());
        order2.setQuantity(2);
        order2.setItemPrice(menuItem2.getPrice());
        order2.setDinerId(diner2.getId());
        order3.setSessionId(session1.getId());
        order3.setQuantity(3);
        order3.setItemPrice(menuItem3.getPrice());
        order3.setDinerId(diner3.getId());
        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        order3 = orderRepository.save(order3);

        session1.setRestaurantId(restaurant1.getId());
        session1.getAdjustments().clear();
        session1.setTipPercentage(0D);

        service1.setActive(true);
        service1.setDefaultMenuId(menu1.getId());
        menu1.setRestaurantId(restaurant1.getId());
        service1.setName("Default Service");
        service1.setSchedule(schedule1);
        service1.setSessionType(SessionType.SEATED);
        session1.setService(service1);
        session1.setStartTime(System.currentTimeMillis());
        session1.setSessionType(SessionType.SEATED);

        party1.setNumberOfPeople(2);
        party1 = partyRepository.save(party1);

        session1.setOriginalParty(party1);
        session1.getTables().add(table1.getId());

        session1.setOriginalBooking(null);
        session1.setVoidReason(null);

        session1.setDelay(0);

        session1 = sessionRepository.save(session1);

        menu1 = menuRepository.save(menu1);

        setUpNotifications();
    }

    protected void setUpTakeawayDeliverySession() throws Exception{
        setUpSession();

        Address deliveryAddress = new Address();
        deliveryAddress.setCity("London");
        deliveryAddress.setPostcode("HA6 1AU");
        deliveryAddress.setStreet("61 Northwood Way");
        booking1.setDeliveryAddress(deliveryAddress);
        bookingRepository.save(booking1);

        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setTakeawayType(TakeawayType.DELIVERY);
        session1.setOriginalBooking(booking1);
        session1 = sessionRepository.save(session1);

        Address restaurantAddress = new Address();
        restaurantAddress.setCity("Leicester");
        restaurantAddress.setPostcode("LE7 9UD");
        restaurantAddress.setStreet("17 Pulford Drive");
        restaurant1.setAddress(restaurantAddress);
        restaurantRepository.save(restaurant1);
    }

    protected void setUpNotifications() {
        notification1.setSessionId(session1.getId());
        notification1.setRestaurantId(restaurant1.getId());
        notification1.setScheduledItemId(schedule1.getScheduledItems().get(0).getId());
        notification1.setNotificationType(NotificationType.SCHEDULED);
        notification2.setSessionId(session1.getId());
        notification2.setRestaurantId(restaurant1.getId());
        notification2.setScheduledItemId(schedule1.getScheduledItems().get(1).getId());
        notification2.setNotificationType(NotificationType.SCHEDULED);
        notification3.setSessionId(session1.getId());
        notification3.setRestaurantId(restaurant1.getId());
        notification3.setScheduledItemId(schedule1.getScheduledItems().get(2).getId());
        notification3.setNotificationType(NotificationType.SCHEDULED);

        notification1 = notificationRepository.save(notification1);
        notification2 = notificationRepository.save(notification2);
        notification3 = notificationRepository.save(notification3);
    }

    protected void setUpMixedSessions() {
        service1.setSessionType(SessionType.TAKEAWAY);
        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        service1.setId(IDAble.generateId(restaurant1.getId()));
        service1.setName("Takeaway Service");
        menu1.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);
        service1.setDefaultMenuId(menu1.getId());
        restaurantRepository.save(restaurant1);

        session1.setRestaurantId(restaurant1.getId());
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setTakeawayType(TakeawayType.COLLECTION);
        session1.setStartTime(10L);
        session1.setService(service1);
        booking1.setTargetTime(10L);
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTakeawayType(TakeawayType.COLLECTION);
        bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1.setDiners(createDiners(session1, true));

        session2.setRestaurantId(restaurant1.getId());
        session2.setSessionType(SessionType.TAKEAWAY);
        session2.setTakeawayType(TakeawayType.DELIVERY);
        session2.setStartTime(10L);
        session2.setService(service1);
        session2.setCalculatedDeliveryCost(0);
        booking2.setTargetTime(10L);
        booking2.setRestaurantId(restaurant1.getId());
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setTakeawayType(TakeawayType.DELIVERY);
        bookingRepository.save(booking2);
        session2.setOriginalBooking(booking2);
        session2.setDiners(createDiners(session2, true));

        session3.setRestaurantId(restaurant1.getId());
        session3.setSessionType(SessionType.SEATED);
        session3.setStartTime(10L);
        session3.setService(service1);
        booking3.setTargetTime(10L);
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setBookingType(BookingType.RESERVATION);
        booking3.setTakeawayType(TakeawayType.NONE);
        bookingRepository.save(booking3);
        session3.setOriginalBooking(booking3);
        session3.setDiners(createDiners(session3, false));

        session4.setRestaurantId(restaurant1.getId());
        session4.setSessionType(SessionType.TAB);
        session4.setStartTime(10L);
        session4.setService(service1);
        session4.setDiners(createDiners(session4, false));

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
        sessionRepository.save(session4);
    }

    private List<Diner> createDiners(Session session, boolean isTakeaway) {
        List<Diner> diners = new ArrayList<>();
        Diner diner = new Diner(session);
        diners.add(diner);

        if(!isTakeaway) {
            diner.setDefaultDiner(true);
            Diner extraDiner = new Diner(session);
            diners.add(extraDiner);
        }

        return diners;
    }
}
