package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;

import static org.junit.Assert.*;

/**
 * Created by manish
 */

public class AdjustmentTypeRepositoryTest extends BaseIT {

    @Test
    public void testFindByName() throws Exception {
        AdjustmentType a1 = adjustmentTypeRepository.findByName(adjustmentType1.getName());
        AdjustmentType a2 = adjustmentTypeRepository.findByName(adjustmentType2.getName());

        assertEquals(a1.getId(), adjustmentType1.getId());
        assertEquals(a2.getId(), adjustmentType2.getId());
    }
}