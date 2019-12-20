package com.cooperative.assembly.vote;

import com.cooperative.assembly.user.User;
import com.cooperative.assembly.user.UserService;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.VotingSessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;
import static com.cooperative.assembly.user.VotingAbility.ABLE_TO_VOTE;
import static com.cooperative.assembly.user.VotingAbility.UNABLE_TO_VOTE;
import static com.cooperative.assembly.vote.VoteChoice.YES;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = VoteService.class)
public class VoteServiceTest {

    @Autowired
    private VoteService service;

    @MockBean
    private VoteRepository repository;

    @MockBean
    private UserService userService;

    @MockBean
    private VotingSessionService votingSessionService;

    @Captor
    private ArgumentCaptor<Vote> voteCaptor;

    private String userId;
    private String voteId;
    private String agendaId;
    private String sessionId;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private User expectedUser;
    private VotingAgenda expectedAgenda;
    private VotingSession expectedSession;

    @Before
    public void setUp() {
        this.userId = "12429593009";
        this.voteId = randomUUID().toString();
        this.agendaId = randomUUID().toString();
        this.sessionId = randomUUID().toString();
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(1);
        this.expectedUser = new User(userId, ABLE_TO_VOTE);
        this.expectedAgenda = new VotingAgenda(agendaId, "agenda-title-1");
        this.expectedSession = new VotingSession(sessionId, expectedAgenda, openingTime, closingTime);
    }

    @Test
    public void shouldReturnValidationExceptionOnTryingToChoiceVoteForUserAndAgendaWhenFoundListedBetweenVotesThatAlreadyHasBeenVoted() {
        Vote vote = new Vote(voteId, userId, expectedAgenda, YES);
        when(repository.findByUserIdAndAgendaId(userId, agendaId)).thenReturn(asList(vote));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.choiceVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionOnTryingToChoiceVoteForUserAndAgendaWhenFoundBetweenVotesThatWasNotVotedYet() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(repository.findByUserIdAndAgendaId(userId, agendaId)).thenReturn(emptyList());

        assertThatCode(() -> service.choiceVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldLoadUserForVoteByAgendaIdWhenUserIsChoicingVote() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        service.choiceVote(userId, agendaId, YES);

        verify(userService, only()).loadUser(userId);
    }

    @Test
    public void shouldLoadSessionForVoteByAgendaIdWhenUserIsChoicingVote() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        service.choiceVote(userId, agendaId, YES);

        verify(votingSessionService, only()).loadVoteSession(agendaId);
    }

    @Test
    public void shouldSaveVoteToUserMakeChoiceOnVotingAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        service.choiceVote(userId, agendaId, YES);

        verify(repository, atLeastOnce()).save(voteCaptor.capture());
        assertThat(voteCaptor.getValue(), hasProperty("userId", equalTo(expectedUser.getId())));
        assertThat(voteCaptor.getValue(), hasProperty("agenda", equalTo(expectedSession.getAgenda())));
        assertThat(voteCaptor.getValue(), hasProperty("choice", equalTo(YES)));
    }

    @Test
    public void shouldReturnValidationExceptionWhenVoterUserIsUnableToVote() {
        User expectedUser = new User(userId, UNABLE_TO_VOTE);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.choiceVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVoterUserIsAbleToVote() {
        User expectedUser = new User(userId, ABLE_TO_VOTE);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);

        assertThatCode(() -> service.choiceVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnValidationExceptionWhenVotingSessionIsNoLongerOpenOnPastTimeRange() {
        VotingSession expectedSession = new VotingSession(sessionId, expectedAgenda, openingTime.minusMinutes(10), closingTime.minusMinutes(2));
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.choiceVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVotingSessionIsStillOpenOnPresentTimeRange() {
        User expectedUser = new User(userId, ABLE_TO_VOTE);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);

        assertThatCode(() -> service.choiceVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnSessionForAgendaIdForMakeVoteOnAgendaWhenUserIsChoicingVote() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        Vote expectedVote = new Vote(voteId, userId, expectedSession.getAgenda(), YES);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        Vote vote = service.choiceVote(userId, agendaId, YES);

        assertThat(vote, hasProperty("id", equalTo(expectedVote.getId())));
        assertThat(vote, hasProperty("agenda", equalTo(expectedVote.getAgenda())));
        assertThat(vote, hasProperty("choice", equalTo(expectedVote.getChoice())));
    }

}
