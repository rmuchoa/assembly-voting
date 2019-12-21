package com.cooperative.assembly.vote;

import com.cooperative.assembly.user.User;
import com.cooperative.assembly.user.UserService;
import com.cooperative.assembly.user.VotingAbility;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.voting.session.VotingSessionService;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static com.cooperative.assembly.user.VotingAbility.UNABLE_TO_VOTE;
import static com.cooperative.assembly.vote.VoteChoice.YES;
import static com.cooperative.assembly.vote.VoteChoice.NO;
import static org.springframework.util.CollectionUtils.isEmpty;

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
     * Save vote for user that is able to vote and session that is still open.
     *
     * @param userId
     * @param agendaId
     * @param choice
     * @return
     */
    public Vote chooseVote(final String userId, final String agendaId, final VoteChoice choice) {
        Vote vote = saveChoice(userId, agendaId, choice);

        applyVoteOnAgendaSession(agendaId, vote);

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
            throw new ValidationException("vote.already.exists", "userId|agendaId", format("%s|%s", userId, agendaId));
        }

        User user = loadUser(userId);
        VotingSession session = loadVotingSession(agendaId);

        String id = randomUUID().toString();
        return new Vote(id, user.getId(), session.getAgenda());
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
     * Load agenda session and apply vote on session canvass by affirmative ou negative choice.
     * Updade session canvass with current totalized canvass.
     *
     * @param agendaId
     * @param vote
     */
    protected void applyVoteOnAgendaSession(final String agendaId, final Vote vote) {
        VotingSession session = loadVotingSession(agendaId);

        VotingSessionCanvass canvass = session.getCanvass();
        applyChoice(canvass, vote);

        votingSessionCanvassService.saveCanvass(canvass);
    }

    /**
     * Apply vote choice on session canvass by affirmative or negative choice.
     *
     * @param canvass
     * @param vote
     */
    private void applyChoice(final VotingSessionCanvass canvass, final Vote vote) {
        if (isAffirmativeChoice(vote.getChoice())) {
            canvass.incrementAffirmative();
        }

        if (isNegativeChoice(vote.getChoice())) {
            canvass.incrementNegative();
        }
    }

    /**
     * check if made choice is affirmative
     *
     * @param choice
     * @return
     */
    private Boolean isAffirmativeChoice(final VoteChoice choice) {
        return YES.equals(choice);
    }

    /**
     * check if made choice is negative
     *
     * @param choice
     * @return
     */
    private Boolean isNegativeChoice(final VoteChoice choice) {
        return NO.equals(choice);
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
