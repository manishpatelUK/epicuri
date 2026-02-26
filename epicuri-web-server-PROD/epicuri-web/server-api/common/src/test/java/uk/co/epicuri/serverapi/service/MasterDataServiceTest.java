package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.authentication.StaffAuthentication;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.Cuisine;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.StaffAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class MasterDataServiceTest extends BaseIT {

    @Test
    public void testGetDialingCode() throws Exception {
        assertEquals("00", masterDataService.getDialingCode("foobar"));
        assertEquals("00", masterDataService.getDialingCode(null));
        assertEquals("0044", masterDataService.getDialingCode("GB"));
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetRestaurant() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetRestaurantByStaffFacingId() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testSetRestaurantTakeawayMenu() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testSearchRestaurants() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetStaff() throws Exception {

    }

    @Test
    public void testGetStaffByUserNameAndStaffFacingId() throws Exception {
        String userName = "foo";
        staff2.setUserName(userName);
        staff2.setRestaurantId(restaurant1.getId());
        staff3.setUserName(userName);
        staff3.setRestaurantId(restaurant2.getId());
        staffRepository.save(staff2);
        staffRepository.save(staff3);

        Staff staff = masterDataService.getStaff(userName, restaurant2.getStaffFacingId());

        assertEquals(staff3.getId(), staff.getId());
    }

    @Test
    public void testGetStaffByUserNameAndStaffFacingIdWhenMultipleStaffs() throws Exception {
        staffRepository.deleteAll();
        staff1.setRestaurantId(restaurant2.getId());
        String userName = "foo";
        staff1.setUserName(userName);
        staff1.setId(null);
        staff1.setDeleted(0L);
        staff2.setRestaurantId(restaurant2.getId());
        staff2.setUserName(userName);
        staff2.setId(null);
        staff3.setRestaurantId(restaurant2.getId());
        staff3.setUserName(userName);
        staff3.setId(null);
        staff3.setDeleted(0L);
        staff1 = staffRepository.save(staff1);
        staff2 = staffRepository.save(staff2);
        staff3 = staffRepository.save(staff3);

        Staff staff = masterDataService.getStaff(userName, restaurant2.getStaffFacingId());
        assertEquals(staff2, staff);
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAllStaff() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsert() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testSoftDeleteStaff() throws Exception {

    }

    @Test
    public void testInsertStaffAuthentication() throws Exception {
        StaffAuthentication staffAuthenticationKey = new StaffAuthentication();
        staffAuthenticationKey.setExpires(System.currentTimeMillis() + 60000);
        staffAuthenticationKey.setKey(RandomStringUtils.randomAlphabetic(8));
        staffAuthenticationKey.setRestaurantId(restaurant2.getId());
        staffAuthenticationKey.setStaffId(staff2.getId());
        staffAuthenticationKey.setUsername("foo");

        staffAuthenticationsRepository.deleteAll();
        masterDataService.insertStaffAuthentication(staffAuthenticationKey);

        List<StaffAuthentications> list = staffAuthenticationsRepository.findAll();

        assertEquals(1, list.size());
        assertEquals(staff2.getId(), list.get(0).getStaffId());
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testInsertStaffAuthentication1() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAuthentication() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetStaffRole() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetCuisines() throws Exception {

    }

    
    @Test
    public void testUpsertCuisine() throws Exception {
        String cuisine = "italian";
        masterDataService.upsertCuisine(cuisine);

        List<Cuisine> list = cuisineRepository.findAll();
        assertEquals(1, list.stream().filter(c -> c.getName().equals("Italian")).collect(Collectors.toList()).size());

        masterDataService.upsertCuisine(cuisine);

        list = cuisineRepository.findAll();
        assertEquals(1, list.stream().filter(c -> c.getName().equals("Italian")).collect(Collectors.toList()).size());

        masterDataService.upsertCuisine("Italian");

        assertEquals(1, list.stream().filter(c -> c.getName().equals("Italian")).collect(Collectors.toList()).size());
    }

    @Test
    public void testGetCoursesByRestaurantId() throws Exception {
        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        service1.getCourses().clear();
        course1.setId(service1.getId() + IDAble.SEPARATOR + course1.getId());
        service1.getCourses().add(course1);
        course2.setId(service1.getId() + IDAble.SEPARATOR + course2.getId());
        service1.getCourses().add(course2);
        service2.getCourses().clear();
        course3.setId(service2.getId() + IDAble.SEPARATOR + course3.getId());
        service2.getCourses().add(course3);

        restaurantRepository.save(restaurant1);

        List<Course> courses1 = masterDataService.getCoursesByRestaurantId(restaurant1.getId());
        List<Course> courses2 = masterDataService.getCoursesByRestaurantId(restaurant2.getId());

        assertEquals(3, courses1.size());
        assertTrue(courses1.stream().anyMatch(c -> c.getId().equals(course1.getId())));
        assertTrue(courses1.stream().anyMatch(c -> c.getId().equals(course2.getId())));
        assertTrue(courses1.stream().anyMatch(c -> c.getId().equals(course3.getId())));

        assertEquals(0, courses2.size());
    }

    @Test
    public void testGetCoursesByServiceId() throws Exception {
        restaurant1.getServices().clear();
        service1.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        service1.getCourses().clear();
        course1.setId(service1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service1.getCourses().add(course1);
        course2.setId(service1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service1.getCourses().add(course2);
        service2.getCourses().clear();
        course3.setId(service2.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.getCourses().add(course3);

        restaurantRepository.save(restaurant1);

        List<Course> courses1 = masterDataService.getCoursesByServiceId(service1.getId());
        List<Course> courses2 = masterDataService.getCoursesByServiceId(service2.getId());
        List<Course> courses3 = masterDataService.getCoursesByServiceId(service3.getId());

        assertEquals(2, courses1.size());
        assertEquals(course1.getId(), courses1.get(0).getId());
        assertEquals(course2.getId(), courses1.get(1).getId());

        assertEquals(1, courses2.size());
        assertEquals(course3.getId(), courses2.get(0).getId());

        assertEquals(0, courses3.size());
    }

    @Test
    public void testGetCourses() throws Exception {
        restaurant1.getServices().clear();
        service1.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        service1.getCourses().clear();
        course1.setId(service1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service1.getCourses().add(course1);
        course2.setId(service1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service1.getCourses().add(course2);
        service2.getCourses().clear();
        course3.setId(service2.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.getCourses().add(course3);

        restaurantRepository.save(restaurant1);

        List<String> list = new ArrayList<>();

        list.add(restaurant1.getId() + IDAble.SEPARATOR + "foobar");
        List<Course> courses1 = masterDataService.getCourses(list);
        list.add(course2.getId());
        List<Course> courses2 = masterDataService.getCourses(list);
        list.add(service1.getId() + IDAble.SEPARATOR + "foobar");
        List<Course> courses3 = masterDataService.getCourses(list);
        list.add(course1.getId());
        list.add(course3.getId());
        List<Course> courses4 = masterDataService.getCourses(list);

        assertEquals(0, courses1.size());
        assertEquals(1, courses2.size());
        assertEquals(course2.getId(), courses2.get(0).getId());
        assertEquals(1, courses3.size());
        assertEquals(course2.getId(), courses3.get(0).getId());
        assertEquals(3, courses4.size());
        assertTrue(courses4.stream().anyMatch(c -> c.getId().equals(course1.getId())));
        assertTrue(courses4.stream().anyMatch(c -> c.getId().equals(course2.getId())));
        assertTrue(courses4.stream().anyMatch(c -> c.getId().equals(course3.getId())));
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetMenu() throws Exception {

    }
    
    @Ignore("Thin layer to repository")
    @Test
    public void testGetMenusByRestaurantId() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testAddToMenu() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testAddMenu() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testDeleteMenu() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpdateMenuModifiedTime() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertMenu() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAllMenuItems() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAllMenuItemsNotDeleted() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetMenuItems() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetItem() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testDeleteMenuItem() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertMenuItem() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertMenuItems() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetModifiers() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetModifierGroupsByRestaurant() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetModifierGroup() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertModifierGroup() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testDeleteModifierGroup() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetTaxRates() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testTaxRateExists() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetDefault() throws Exception {

    }

    @Test
    public void testGetRestaurantDefault() throws Exception {
        RestaurantDefault rdef = restaurant2.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.BIRTHDAY_TIMESPAN)).findFirst().orElse(null);
        restaurant2.getRestaurantDefaults().remove(rdef);
        restaurant2 = restaurantRepository.save(restaurant2);

        assert restaurant2.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.BIRTHDAY_TIMESPAN)).findFirst().orElse(null) == null;

        Default def = defaultsRepository.findByName(FixedDefaults.BIRTHDAY_TIMESPAN);
        int span = (Integer)def.getValue();

        RestaurantDefault restaurant2Def = masterDataService.getRestaurantDefault(restaurant2.getId(), FixedDefaults.BIRTHDAY_TIMESPAN);

        assertEquals(span, ((Integer)restaurant2Def.getValue()).intValue());

        def.setValue(span+1);
        restaurant2.getRestaurantDefaults().add(new RestaurantDefault(def));
        restaurantRepository.save(restaurant2);

        restaurant2Def = masterDataService.getRestaurantDefault(restaurant2.getId(), FixedDefaults.BIRTHDAY_TIMESPAN);
        assertEquals(span+1, ((Integer)restaurant2Def.getValue()).intValue());
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAdjustmentTypes() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAdjustmentType() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAdjustmentTypeByName() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetPrinters() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetPrintersByRestaurant() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertPrinter() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testPrinterExists() throws Exception {

    }

    @Test
    public void testGetServicesByRestaurant() throws Exception {
        restaurant1.getServices().clear();
        service1.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        restaurant2.getServices().clear();
        service3.setId(restaurant3.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant3.getServices().clear();
        restaurant3.getServices().add(service3);

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        List<Service> services1 = masterDataService.getServicesByRestaurant(restaurant1.getId());
        List<Service> services2 = masterDataService.getServicesByRestaurant(restaurant2.getId());
        List<Service> services3 = masterDataService.getServicesByRestaurant(restaurant3.getId());

        assertEquals(2,services1.size());
        assertTrue(services1.stream().anyMatch(s -> s.getId().equals(service1.getId())));
        assertTrue(services1.stream().anyMatch(s -> s.getId().equals(service2.getId())));

        assertEquals(0,services2.size());

        assertEquals(1,services3.size());
        assertEquals(service3.getId(), services3.get(0).getId());
    }

    @Test
    public void testGetService() throws Exception {
        restaurant1.getServices().clear();
        service1.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        restaurant2.getServices().clear();
        service3.setId(restaurant3.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        restaurant3.getServices().clear();
        restaurant3.getServices().add(service3);

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        Service s1 = masterDataService.getService(service1.getId());
        Service s2 = masterDataService.getService(service3.getId());
        assertEquals(service1.getId(), s1.getId());
        assertEquals(service3.getId(), s2.getId());

        service1.setId(restaurant3.getId() + IDAble.SEPARATOR +  ControllerUtil.nextRandom(5));

        Service s3 = masterDataService.getService(service1.getId());
        assertNull(s3);
    }

    @Test
    public void testGetTakeawayService() throws Exception {
        restaurant1.getServices().clear();
        service1.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setId(restaurant1.getId() + IDAble.SEPARATOR + ControllerUtil.nextRandom(5));
        service2.setSessionType(SessionType.TAKEAWAY);
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);

        restaurantRepository.save(restaurant1);

        Service s1 = masterDataService.getTakeawayService(restaurant1.getId());

        assertEquals(service2.getId(), s1.getId());
    }

    @Test
    public void testGetFloors() throws Exception {
        restaurant2.getFloors().add(floor1);
        restaurant2.getFloors().add(floor3);

        restaurantRepository.save(restaurant2);

        List<Floor> floors = masterDataService.getFloors(restaurant2.getId());

        assertEquals(2, floors.size());
        assertTrue(floors.stream().anyMatch(f -> f.getId().equals(floor1.getId())));
        assertTrue(floors.stream().anyMatch(f -> f.getId().equals(floor3.getId())));
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testSetLayoutSelected() throws Exception {

    }

    @Test
    public void testGetTables() throws Exception {
        restaurant2.getTables().add(table1);
        restaurant2.getTables().add(table3);
        restaurantRepository.save(restaurant2);

        List<String> tables = new ArrayList<>();

        tables.add("foobar");
        List<Table> t1 = masterDataService.getTables(restaurant2.getId(), tables);
        tables.add(table2.getId());
        tables.add(table3.getId());
        List<Table> t2 = masterDataService.getTables(restaurant2.getId(), tables);
        List<Table> t3 = masterDataService.getTables(restaurant1.getId(), tables);

        assertEquals(0, t1.size());
        assertEquals(2, t2.size());
        assertTrue(t2.stream().anyMatch(t -> t.getId().equals(table2.getId())));
        assertTrue(t2.stream().anyMatch(t -> t.getId().equals(table3.getId())));
        assertEquals(0, t3.size());

    }

    @Test
    public void testGetTables1() throws Exception {
        restaurant2.getTables().add(table1);
        restaurant2.getTables().add(table3);
        restaurant2 = restaurantRepository.save(restaurant2);

        List<Table> t1 = masterDataService.getTables(restaurant2.getId());

        assertEquals(3, t1.size());
        assertTrue(t1.stream().anyMatch(t -> t.getId().equals(table1.getId())));
        assertTrue(t1.stream().anyMatch(t -> t.getId().equals(table2.getId())));
        assertTrue(t1.stream().anyMatch(t -> t.getId().equals(table3.getId())));
    }

    @Test
    public void testGetMenuItemsByPlu() {
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setPlu("p1");
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem2.setPlu("p1");
        menuItem3.setRestaurantId(restaurant2.getId());
        menuItem3.setPlu("p1");
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        List<MenuItem> items = masterDataService.getMenuItemsByPlu(restaurant1.getId(), Collections.singletonList("p1"));
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(m -> m.getId().equals(menuItem1.getId())));
        assertTrue(items.stream().anyMatch(m -> m.getId().equals(menuItem2.getId())));

        menuItem2.setPlu("p2");
        menuItemRepository.save(menuItem2);
        items = masterDataService.getMenuItemsByPlu(restaurant1.getId(), Collections.singletonList("p1"));
        assertEquals(1, items.size());
    }

    @Ignore("Thin layer to repository")
    @Test
    public void testAddLayoutToFloor() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpdateTables() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testAddTable() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpsertTable() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testDeleteTable() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testUpdateLayout() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetTaxRatesByCountry() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetOpeningHours() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetAllPreferences() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetRestaurantImage() throws Exception {

    }

    @Ignore("Thin layer to repository")
    @Test
    public void testGetRestaurantImage1() throws Exception {

    }
}