package com.cooperative.assembly.vote.counting;

import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.VotingSessionService;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        VotingSession session = votingSessionService.loadVoteSession(agendaId);
        VotingSessionCanvass canvass = session.getCanvass();
        VotingAgenda agenda = session.getAgenda();

        return new VoteCounting(agenda.getTitle(), session.getOpeningTime(), session.getClosingTime(),
                canvass.getTotalVotes(), canvass.getAffirmativeVotes(), canvass.getNegativeVotes());
    }

}
