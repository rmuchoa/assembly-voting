package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.v1.user.User;
import com.cooperative.assembly.v1.user.UserService;
import com.cooperative.assembly.v1.user.VotingAbility;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
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

    @Autowired
    public VoteService(final VoteRepository repository, final UserService userService, final VotingSessionService votingSessionService) {
        this.repository = repository;
        this.userService = userService;
        this.votingSessionService = votingSessionService;
    }

    /**
     * Apply vote choice for user on specific voting agenda.
     * Save vote for user that is able to vote and session that is still opened.
     *
     * @param userId
     * @param sessionId
     * @param choice
     * @return
     */
    public Vote chooseVote(final String userId, final String sessionId, final VoteChoice choice) {
        Vote vote = validateAndBuildVote(userId, sessionId);
        vote.setChoice(choice);

        log.debug("Saving vote made by user");
        return repository.save(vote);
    }

    /**
     * Validate and build vote object by agenda to save user choice.
     *
     * @param userId
     * @param sessionId
     * @return
     */
    protected Vote validateAndBuildVote(final String userId, final String sessionId) {
        if (hasUserAlreadyVotedOnSession(userId, sessionId)) {
            log.error("Found previous vote on this session by the same user");
            throw new ValidationException("vote.already.exists", "userId|sessionId", format("%s|%s", userId, sessionId));
        }

        User user = loadUser(userId);
        VotingSession session = loadVotingSession(sessionId);

        String id = randomUUID().toString();
        return new Vote(id, user.getId(), session);
    }

    /**
     * List all votes registered for specific userId and agendaId relationship.
     *
     * @param userId
     * @param sessionId
     * @return
     */
    private Boolean hasUserAlreadyVotedOnSession(final String userId, final String sessionId) {
        List<Vote> votes = repository.findByUserIdAndSessionId(userId, sessionId);
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
     * @param sessionId
     * @return
     */
    private VotingSession loadVotingSession(final String sessionId) {
        VotingSession session = votingSessionService.loadVoteSession(sessionId);
        if (session != null && session.isNoLongerOpen()) {
            log.debug("Found session is closed for voting now", session);
            throw new ValidationException("voting.session.no.longer.open", "sessionId", sessionId);
        }

        return session;
    }

    /**
     * List all votes from a voting session.
     *
     * @param session
     * @return
     */
    public List<Vote> getSessionVotes(VotingSession session) {
        return repository.findBySessionId(session.getId());
    }

}
