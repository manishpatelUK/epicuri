package uk.co.epicuri.serverapi.repository;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class OrderRepositoryImplTest extends BaseIT {

    @Test
    public void testUpdateVoid() throws Exception {
        assert !order1.isVoided();
        assert !order2.isVoided();

        List<String> ids = new ArrayList<>();
        ids.add(order1.getId());
        ids.add(order2.getId());

        orderRepository.updateVoid(ids);

        assertTrue(orderRepository.findOne(order1.getId()).isVoided());
        assertTrue(orderRepository.findOne(order2.getId()).isVoided());
        assertFalse(orderRepository.findOne(order3.getId()).isVoided());
    }
}