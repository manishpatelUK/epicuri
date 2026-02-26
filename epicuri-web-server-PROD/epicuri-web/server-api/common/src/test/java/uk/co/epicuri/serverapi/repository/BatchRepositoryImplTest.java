package uk.co.epicuri.serverapi.repository;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class BatchRepositoryImplTest extends BaseIT {

    @Test
    public void testPushSpoolTime() throws Exception {
        assert batch1.getSpoolTime().size() == 0;
        assert batch2.getSpoolTime().size() == 0;

        List<String> ids = new ArrayList<>();
        ids.add(batch1.getId());
        ids.add(batch2.getId());

        batchRepository.pushSpoolTime(ids,10);
        batchRepository.pushSpoolTime(ids,11);
        ids.remove(1);
        batchRepository.pushSpoolTime(ids,12);

        Batch test1 = batchRepository.findOne(batch1.getId());
        Batch test2 = batchRepository.findOne(batch2.getId());

        assertTrue(test1.getSpoolTime().size() == 3);
        assertTrue(test1.getSpoolTime().get(0) == 10);
        assertTrue(test1.getSpoolTime().get(1) == 11);
        assertTrue(test1.getSpoolTime().get(2) == 12);
        assertTrue(test2.getSpoolTime().size() == 2);
        assertTrue(test2.getSpoolTime().get(0) == 10);
        assertTrue(test2.getSpoolTime().get(1) == 11);

        Batch negative = batchRepository.findOne(batch3.getId());
        assertTrue(negative.getSpoolTime().size() == 0);
    }

    @Test
    public void testSetPrintedTime() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(batch1.getId());
        ids.add(batch2.getId());

        batchRepository.setPrintedTime(ids,10);
        List<Batch> all = Lists.newArrayList(batchRepository.findAll(ids));

        assertTrue(all.size() == 2);
        assertTrue(all.get(0).getId().equals(batch1.getId()));
        assertTrue(all.get(0).getPrintedTime() == 10);
        assertTrue(all.get(1).getId().equals(batch2.getId()));
        assertTrue(all.get(1).getPrintedTime() == 10);

        Batch negative = batchRepository.findOne(batch3.getId());
        assertTrue(negative.getPrintedTime() == null);
    }
}