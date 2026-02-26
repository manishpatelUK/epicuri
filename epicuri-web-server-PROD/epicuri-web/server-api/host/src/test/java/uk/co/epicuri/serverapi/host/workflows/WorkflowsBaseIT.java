package uk.co.epicuri.serverapi.host.workflows;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.NotificationType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
public abstract class WorkflowsBaseIT extends BaseIT {

    protected RestaurantImage restaurantImage3;
    protected Restaurant testRestaurant;
    protected final String staffPassword = "foobar";
    protected final String staffPIN = "1234";
    protected final String staffUserName = "mrmanchu";
    protected Staff testStaff;
    protected List<ScheduledItem> scheduledItemList = new ArrayList<>();
    protected List<ScheduledItem> recurringItemList = new ArrayList<>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        createTestRestaurant();
    }

    private void createTestRestaurant() throws Exception{
        testRestaurant = masterDataCreationService.createRestaurantWithDefaultMasterData(new Restaurant());

        Service service = testRestaurant.getServices().get(0);
        Schedule schedule = new Schedule(service);
        service.setSchedule(schedule);
        schedule.setName("Normal Schedule");

        ScheduledItem scheduledItem1 = new ScheduledItem(schedule);
        scheduledItem1.setInitialDelay(1000 * 60 * 5);
        scheduledItem1.setTimeAfterStart(1000 * 60);
        scheduledItem1.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem1.setText("Menus");
        scheduledItemList.add(scheduledItem1);

        ScheduledItem scheduledItem2 = new ScheduledItem(schedule);
        scheduledItem2.setInitialDelay(0);
        scheduledItem2.setTimeAfterStart(1000 * 60 * 5);
        scheduledItem2.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem2.setText("Take orders");
        scheduledItemList.add(scheduledItem2);

        ScheduledItem scheduledItem3 = new ScheduledItem(schedule);
        scheduledItem3.setInitialDelay(0);
        scheduledItem3.setTimeAfterStart(1000 * 60 * 30);
        scheduledItem3.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem3.setText("Take payments");
        scheduledItemList.add(scheduledItem3);

        ScheduledItem recurringItem1 = new ScheduledItem(schedule);
        recurringItem1.setInitialDelay(0);
        recurringItem1.setRecurring(1000 * 60);
        recurringItem1.setNotificationType(NotificationType.RECURRING);
        recurringItem1.setText("Take payments");
        recurringItemList.add(recurringItem1);

        schedule.getScheduledItems().add(scheduledItem1);
        schedule.getScheduledItems().add(scheduledItem2);
        schedule.getScheduledItems().add(scheduledItem3);
        schedule.getRecurringItems().add(recurringItem1);

        testRestaurant = restaurantRepository.save(testRestaurant);

        File file = new File("src/test/resources/floor.jpg");
        restaurantImage3 = new RestaurantImage();
        restaurantImage3.setImage(IOUtils.toByteArray(new FileInputStream(file)));
        restaurantImage3.setImageType(RestaurantImageType.FLOOR_PLAN);
        restaurantImage3 = restaurantImageRepository.save(restaurantImage3);
        restaurantImage3.setRestaurantId(testRestaurant.getId());

        Floor floor = new Floor(testRestaurant.getId());
        floor.setCapacity(100);
        floor.setName("floor1");
        floor.setScale(1);
        floor.setImageURL(restaurantImage1.getId());
        Layout layout = new Layout(floor);
        layout.setName("layout1");
        floor.setActiveLayout(layout.getId());

        for(int i = 0; i< 10; i++) {
            Table table = new Table();
            table.setName("table"+i);
            table.setId(IDAble.generateId(testRestaurant.getId()));
            table.setShape(i % 2 == 0 ? TableShape.CIRCLE : TableShape.SQUARE);
            Position position = new Position();
            position.setRotation(0);
            position.setScaleX(1);
            position.setScaleY(1);
            position.setX(i * 10);
            position.setY(i * 10);
            table.setPosition(position);
            testRestaurant.getTables().add(table);
            layout.getTables().add(table.getId());
        }

        testRestaurant = restaurantRepository.save(testRestaurant);

        testStaff = new Staff();
        testStaff.setName("Mr Foo Man");
        testStaff.setMash(AuthenticationUtil.encrypt(staffPassword));
        testStaff.setPin(staffPIN);
        testStaff.setRestaurantId(testRestaurant.getId());
        testStaff.setUserName(staffUserName);
        testStaff.setRole(StaffRole.MANAGER);
        testStaff = staffRepository.save(testStaff);
    }
}

