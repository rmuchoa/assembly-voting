package com.cooperative.assembly.vote;

import com.cooperative.assembly.user.User;
import com.cooperative.assembly.user.UserService;
import com.cooperative.assembly.user.VotingAbility;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.VotingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static com.cooperative.assembly.user.VotingAbility.UNABLE_TO_VOTE;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class VoteService {

    private VotingSessionService votingSessionService;
    private UserService userService;
    private VoteRepository repository;

    @Autowired
    public VoteService(final VotingSessionService votingSessionService, final UserService userService, final VoteRepository repository) {
        this.votingSessionService = votingSessionService;
        this.userService = userService;
        this.repository = repository;
    }

    /**
     * Apply vote choice for user on specific voting agenda.
     * Save vote for user that is able to vote and session that is still open.
     *
     * @param userId
     * @param agendaId
     * @param choice
     * @return
     */
    public Vote choiceVote(final String userId, final String agendaId, final VoteChoice choice) {
        if (hasUserAlreadyVotedAgenda(userId, agendaId)) {
            throw new ValidationException("vote.already.exists", "userId|agendaId", format("%s|%s", userId, agendaId));
        }

        User user = loadUser(userId);
        VotingSession session = loadVotingSession(agendaId);

        String id = randomUUID().toString();
        Vote vote = new Vote(id, user.getId(), session.getAgenda(), choice);

        return repository.save(vote);
    }

    /**
     * List all votes registered for specific userId and agendaId relationship.
     *
     * @param userId
     * @param agendaId
     * @return
     */
    private Boolean hasUserAlreadyVotedAgenda(final String userId, final String agendaId) {
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
        return UNABLE_TO_VOTE.equals(ability);
    }

    /**
     * Load voting session by agendaId for apply vote.
     * Check if voting session is still open before vote.
     * Throw ValidationException when voting session is not longer open.
     *
     * @param agendaId
     * @return
     */
    private VotingSession loadVotingSession(final String agendaId) {
        VotingSession session = votingSessionService.loadVoteSession(agendaId);
        if (session != null && isNoLongerOpen(session)) {
            throw new ValidationException("voting.session.no.longer.open", "agendaId", agendaId);
        }

        return session;
    }

    /**
     * Check if closing time is past before right now to infer this voting session is still open
     *
     * @param session
     * @return
     */
    private Boolean isNoLongerOpen(final VotingSession session) {
        LocalDateTime closingTimeSession = session.getClosingTime();
        return now().isAfter(closingTimeSession);
    }

}
