package uk.co.epicuri.serverapi.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BatchServiceTest extends BaseIT{

    @Autowired
    private BatchService batchService;

    @Test
    public void getBatches() throws Exception {
    }

    @Test
    public void markAsPrinted() throws Exception {
    }

    @Test
    public void getBatchesToPrintBySessionId() throws Exception {
    }

    @Test
    public void testFilterValidSessions() throws Exception {
        session1.setSessionType(SessionType.SEATED);
        session1.setClosedTime(null);
        session2.setSessionType(SessionType.ADHOC);
        session3.setSessionType(SessionType.TAKEAWAY);

        List<Session> sessions = Arrays.asList(session1, session2, session3);
        assertEquals(3, batchService.filterValidSessions(sessions, restaurant1.getId()).getA().size());

        session1.setClosedTime(System.currentTimeMillis());
        assertEquals(2, batchService.filterValidSessions(sessions, restaurant1.getId()).getA().size());

        session3.setSessionType(SessionType.NONE);
        assertEquals(1, batchService.filterValidSessions(sessions, restaurant1.getId()).getA().size());

        session1.setClosedTime(null);
        session1.setSessionType(SessionType.TAB);
        assertEquals(2, batchService.filterValidSessions(sessions, restaurant1.getId()).getA().size());
    }

    @Test
    public void testFilterBatches() throws Exception {
        long currentTime = System.currentTimeMillis();
        batch1.setPrintedTime(currentTime);
        batch2.setPrintedTime(currentTime);
        batch3.setPrintedTime(currentTime);

        List<Batch> batches = Arrays.asList(batch1, batch2, batch3);

        List<Batch> filtered = batchService.filterBatches(batches, currentTime, 1000);
        assertEquals(0, filtered.size());

        batch1.setPrintedTime(null);
        batch2.setPrintedTime(null);

        filtered = batchService.filterBatches(batches, currentTime, 1000);
        assertEquals(2, filtered.size());

        batch1.getSpoolTime().add(currentTime);

        filtered = batchService.filterBatches(batches, currentTime, 1000);
        assertEquals(1, filtered.size());

        filtered = batchService.filterBatches(batches, currentTime+1001, 1000);
        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterBatchesDeleted() throws Exception {
        long currentTime = System.currentTimeMillis();
        batch1.setPrintedTime(null);
        batch2.setPrintedTime(null);
        batch3.setPrintedTime(currentTime);

        List<Batch> batches = Arrays.asList(batch1, batch2, batch3);

        List<Batch> filtered = batchService.filterBatches(batches, currentTime, 1000);
        assertEquals(2, filtered.size());

        batch2.setDeleted(currentTime);
        batchRepository.save(batch2);

        filtered = batchService.filterBatches(batches, currentTime, 1000);
        assertEquals(1, filtered.size());
    }

    @Test
    public void testGetBatchesWherePrintingRequired() throws Exception {
        printer1.setIp(null);
        printer2.setIp("192.168.0.1");

        printerRepository.save(printer1);
        printerRepository.save(printer2);

        batch1.setPrinterId(printer1.getId());
        batch2.setPrinterId(printer1.getId());
        batch3.setPrinterId(printer1.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        List<Batch> batches = Arrays.asList(batch1, batch2, batch3);

        assertEquals(0, batchService.getBatchesWherePrintingRequired(batches, true).size());

        batch2.setPrinterId(printer2.getId());
        batchRepository.save(batch2);
        assertEquals(1, batchService.getBatchesWherePrintingRequired(batches, true).size());

        batch1.setPrinterId(printer2.getId());
        batch3.setPrinterId(printer2.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch3);
        assertEquals(3, batchService.getBatchesWherePrintingRequired(batches, true).size());

        batch1.setAwaitingImmediatePrint(true);
        batchRepository.save(batch1);
        assertEquals(3, batchService.getBatchesWherePrintingRequired(batches, true).size());
        assertEquals(2, batchService.getBatchesWherePrintingRequired(batches, false).size());
    }

    @Test
    public void testGetBookingsById() throws Exception {
        session1.setOriginalBookingId(booking1.getId());
        session3.setOriginalBookingId(booking3.getId());
        session2.setOriginalBookingId(null);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<Session> sessionList = Arrays.asList(session1, session2, session3);
        Map<String,Booking> bookingMap = batchService.getBookingsById(sessionList);

        assertEquals(2, bookingMap.size());
        assertEquals(booking1.getId(), bookingMap.get(booking1.getId()).getId());
        assertEquals(booking3.getId(), bookingMap.get(booking3.getId()).getId());
    }
}