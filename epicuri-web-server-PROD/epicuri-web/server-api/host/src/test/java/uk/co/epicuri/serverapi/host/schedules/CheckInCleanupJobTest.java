package uk.co.epicuri.serverapi.host.schedules;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionArchive;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static org.junit.Assert.*;

public class CheckInCleanupJobTest extends BaseIT {
    CheckInCleanupJob checkInCleanupJob = new CheckInCleanupJob();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        checkInRepository.deleteAll();
        sessionArchiveRepository.deleteAll();

        Whitebox.setInternalState(checkInCleanupJob, checkInRepository);
        Whitebox.setInternalState(checkInCleanupJob, sessionService);
        Whitebox.setInternalState(checkInCleanupJob, archiveDataService);
    }

    private CheckIn addCheckIn(long time, String sessionId, String partyId) {
        CheckIn checkIn = new CheckIn();
        checkIn.setTime(time);
        checkIn.setSessionId(sessionId);
        checkIn.setPartyId(partyId);
        return checkInRepository.save(checkIn);
    }

    @Test
    public void cleanupOldCheckIns_DeleteOrphaned() {
        CheckIn c1 = addCheckIn(System.currentTimeMillis() - (1000*60*60*24), null, null);
        CheckIn c2 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), null, null);
        CheckIn c3 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), null, null);
        checkInCleanupJob.cleanupOldCheckIns();

        assertNull(checkInRepository.findOne(c1.getId()));
        assertNull(checkInRepository.findOne(c2.getId()));
        assertEquals(c3, checkInRepository.findOne(c3.getId()));
    }

    @Test
    public void cleanupOldCheckIns_LiveSessionCheckInNotDeleted() {
        CheckIn c1 = addCheckIn(System.currentTimeMillis() - (1000*60*60*24), null, null);
        CheckIn c2 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), null, null);
        CheckIn c3 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), null, null);
        CheckIn c4 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), session1.getId(), party1.getId());
        CheckIn c5 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), session1.getId(), party1.getId());
        checkInCleanupJob.cleanupOldCheckIns();

        assertEquals(c3, checkInRepository.findOne(c3.getId()));
        assertEquals(c4, checkInRepository.findOne(c4.getId()));
        assertEquals(c5, checkInRepository.findOne(c5.getId()));
    }

    @Test
    public void cleanupOldCheckIns_ClosedSessionCheckInDeleted() {
        CheckIn c1 = addCheckIn(System.currentTimeMillis() - (1000*60*60*24), null, null);
        CheckIn c2 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), null, null);
        CheckIn c3 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), null, null);
        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);
        CheckIn c4 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), session1.getId(), party1.getId());
        CheckIn c5 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), session1.getId(), party1.getId());
        checkInCleanupJob.cleanupOldCheckIns();

        assertEquals(c3, checkInRepository.findOne(c3.getId()));
        assertEquals(c4, checkInRepository.findOne(c4.getId()));
        assertNull(checkInRepository.findOne(c5.getId()));
    }

    @Test
    public void cleanupOldCheckIns_ArchivedSessionCheckInDeleted() {
        CheckIn c1 = addCheckIn(System.currentTimeMillis() - (1000*60*60*24), null, null);
        CheckIn c2 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), null, null);
        CheckIn c3 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), null, null);
        session1.setClosedTime(System.currentTimeMillis());
        SessionArchive sessionArchive = new SessionArchive(session1);
        sessionRepository.delete(session1);
        sessionArchiveRepository.save(sessionArchive);
        CheckIn c4 = addCheckIn(System.currentTimeMillis() - (1000*60*60*5), session2.getId(), party2.getId());
        CheckIn c5 = addCheckIn(System.currentTimeMillis() - (1000*60*60*7), session1.getId(), party1.getId());
        checkInCleanupJob.cleanupOldCheckIns();

        assertEquals(c3, checkInRepository.findOne(c3.getId()));
        assertEquals(c4, checkInRepository.findOne(c4.getId()));
        assertNull(checkInRepository.findOne(c5.getId()));

        sessionArchive = sessionArchiveRepository.findOne(sessionArchive.getId());
        assertEquals(1, sessionArchive.getCheckIns().size());
        assertEquals(c5, sessionArchive.getCheckIns().get(0));
    }
}