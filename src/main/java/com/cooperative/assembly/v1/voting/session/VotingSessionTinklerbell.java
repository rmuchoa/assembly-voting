package com.cooperative.assembly.v1.voting.session;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static org.springframework.util.CollectionUtils.isEmpty;

@Log4j2
@Service
public class VotingSessionTinklerbell {

    private VotingSessionService votingSessionService;

    public VotingSessionTinklerbell(final VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void ringTheSessionBell() {
        List<VotingSession> openedSessions = votingSessionService.loadMissClosedSessions();

        if (!isEmpty(openedSessions)) {
            log.debug("Found opened sessions to close.");
            closeMissClosedSessions(openedSessions);
        }
    }

    private void closeMissClosedSessions(final List<VotingSession> sessions) {
        for (VotingSession session : sessions) {
            updateStatusSessionCanvass(session);
        }
    }

    private void updateStatusSessionCanvass(final VotingSession session) {
        session.setStatus(CLOSED);
        log.debug("Closing session: ", session.getId());
        votingSessionService.saveSession(session);
    }

}
