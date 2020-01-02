package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class VoteCountingService {

    private VotingSessionService votingSessionService;
    private VotingSessionCanvassService votingSessionCanvassService;

    @Autowired
    public VoteCountingService(final VotingSessionService votingSessionService, final VotingSessionCanvassService votingSessionCanvassService) {
        this.votingSessionService = votingSessionService;
        this.votingSessionCanvassService = votingSessionCanvassService;
    }

    /**
     * Load data from voting session by agendaId to publish results from voting.
     *
     * @param agendaId
     * @return
     */
    public VoteCounting getVoteCounting(final String agendaId) {
        log.debug("Finding voting session by agendaId: ", agendaId);
        VotingSession session = votingSessionService.loadVoteSessionByAgenda(agendaId);
        VotingSessionCanvass canvass = votingSessionCanvassService.reloadVotingSessionCanvass(session);
        VotingAgenda agenda = session.getAgenda();

        return new VoteCounting(agenda.getTitle(), canvass.getTotalVotes(), canvass.getAffirmativeVotes(),
                canvass.getNegativeVotes(), session.getOpeningTime(), session.getClosingTime(), session.getStatus());
    }

}
