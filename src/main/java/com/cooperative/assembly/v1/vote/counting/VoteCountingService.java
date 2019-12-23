package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class VoteCountingService {

    private VotingSessionService votingSessionService;

    @Autowired
    public VoteCountingService(final VotingSessionService votingSessionService) {
        this.votingSessionService = votingSessionService;
    }

    /**
     * Load data from voting session by agendaId to publish results from voting.
     *
     * @param agendaId
     * @return
     */
    public VoteCounting getVoteCounting(final String agendaId) {
        log.debug("Finding voting session by agendaId: ", agendaId);
        VotingSession session = votingSessionService.loadVoteSession(agendaId);
        VotingSessionCanvass canvass = session.getCanvass();
        VotingAgenda agenda = session.getAgenda();

        return new VoteCounting(agenda.getTitle(), canvass.getTotalVotes(), canvass.getAffirmativeVotes(),
                canvass.getNegativeVotes(), session.getOpeningTime(), session.getClosingTime());
    }

}
