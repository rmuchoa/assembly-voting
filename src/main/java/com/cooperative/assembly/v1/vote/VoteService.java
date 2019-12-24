package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.v1.user.User;
import com.cooperative.assembly.v1.user.UserService;
import com.cooperative.assembly.v1.user.VotingAbility;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.springframework.util.CollectionUtils.isEmpty;

@Log4j2
@Service
public class VoteService {

    private VoteRepository repository;
    private UserService userService;
    private VotingSessionService votingSessionService;
    private VotingSessionCanvassService votingSessionCanvassService;

    @Autowired
    public VoteService(final VoteRepository repository, final UserService userService, final VotingSessionService votingSessionService, final VotingSessionCanvassService votingSessionCanvassService) {
        this.repository = repository;
        this.userService = userService;
        this.votingSessionService = votingSessionService;
        this.votingSessionCanvassService = votingSessionCanvassService;
    }

    /**
     * Apply vote choice for user on specific voting agenda.
     * Save vote for user that is able to vote and session that is still opened.
     *
     * @param userId
     * @param agendaId
     * @param choice
     * @return
     */
    public Vote chooseVote(final String userId, final String agendaId, final VoteChoice choice) {
        Vote vote = saveChoice(userId, agendaId, choice);

        applyVoteOnSessionCanvass(agendaId, vote);

        return vote;
    }

    /**
     * Build and save valid vote by agenda with user choice.
     *
     * @param userId
     * @param agendaId
     * @param choice
     * @return
     */
    protected Vote saveChoice(final String userId, final String agendaId, final VoteChoice choice) {
        Vote vote = validateAndBuildVote(userId, agendaId);
        vote.setChoice(choice);

        log.debug("Saving vote made by user");
        return repository.save(vote);
    }

    /**
     * Validate and build vote object by agenda to save user choice.
     *
     * @param userId
     * @param agendaId
     * @return
     */
    protected Vote validateAndBuildVote(final String userId, final String agendaId) {
        if (hasUserAlreadyVotedOnAgenda(userId, agendaId)) {
            log.error("Found previous vote on this agenda by the same user");
            throw new ValidationException("vote.already.exists", "userId|agendaId", format("%s|%s", userId, agendaId));
        }

        User user = loadUser(userId);
        VotingSession session = loadVotingSession(agendaId);

        String id = randomUUID().toString();
        return new Vote(id, user.getId(), session.getAgenda(), session);
    }

    /**
     * List all votes registered for specific userId and agendaId relationship.
     *
     * @param userId
     * @param agendaId
     * @return
     */
    private Boolean hasUserAlreadyVotedOnAgenda(final String userId, final String agendaId) {
        List<Vote> votes = repository.findByUserIdAndAgendaId(userId, agendaId);
        return !isEmpty(votes);
    }

    /**
     * Load user by id for apply vote.
     * Check if user is able to vote on this Cooperative assemblies.
     * Throw ValidationException when user in unable to vote.
     *
     * @param userId
     * @return
     */
    private User loadUser(final String userId) {
        User user = userService.loadUser(userId);
        if (user != null && isUnableToVote(user)) {
            log.error("Found user is not able to vote on this Cooperative", user);
            throw new ValidationException("user.unable.to.vote", "userId", userId);
        }

        return user;
    }

    /**
     * Check if user is able to vote on Cooperative comparing user ability status.
     *
     * @param user
     * @return
     */
    private Boolean isUnableToVote(final User user) {
        VotingAbility ability = user.getAbility();
        return ability.isUserUnableToVote();
    }

    /**
     * Load agenda session and apply vote on session canvass by affirmative ou negative choice.
     * Updade session canvass with current totalized canvass.
     *
     * @param agendaId
     * @param vote
     */
    protected void applyVoteOnSessionCanvass(final String agendaId, final Vote vote) {
        VotingSession session = vote.getSession();
        VotingSessionCanvass canvass = session.getCanvass();
        applyChoice(canvass, vote);

        log.debug("Save totalized voting session canvass", canvass);
        votingSessionCanvassService.saveCanvass(canvass);
    }

    /**
     * Apply vote choice on session canvass by affirmative or negative choice.
     *
     * @param canvass
     * @param vote
     */
    private void applyChoice(final VotingSessionCanvass canvass, final Vote vote) {
        VoteChoice choice = vote.getChoice();
        if (choice.isAffirmative()) {
            canvass.incrementAffirmative();
        }

        if (choice.isNegative()) {
            canvass.incrementNegative();
        }
    }

    /**
     * Load voting session by agendaId for apply vote.
     * Check if voting session is still opened before vote.
     * Throw ValidationException when voting session is not longer open.
     *
     * @param agendaId
     * @return
     */
    private VotingSession loadVotingSession(final String agendaId) {
        VotingSession session = votingSessionService.loadVoteSession(agendaId);
        if (session != null && session.isNoLongerOpen()) {
            log.debug("Found agenda session is closed for voting now", session);
            throw new ValidationException("voting.session.no.longer.open", "agendaId", agendaId);
        }

        return session;
    }

}
