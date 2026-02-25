package uk.co.epicuri.serverapi.repository;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.ChairData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SessionRepositoryImplTest extends BaseIT {
    @Test
    public void testFindByStartTime() throws Exception {
        session1.setStartTime(10L);
        session1.setRestaurantId(restaurant1.getId());
        session2.setStartTime(20L);
        session2.setRestaurantId(restaurant1.getId());
        session3.setStartTime(10L);
        session3.setRestaurantId(restaurant2.getId());
        Session session4 = new Session(); //0 start time
        session4.setRestaurantId(restaurant1.getId());

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
        sessionRepository.save(session4);

        List<Session> list1 = sessionRepository.findByStartTime(restaurant1.getId(), 0, 30);
        List<Session> list2 = sessionRepository.findByStartTime(restaurant1.getId(), 10, 20);
        List<Session> list3 = sessionRepository.findByStartTime(restaurant1.getId(), 10, 19);
        List<Session> list4 = sessionRepository.findByStartTime(restaurant1.getId(), 11, 19);

        assertEquals(3, list1.size());
        List<String> list1IDs = list1.stream().map(Session::getId).collect(Collectors.toList());
        assertTrue(list1IDs.contains(session1.getId()));
        assertTrue(list1IDs.contains(session2.getId()));
        assertTrue(list1IDs.contains(session4.getId()));

        assertEquals(2, list2.size());
        List<String> list2IDs = list2.stream().map(Session::getId).collect(Collectors.toList());
        assertTrue(list2IDs.contains(session1.getId()));
        assertTrue(list2IDs.contains(session2.getId()));

        assertEquals(1, list3.size());
        assertEquals(session1.getId(), list3.get(0).getId());

        assertEquals(0, list4.size());
    }

    @Test
    public void testFindByEndTime() throws Exception {
        session1.setStartTime(0);
        session1.setClosedTime(1552552367479L);
        session1.setRestaurantId(restaurant1.getId());
        session2.setStartTime(0);
        session2.setClosedTime(null);
        session2.setRestaurantId(restaurant1.getId());
        session3.setStartTime(0);
        session3.setClosedTime(1552582800000L);
        session3.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<Session> byCloseTime = sessionRepository.findByCloseTime(restaurant1.getId(), 1552582800000L, 1552644000000L);
        assertEquals(1, byCloseTime.size());
        assertEquals(session3.getId(), byCloseTime.get(0).getId());

        session2.setClosedTime(1552582800000L+1);
        session1.setClosedTime(1552644000000L+1);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        byCloseTime = sessionRepository.findByCloseTime(restaurant1.getId(), 1552582800000L, 1552644000000L);
        assertEquals(2, byCloseTime.size());
    }

    @Test
    public void testFindTakeawaySessions() throws Exception {
        session1.setRestaurantId(restaurant2.getId());
        session1.setOriginalBooking(booking1);
        session2.setTakeawayType(TakeawayType.COLLECTION);
        session2.setRestaurantId(restaurant2.getId());
        session2.setOriginalBooking(booking2);
        session3.setRestaurantId(restaurant3.getId());
        session3.setOriginalBooking(booking3);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<String> ids = new ArrayList<>();
        ids.add(booking1.getId());
        ids.add(booking2.getId());
        ids.add(booking3.getId());
        ids.add("foo");

        List<Session> list1 = sessionRepository.findTakeawaySessions(restaurant1.getId(),ids);
        List<Session> list2 = sessionRepository.findTakeawaySessions(restaurant2.getId(),ids);
        session2.setTakeawayType(TakeawayType.DELIVERY);
        sessionRepository.save(session2);
        List<Session> list3 = sessionRepository.findTakeawaySessions(restaurant2.getId(),ids);
        ids.remove(booking2.getId());
        List<Session> list4 = sessionRepository.findTakeawaySessions(restaurant2.getId(),ids);
        List<Session> list5 = sessionRepository.findTakeawaySessions(restaurant2.getId(),new ArrayList<>());
        session2.setOriginalBooking(null);
        ids.add(booking2.getId());
        sessionRepository.save(session2);
        List<Session> list6 = sessionRepository.findTakeawaySessions(restaurant2.getId(),ids);

        assertEquals(0, list1.size());

        assertEquals(1, list2.size());
        assertEquals(session2.getId(), list2.get(0).getId());
        assertEquals(1, list3.size());
        assertEquals(session2.getId(), list3.get(0).getId());

        assertEquals(0, list4.size());
        assertEquals(0, list5.size());
        assertEquals(0, list6.size());
    }

    @Test
    public void testFindCurrentLiveSessions() throws Exception {
        session1.setStartTime(10L);
        session1.setRestaurantId(restaurant1.getId());
        session2.setStartTime(20L);
        session2.setClosedTime(30L);
        session2.setRestaurantId(restaurant1.getId());
        Session session4 = new Session(); //0 start time
        session4.setRestaurantId(restaurant1.getId());
        session3.setStartTime(10L);
        session3.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
        session4 = sessionRepository.save(session4);

        List<Session> list1 = sessionRepository.findCurrentLiveSessions(restaurant1.getId(), 0);
        List<Session> list2 = sessionRepository.findCurrentLiveSessions(restaurant1.getId(), 10);
        List<Session> list3 = sessionRepository.findCurrentLiveSessions(restaurant1.getId(), 100);
        List<Session> list4 = sessionRepository.findCurrentLiveSessions(restaurant2.getId(), 0);
        List<Session> list5 = sessionRepository.findCurrentLiveSessions(restaurant2.getId(), 10);
        List<Session> list6 = sessionRepository.findCurrentLiveSessions(restaurant2.getId(), 100);
        session2.setClosedTime(null);
        sessionRepository.save(session2);
        List<Session> list7 = sessionRepository.findCurrentLiveSessions(restaurant1.getId(), 100);
        session2.setDeleted(100L);
        sessionRepository.save(session2);
        List<Session> list8 = sessionRepository.findCurrentLiveSessions(restaurant1.getId(), 100);


        assertEquals(1, list1.size());
        assertEquals(session4.getId(), list1.get(0).getId());

        assertEquals(2, list2.size());
        assertTrue(list2.get(0).getId().equals(session1.getId()) || list2.get(1).getId().equals(session1.getId()));

        assertEquals(2, list3.size());
        assertTrue(list2.get(0).getId().equals(session1.getId()) || list2.get(1).getId().equals(session1.getId()));

        assertEquals(0, list4.size());

        assertEquals(1, list5.size());
        assertEquals(session3.getId(), list5.get(0).getId());

        assertEquals(1, list6.size());
        assertEquals(session3.getId(), list6.get(0).getId());

        assertEquals(3, list7.size());
        assertTrue(list7.stream().anyMatch(s -> s.getId().equals(session1.getId())));
        assertTrue(list7.stream().anyMatch(s -> s.getId().equals(session2.getId())));
        Session finalSession4 = session4;
        assertTrue(list7.stream().anyMatch(s -> s.getId().equals(finalSession4.getId())));

        assertEquals(2, list8.size());
        assertTrue(list8.stream().anyMatch(s -> s.getId().equals(session1.getId())));
        assertTrue(list8.stream().anyMatch(s -> s.getId().equals(finalSession4.getId())));
    }

    @Test
    public void testFindCurrentSeatedSessions() throws Exception {
        session1.setRestaurantId(restaurant1.getId());
        session1.setSessionType(SessionType.SEATED);
        session2.setClosedTime(30L);
        session2.setSessionType(SessionType.SEATED);
        session2.setRestaurantId(restaurant1.getId());
        session3.setRestaurantId(restaurant2.getId());
        session3.setSessionType(SessionType.SEATED);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<Session> list1 = sessionRepository.findCurrentSeatedSessions(restaurant1.getId());
        List<Session> list2 = sessionRepository.findCurrentSeatedSessions(restaurant2.getId());
        session1.setSessionType(SessionType.ADHOC);
        sessionRepository.save(session1);
        List<Session> list3 = sessionRepository.findCurrentSeatedSessions(restaurant1.getId());

        assertEquals(1, list1.size());
        assertEquals(session1.getId(), list1.get(0).getId());

        assertEquals(1, list2.size());
        assertEquals(session3.getId(), list2.get(0).getId());

        assertEquals(0, list3.size());
    }

    @Test
    public void testPushDiners() throws Exception {
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);

        Diner diner4 = new Diner(session1);
        Diner diner5 = new Diner(session1);
        List<Diner> list = new ArrayList<>();
        list.add(diner4);
        list.add(diner5);

        sessionRepository.pushDiners(session1.getId(), list);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(2, s1.getDiners().size());
        assertEquals(diner4.getId(), s1.getDiners().get(0).getId());
        assertEquals(diner5.getId(), s1.getDiners().get(1).getId());

        assertEquals(0, s2.getDiners().size());

        sessionRepository.pushDiners(session1.getId(), list); //should not double up

        s1 = sessionRepository.findOne(session1.getId());
        assertEquals(2, s1.getDiners().size());
        assertEquals(diner4.getId(), s1.getDiners().get(0).getId());
        assertEquals(diner5.getId(), s1.getDiners().get(1).getId());
    }

    @Test
    public void testSetService() throws Exception {
        assert session1.getService() == null;

        sessionRepository.setService(session1.getId(), service1);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNotNull(s1.getService());
        assertNull(s2.getService());
        assertEquals(service1.getId(), s1.getService().getId());

        sessionRepository.setService(session1.getId(), null);
        sessionRepository.save(session1);
        s1 = sessionRepository.findOne(session1.getId());

        assertNull(s1.getService());
    }

    @Test
    public void testSetName() throws Exception {
        String currentName = session1.getName();
        String newName = currentName + "foo";

        sessionRepository.setName(session1.getId(), newName);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNotNull(s1.getName());
        assertNotEquals(newName, s2.getName());
        assertEquals(newName, s1.getName());

        sessionRepository.setName(session1.getId(), null);
        s1 = sessionRepository.findOne(session1.getId());

        assertNull(s1.getName());
    }

    @Test
    public void testPushTables() throws Exception {
        assert session1.getTables().size() == 0;

        List<String> list = new ArrayList<>();
        list.add(table1.getId());

        sessionRepository.pushTables(session1.getId(), list);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(1, s1.getTables().size());
        assertEquals(table1.getId(), s1.getTables().get(0));

        assertEquals(0, s2.getTables().size());

        list.add(table2.getId());
        sessionRepository.pushTables(session1.getId(), list);
        s1 = sessionRepository.findOne(session1.getId());

        assertEquals(2, s1.getTables().size());

        sessionRepository.pushTables(session1.getId(), list);
        s1 = sessionRepository.findOne(session1.getId());

        assertEquals(2, s1.getTables().size());
    }

    @Test
    public void testSetTables() throws Exception {
        assert session1.getTables().size() == 0;

        List<String> list = new ArrayList<>();
        list.add(table1.getId());

        sessionRepository.setTables(session1.getId(), list);

        Session s1 = sessionRepository.findOne(session1.getId());
        assertEquals(1, s1.getTables().size());
        assertEquals(list.get(0), s1.getTables().get(0));

        list.clear();
        list.add(table2.getId());

        sessionRepository.setTables(session1.getId(), list);

        s1 = sessionRepository.findOne(session1.getId());
        assertEquals(1, s1.getTables().size());
        assertEquals(list.get(0), s1.getTables().get(0));
    }

    @Test
    public void testSetBillRequested() throws Exception {
        sessionRepository.setBillRequested(session2.getId(), true);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isBillRequested());
        assertTrue(s2.isBillRequested());

        sessionRepository.setBillRequested(session2.getId(), false);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isBillRequested());
        assertFalse(s2.isBillRequested());

        sessionRepository.setBillRequested(session2.getId(), false);

        s2 = sessionRepository.findOne(session2.getId());
        assertFalse(s2.isBillRequested());
    }

    @Test
    public void testSetRemoveFromReports() throws Exception {
        sessionRepository.setRemoveFromReports(session2.getId(), true);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isRemoveFromReports());
        assertTrue(s2.isRemoveFromReports());

        sessionRepository.setRemoveFromReports(session2.getId(), false);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isRemoveFromReports());
        assertFalse(s2.isRemoveFromReports());

        sessionRepository.setRemoveFromReports(session2.getId(), false);

        s2 = sessionRepository.findOne(session2.getId());
        assertFalse(s2.isRemoveFromReports());
    }

    /*@Test
    public void testSetPaid() throws Exception {
        sessionRepository.setPaid(session2.getId(), true);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isPaid());
        assertTrue(s2.isPaid());

        sessionRepository.setPaid(session2.getId(), false);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertFalse(s1.isPaid());
        assertFalse(s2.isPaid());

        sessionRepository.setPaid(session2.getId(), false);

        s2 = sessionRepository.findOne(session2.getId());
        assertFalse(s2.isPaid());
    }*/

    @Test
    public void testSetClosed() throws Exception {
        sessionRepository.setClosed(session2.getId(), 10L);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getClosedTime());
        assertEquals(10L,(long)s2.getClosedTime());

        sessionRepository.setClosed(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getClosedTime());
        assertNull(s2.getClosedTime());
    }

    @Test
    public void testSetStartTime() throws Exception {
        sessionRepository.setStartTime(session2.getId(), 10L);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0L, s1.getStartTime());
        assertEquals(10L,s2.getStartTime());

        sessionRepository.setStartTime(session2.getId(), 0L);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0L, s1.getStartTime());
        assertEquals(0L, s2.getStartTime());
    }

    @Test
    public void testSetVoid() throws Exception {
        VoidReason voidReason = new VoidReason();
        voidReason.setDescription("foo");

        sessionRepository.setVoid(session2.getId(), voidReason);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getVoidReason());
        assertEquals(voidReason.getDescription(),s2.getVoidReason().getDescription());

        sessionRepository.setVoid(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getVoidReason());
        assertNull(s2.getVoidReason());
    }

    @Test
    public void testSetChairData() throws Exception {
        String chairDataString = "[{\"breadth\":200,\"dinerId\":5292,\"type\":\"table\",\"rotation\":90,\"y\":500,\"width\":150,\"x\":500},{\"breadth\":200,\"dinerId\":5293,\"type\":\"table\",\"rotation\":90,\"y\":220,\"width\":150,\"x\":280},{\"breadth\":200,\"dinerId\":5294,\"type\":\"table\",\"rotation\":90,\"y\":220,\"width\":150,\"x\":720},{\"breadth\":200,\"dinerId\":5295,\"type\":\"table\",\"rotation\":90,\"y\":780,\"width\":150,\"x\":280},{\"breadth\":200,\"dinerId\":5296,\"type\":\"table\",\"rotation\":90,\"y\":780,\"width\":150,\"x\":720}]";
        List<ChairData> chairData = Lists.newArrayList(ControllerUtil.OBJECT_MAPPER.readValue(chairDataString, ChairData[].class));

        assertNotNull(chairData);
        assertTrue(chairData.size() > 0);

        sessionRepository.setChairData(session2.getId(), chairData);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0, s1.getChairData().size());
        assertEquals(chairData,s2.getChairData());

        sessionRepository.setChairData(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0, s1.getChairData().size());
        assertNull(s2.getChairData());
    }

    @Test
    public void testSetTipPercentage() throws Exception {
        double tip = 2;
        sessionRepository.setTipPercentage(session2.getId(), tip);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getTipPercentage());
        assertEquals(0, Double.compare(tip,s2.getTipPercentage()));

        sessionRepository.setTipPercentage(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getTipPercentage());
        assertNull(s2.getTipPercentage());
    }

    @Test
    public void testSetDeliveryCost() throws Exception {
        int deliveryCost = 2;
        sessionRepository.setCalculatedDeliveryCost(session2.getId(), deliveryCost);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getCalculatedDeliveryCost());
        assertEquals(0, Integer.compare(deliveryCost,s2.getCalculatedDeliveryCost()));

        sessionRepository.setCalculatedDeliveryCost(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertNull(s1.getCalculatedDeliveryCost());
        assertNull(s2.getCalculatedDeliveryCost());
    }

    @Test
    public void testSetSessionType() throws Exception {
        SessionType type = SessionType.TAB;
        sessionRepository.setSessionType(session2.getId(), type);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(session1.getSessionType(), s1.getSessionType());
        assertEquals(type, s2.getSessionType());

        sessionRepository.setSessionType(session2.getId(), null);

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(session1.getSessionType(), s1.getSessionType());
        assertNull(s2.getSessionType());
    }

    @Test
    public void testUpdateDiner() throws Exception {
        assert diner1.getId() != null;
        assert diner1.getCustomerId() == null;

        session2.getDiners().add(diner1);
        sessionRepository.save(session2);

        diner1.setCustomerId("foo");
        sessionRepository.updateDiner(session2.getId(), diner1);

        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(1, s2.getDiners().size());
        assertEquals(diner1.getCustomerId(), s2.getDiners().get(0).getCustomerId());

        assert diner2.getId() != null;
        assert diner2.getCustomerId() == null;

        session2.getDiners().add(diner2);
        sessionRepository.save(session2);

        diner2.setCustomerId("bar");
        sessionRepository.updateDiner(session2.getId(), diner2);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(2, s2.getDiners().size());
        assertEquals(diner1.getCustomerId(), s2.getDiners().get(0).getCustomerId());
        assertEquals(diner2.getCustomerId(), s2.getDiners().get(1).getCustomerId());

        diner3.setCustomerId("bar");
        sessionRepository.updateDiner(session2.getId(), diner3);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(2, s2.getDiners().size());
        assertEquals(diner1.getCustomerId(), s2.getDiners().get(0).getCustomerId());
        assertEquals(diner2.getCustomerId(), s2.getDiners().get(1).getCustomerId());
    }

    @Test
    public void testPushDiner() throws Exception {
        assert session1.getDiners().size() == 0;
        assert session2.getDiners().size() == 0;

        sessionRepository.pushDiner(session2.getId(), diner1);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0, s1.getDiners().size());
        assertEquals(1, s2.getDiners().size());
        assertEquals(diner1.getId(), s2.getDiners().get(0).getId());

        sessionRepository.pushDiner(session2.getId(), diner1);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(1, s2.getDiners().size());
        assertEquals(diner1.getId(), s2.getDiners().get(0).getId());

        sessionRepository.pushDiner(session2.getId(), diner2);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(2, s2.getDiners().size());
        assertEquals(diner1.getId(), s2.getDiners().get(0).getId());
        assertEquals(diner2.getId(), s2.getDiners().get(1).getId());
    }

    @Test
    public void testRemoveDiner() throws Exception {
        session2.getDiners().add(diner1);
        session2.getDiners().add(diner2);
        session3.getDiners().add(diner3);

        sessionRepository.save(session2);
        sessionRepository.save(session3);

        sessionRepository.removeDiner(session2.getId(), diner1.getId());

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());
        Session s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getDiners().size());
        assertEquals(1, s2.getDiners().size());
        assertEquals(diner2.getId(), s2.getDiners().get(0).getId());
        assertEquals(1, s3.getDiners().size());
        assertEquals(diner3.getId(), s3.getDiners().get(0).getId());

        sessionRepository.removeDiner(session2.getId(), diner1.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getDiners().size());
        assertEquals(1, s2.getDiners().size());
        assertEquals(diner2.getId(), s2.getDiners().get(0).getId());
        assertEquals(1, s3.getDiners().size());
        assertEquals(diner3.getId(), s3.getDiners().get(0).getId());

        sessionRepository.removeDiner(session2.getId(), diner2.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getDiners().size());
        assertEquals(0, s2.getDiners().size());
        assertEquals(1, s3.getDiners().size());
        assertEquals(diner3.getId(), s3.getDiners().get(0).getId());

        sessionRepository.removeDiner(session2.getId(), diner2.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getDiners().size());
        assertEquals(0, s2.getDiners().size());
        assertEquals(1, s3.getDiners().size());
        assertEquals(diner3.getId(), s3.getDiners().get(0).getId());
    }

    @Test
    public void testPushAdjustment() throws Exception {
        assert session1.getAdjustments().size() == 0;
        assert session2.getAdjustments().size() == 0;

        sessionRepository.pushAdjustment(session2.getId(), adjustment1);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(0, s1.getAdjustments().size());
        assertEquals(1, s2.getAdjustments().size());
        assertEquals(adjustment1.getId(), s2.getAdjustments().get(0).getId());

        sessionRepository.pushAdjustment(session2.getId(), adjustment2);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(2, s2.getAdjustments().size());
        assertEquals(adjustment1.getId(), s2.getAdjustments().get(0).getId());
        assertEquals(adjustment2.getId(), s2.getAdjustments().get(1).getId());
    }

    @Test
    public void testRemoveAdjustment() throws Exception {
        session2.getAdjustments().add(adjustment1);
        session2.getAdjustments().add(adjustment2);
        session3.getAdjustments().add(adjustment3);

        sessionRepository.save(session2);
        sessionRepository.save(session3);

        sessionRepository.removeAdjustment(session2.getId(), adjustment1.getId());

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());
        Session s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getAdjustments().size());
        assertEquals(1, s2.getAdjustments().size());
        assertEquals(adjustment2.getId(), s2.getAdjustments().get(0).getId());
        assertEquals(1, s3.getAdjustments().size());
        assertEquals(adjustment3.getId(), s3.getAdjustments().get(0).getId());

        sessionRepository.removeAdjustment(session2.getId(), adjustment1.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getAdjustments().size());
        assertEquals(1, s2.getAdjustments().size());
        assertEquals(adjustment2.getId(), s2.getAdjustments().get(0).getId());
        assertEquals(1, s3.getAdjustments().size());
        assertEquals(adjustment3.getId(), s3.getAdjustments().get(0).getId());

        sessionRepository.removeAdjustment(session2.getId(), adjustment2.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getAdjustments().size());
        assertEquals(0, s2.getAdjustments().size());
        assertEquals(1, s3.getAdjustments().size());
        assertEquals(adjustment3.getId(), s3.getAdjustments().get(0).getId());

        sessionRepository.removeAdjustment(session2.getId(), adjustment2.getId());

        s1 = sessionRepository.findOne(session1.getId());
        s2 = sessionRepository.findOne(session2.getId());
        s3 = sessionRepository.findOne(session3.getId());

        assertEquals(0, s1.getAdjustments().size());
        assertEquals(0, s2.getAdjustments().size());
        assertEquals(1, s3.getAdjustments().size());
        assertEquals(adjustment3.getId(), s3.getAdjustments().get(0).getId());
    }

    @Test
    public void testSetDelay() throws Exception {
        assert session2.getDelay() == 0;

        long delay = 10L;
        sessionRepository.setDelay(session2.getId(), delay);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(session1.getDelay(), s1.getDelay());
        assertEquals(delay, s2.getDelay());
    }

    @Test
    public void testIncrementDelay() throws Exception {
        assert session2.getDelay() == 0;

        long increment = 10L;
        sessionRepository.incrementDelay(session2.getId(), increment);

        Session s1 = sessionRepository.findOne(session1.getId());
        Session s2 = sessionRepository.findOne(session2.getId());

        assertEquals(session1.getDelay(), s1.getDelay());
        assertEquals(increment, s2.getDelay());

        sessionRepository.incrementDelay(session2.getId(), increment);

        s2 = sessionRepository.findOne(session2.getId());

        assertEquals(increment*2, s2.getDelay());
    }
}
