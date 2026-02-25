package uk.co.epicuri.serverapi.host.schedules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionArchive;
import uk.co.epicuri.serverapi.repository.CheckInRepository;
import uk.co.epicuri.serverapi.repository.SessionRepository;
import uk.co.epicuri.serverapi.service.ArchiveDataService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.SessionService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CheckInCleanupJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInCleanupJob.class);

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Scheduled(initialDelay = 60000L, fixedRate = 1800000L)
    public void cleanupOldCheckIns() {
        long limit = System.currentTimeMillis() - (1000 * 60 * 60 * 6);
        List<CheckIn> checkIns = checkInRepository.findByTimeBefore(limit);
        List<CheckIn> orphans = checkIns.stream().filter(c -> c.getSessionId() == null & c.getPartyId() == null).collect(Collectors.toList());

        //delete orphans
        LOGGER.trace("Delete {} orphaned checkins", orphans.size());
        checkInRepository.delete(orphans);
        checkIns.removeAll(orphans);

        //checkIns with a session - if the session is closed or archived, make sure check ins go away
        List<CheckIn> sessionedCheckIns = checkIns.stream().filter(c -> c.getSessionId() != null).collect(Collectors.toList());
        Map<String,List<CheckIn>> sessionIdToCheckIn = sessionedCheckIns.stream().collect(Collectors.groupingBy(CheckIn::getSessionId));
        Map<String,SessionArchive> sessionIdToArchive = archiveDataService.getSessionArchives(sessionIdToCheckIn.keySet()).stream().collect(Collectors.toMap(SessionArchive::getSessionId, Function.identity()));
        List<Session> sessions = sessionService.getSessions(sessionIdToCheckIn.keySet());

        //sessions that are not yet cashed up but are closed
        for(Session session : sessions) {
            List<CheckIn> closedSessionCheckins = sessionIdToCheckIn.get(session.getId());
            if(session.getClosedTime() != null && closedSessionCheckins.size() > 0) {
                archiveDataService.archiveCheckIns(session, closedSessionCheckins);
                checkIns.removeAll(closedSessionCheckins);
                checkInRepository.delete(closedSessionCheckins);
            }
        }

        //sessions that are cashed up and only in archive
        Map<String,Session> liveSessions = sessions.stream().collect(Collectors.toMap(Session::getId, Function.identity()));
        for(String sessionId : sessionIdToArchive.keySet()) {
            if(liveSessions.containsKey(sessionId)) {
                continue;
            }

            archiveDataService.archiveCheckIns(sessionIdToArchive.get(sessionId), sessionIdToCheckIn.get(sessionId));
            checkIns.removeAll(sessionIdToCheckIn.get(sessionId));
            checkInRepository.delete(sessionIdToCheckIn.get(sessionId));
        }
    }
}
