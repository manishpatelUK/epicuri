package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CashUpRepositoryTest extends BaseIT {

    @Test
    public void testFindLastCashUp() throws Exception {
        assert cashUp1.getEndTime() < cashUp2.getEndTime();

        PageRequest request = new PageRequest(0,1,new Sort(Sort.Direction.DESC, "endTime"));
        Page<CashUp> page = cashUpRepository.findLastCashUp(restaurant1.getId(),request);

        assertEquals(1,page.getSize());
        assertEquals(1, page.getContent().size());
        assertEquals(cashUp2.getId(), page.getContent().get(0).getId());

        request = new PageRequest(0,1,new Sort(Sort.Direction.DESC, "endTime"));
        page = cashUpRepository.findLastCashUp("foobar",request);
        assertEquals(1,page.getSize());
        assertEquals(0,page.getContent().size());
    }

    @Test
    public void testFindByRestaurantIdAndEndTimeGreaterThanEqual() throws Exception {
        List<CashUp> list = cashUpRepository.findByRestaurantIdAndEndTimeGreaterThanEqual(restaurant1.getId(),10);
        assertEquals(3, list.size());

        list = cashUpRepository.findByRestaurantIdAndEndTimeGreaterThanEqual("foobar",10);
        assertEquals(0, list.size());

        list = cashUpRepository.findByRestaurantIdAndEndTimeGreaterThanEqual(restaurant1.getId(),20);
        assertEquals(1, list.size());

        list = cashUpRepository.findByRestaurantIdAndEndTimeGreaterThanEqual(restaurant1.getId(),30);
        assertEquals(0, list.size());
    }
}