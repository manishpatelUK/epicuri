package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class BatchRepositoryTest extends BaseIT {

    @Test
    public void testFindBySessionIdIn() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add("random");
        ids.add(null);
        ids.add(batch1.getSessionId());
        ids.add(batch2.getSessionId());
        ids.add(batch2.getSessionId()); //repeat

        List<Batch> batches = batchRepository.findBySessionIdIn(ids);
        assertTrue(batches.size() == 3);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch1.getId())).findFirst().orElse(null) != null);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch2.getId())).findFirst().orElse(null) != null);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch3.getId())).findFirst().orElse(null) != null);

        ids = ids.subList(0,3);
        batches = batchRepository.findBySessionIdIn(ids);
        assertTrue(batches.size() == 1);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch1.getId())).findFirst().orElse(null) != null);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch2.getId())).findFirst().orElse(null) == null);
        assertTrue(batches.stream().filter(b->b.getId().equals(batch3.getId())).findFirst().orElse(null) == null);
    }

    @Test
    public void testFindBySessionIdInAndIntendedPrintTimeLessThanEqual() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add("random");
        ids.add(null);
        ids.add(batch1.getSessionId());
        ids.add(batch2.getSessionId());

        assertTrue(batch2.getIntendedPrintTime() > batch1.getIntendedPrintTime());
        List<Batch> batches = batchRepository.findBySessionIdInAndIntendedPrintTimeLessThanEqual(ids,batch1.getIntendedPrintTime());

        assertTrue(batches.size() == 1);
        assertTrue(batches.get(0).getId().equals(batch1.getId()));
    }

    @Test
    public void testFindBySessionId() throws Exception {
        List<Batch> batches = batchRepository.findBySessionId(batch1.getSessionId());
        assertTrue(batches.size() == 1);
        assertTrue(batches.get(0).getId().equals(batch1.getId()));
    }
}