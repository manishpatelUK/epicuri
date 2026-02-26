package uk.co.epicuri.serverapi.service.reporting;

import org.junit.Ignore;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.List;

@Ignore
public class BaseReportingServiceTest extends SessionSetupBaseIT {
    protected String token;

    protected long bst1 =  1498690800000L; //23:00 June 28th 2017 UTC
    protected long bst2 =  1498737600000L; //12:00 June 29th 2017 UTC

    @Override
    public void setUp() throws Exception{
        super.setUpSession();

        restaurant1.setIANATimezone("Europe/London");
        restaurant1.getIntegrations().put(ExternalIntegration.MEWS, new KVData());
        restaurant1.getIntegrations().put(ExternalIntegration.PAYMENT_SENSE, new KVData());
        restaurant1.setISOCurrency("GBP");
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        restaurantRepository.save(restaurant1);

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setName("Foo Man Chu");
        staff1.setUserName("asdlfldsjf");
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);
        token = getTokenForStaff(staff1);

        session1.setStartTime(bst1 + 1000);
        session1.setClosedTime(bst2 - 1000);
        session1.setClosedBy(staff1.getId());
        session1.setRestaurantId(restaurant1.getId());
        session1.getTables().clear();
        session1.getTables().add(table1.getId());
        session1.setReadableId("1");
        for(int i = 0; i < 2; i++) {
            Diner diner = new Diner(session1);
            diner.setName("aName"+i);
            session1.getDiners().add(diner);
        }
        session1.getDiners().get(0).setDefaultDiner(true);
        SessionArchive sessionArchive = new SessionArchive(session1);
        List<Order> orders = orderRepository.findBySessionId(session1.getId());
        orderRepository.delete(orders);
        orders.forEach(o ->{
            o.setTime(bst1 + 2000);
            o.setStaffId(staff1.getId());
            //set all items PLU to m1
            o.getMenuItem().setPlu("m1");
        }); //Thu Jun 29 2017 00:00:02
        sessionArchive.setOrders(orders);
        addAdjustments(session1);
        sessionArchiveRepository.insert(sessionArchive);
        sessionRepository.delete(session1.getId());

        session2.setStartTime(bst1 + 1000);
        session2.setClosedTime(bst2 - 1000);
        session2.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session2);

        session3.setStartTime(bst1 + 1000);
        session3.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session3);
    }

    protected void addAdjustments(Session session) {
        Adjustment discount1 = new Adjustment(session.getId());
        discount1.setValue(166);
        discount1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        discount1.setAdjustmentType(adjustmentType1);
        discount1.setStaffId(staff1.getId());
        discount1.setCreated(bst1 + 1000);
        session.getAdjustments().add(discount1);

        Adjustment payment1 = new Adjustment(session.getId());
        payment1.setValue(200);
        payment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        payment1.setAdjustmentType(adjustmentType2);
        payment1.setStaffId("1");
        payment1.setCreated(bst1 + 2000);
        session.getAdjustments().add(payment1);
    }
}
