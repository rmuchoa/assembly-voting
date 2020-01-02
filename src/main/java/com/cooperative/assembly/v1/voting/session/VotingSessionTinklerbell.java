package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
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
    private VotingSessionCanvassService votingSessionCanvassService;

    public VotingSessionTinklerbell(final VotingSessionService votingSessionService, final VotingSessionCanvassService votingSessionCanvassService) {
        this.votingSessionService = votingSessionService;
        this.votingSessionCanvassService = votingSessionCanvassService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void ringTheSessionBell() {
        List<VotingSession> openedSessions = votingSessionService.loadMissClosedSessions();

        if (!isEmpty(openedSessions)) {
            log.debug("Found opened sessions to close.");
            for (VotingSession session : openedSessions) {
                closeMissClosedSession(session);
            }
        }
    }

    /**
     * Close opened session that should to be closed already.
     *
     * @param session
     */
    private void closeMissClosedSession(final VotingSession session) {
        votingSessionCanvassService.reloadVotingSessionCanvass(session);

        session.setStatus(CLOSED);
        log.debug("Closing session: ", session.getId());
        votingSessionService.saveSession(session);
    }

}
