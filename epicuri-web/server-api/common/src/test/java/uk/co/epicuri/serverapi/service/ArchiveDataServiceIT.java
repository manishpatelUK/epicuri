package uk.co.epicuri.serverapi.service;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by manish
 */
public class ArchiveDataServiceIT extends BaseIT {

    @Test
    public void testGetLastCashUp() throws Exception {

        for(int i = 0 ; i < 20; i++) {
            if(i == 10) {
                cashUp1.setStartTime(System.currentTimeMillis()-10);
                cashUp1.setEndTime(System.currentTimeMillis());
                cashUp1.setRestaurantId(restaurant2.getId());
            } else {
                CashUp cashUp = new CashUp();
                cashUp.setRestaurantId(restaurant2.getId());
                cashUp.setStartTime(i + 1);
                cashUp.setEndTime(i + 20);
                cashUpRepository.save(cashUp);
            }
        }

        cashUpRepository.save(cashUp1);

        CashUp last = archiveDataService.getLastCashUp(restaurant2.getId());

        assertEquals(cashUp1.getId(), last.getId());
    }

    @Test
    public void testGetLastCashUps() throws Exception {
        List<CashUp> toSave = new ArrayList<>();
        for(int i = 0 ; i < 20; i++) {
            CashUp cashUp = new CashUp();
            cashUp.setRestaurantId(restaurant2.getId());
            cashUp.setStartTime(i*10);
            cashUp.setEndTime((i+1)*10);
            toSave.add(cashUp);
        }

        Collections.shuffle(toSave);
        cashUpRepository.save(toSave);

        List<CashUp> list = archiveDataService.getLastCashUps(restaurant2.getId(), 150);
        assertEquals(6,list.size());
    }

    @Test
    public void testAddCashUp() throws Exception {
        CashUp cashUp = new CashUp();
        cashUp.setRestaurantId(restaurant2.getId());
        cashUp.setStartTime(10);
        cashUp.setEndTime(20);

        int size = cashUpRepository.findAll().size();
        archiveDataService.addCashUp(cashUp);

        assertTrue(cashUpRepository.findAll().size() == size+1);
    }
}