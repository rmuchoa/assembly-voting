package com.cooperative.assembly.v1.voting.session.canvass;

import com.cooperative.assembly.v1.vote.Vote;
import com.cooperative.assembly.v1.vote.VoteChoice;
import com.cooperative.assembly.v1.vote.VoteService;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.isNoLongerOpen;
import static java.util.UUID.randomUUID;

@Log4j2
@Service
public class VotingSessionCanvassService {

    private VotingSessionCanvassRepository repository;
    private VoteService voteService;

    @Autowired
    public VotingSessionCanvassService(final VotingSessionCanvassRepository repository, final VoteService voteService) {
        this.repository = repository;
        this.voteService = voteService;
    }

    /**
     * Save voting session canvass with incremented totalizers
     *
     * @param canvass
     * @return
     */
    public VotingSessionCanvass saveCanvass(final VotingSessionCanvass canvass) {
        log.debug("Save session canvass to allow publish voting counting results");
        return repository.save(canvass);
    }

    /**
     * Apply vote choices on voting session canvass.
     * Should increment affirmative and negative totalizers.
     *
     * @param session
     */
    public VotingSessionCanvass reloadVotingSessionCanvass(final VotingSession session) {
        log.debug("Reload vote counting for session: ", session.getId());
        VotingSessionCanvass canvass = loadSessionCanvass(session);
        if (isNoLongerOpen(session)) {
            return canvass;
        }

        applyVoteChoices(canvass, session);
        return saveCanvass(canvass);
    }

    /**
     * Load all votes from session and apply on session canvass totalizers.
     *
     * @param session
     * @return
     */
    protected VotingSessionCanvass loadSessionCanvass(VotingSession session) {
        Optional<VotingSessionCanvass> canvass = repository.findBySessionId(session.getId());
        if (canvass.isPresent()) {
            return canvass.get();
        }

        return buildNewSessionCanvass(session);
    }

    /**
     * Load all votes from session and apply on session canvass totalizers.
     *
     * @param session
     */
    protected void applyVoteChoices(VotingSessionCanvass canvass, VotingSession session) {
        List<Vote> currentVotes = voteService.getSessionVotes(session);
        for (Vote vote : currentVotes) {
            applyVoteChoices(canvass, vote);
        }
    }

    /**
     * Apply vote choice on session canvass by affirmative or negative choice.
     *
     * @param canvass
     * @param vote
     */
    private void applyVoteChoices(final VotingSessionCanvass canvass, final Vote vote) {
        VoteChoice choice = vote.getChoice();
        if (choice.isAffirmative()) {
            canvass.incrementAffirmative();
        }

        if (choice.isNegative()) {
            canvass.incrementNegative();
        }
    }

    /**
     * Build empty session canvass for init voting session.
     *
     * @param session
     * @return
     */
    protected VotingSessionCanvass buildNewSessionCanvass(final VotingSession session) {
        String id = randomUUID().toString();
        VotingAgenda agenda = session.getAgenda();
        return new VotingSessionCanvass(id, agenda.getTitle(), 0, 0, 0, session);
    }

}
