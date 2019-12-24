package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
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
            closeMissClosedSessions(openedSessions);
        }
    }

    private void closeMissClosedSessions(final List<VotingSession> sessions) {
        for (VotingSession session : sessions) {
            updateStatusSessionCanvass(session);
        }
    }

    private void updateStatusSessionCanvass(final VotingSession session) {
        VotingSessionCanvass canvass = session.getCanvass();
        canvass.setStatus(CLOSED);
        log.debug("Closing session: ", session.getId());
        votingSessionCanvassService.saveCanvass(canvass);
    }

}
