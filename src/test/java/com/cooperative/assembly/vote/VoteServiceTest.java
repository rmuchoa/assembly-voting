package com.cooperative.assembly.vote;

import com.cooperative.assembly.user.User;
import com.cooperative.assembly.user.UserService;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.voting.session.VotingSessionService;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvassService;
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
import static com.cooperative.assembly.vote.VoteChoice.NO;

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

    @MockBean
    private VotingSessionCanvassService votingSessionCanvassService;

    @Captor
    private ArgumentCaptor<Vote> voteCaptor;

    @Captor
    private ArgumentCaptor<VotingSessionCanvass> canvassCaptor;

    private String userId;
    private String voteId;
    private String agendaId;
    private String sessionId;
    private String canvassId;
    private String agendaTitle;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private User expectedUser;
    private VotingAgenda expectedAgenda;
    private VotingSession expectedSession;
    private VotingSessionCanvass expectedCanvass;
    private Vote expectedVote;

    @Before
    public void setUp() {
        this.userId = "12429593009";
        this.voteId = randomUUID().toString();
        this.agendaId = randomUUID().toString();
        this.sessionId = randomUUID().toString();
        this.canvassId = randomUUID().toString();
        this.agendaTitle = "agenda-title-1";
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(1);
        this.expectedUser = new User(userId, ABLE_TO_VOTE);
        this.expectedAgenda = new VotingAgenda(agendaId, agendaTitle);
        this.expectedCanvass = new VotingSessionCanvass(canvassId, agendaTitle, 0, 0, 0);
        this.expectedSession = new VotingSession(sessionId, expectedAgenda, expectedCanvass, openingTime, closingTime);
        this.expectedVote = new Vote(voteId, userId, expectedAgenda, expectedSession, YES);
    }

    @Test
    public void shouldFindVotesByUserAndAgendaWhenChooseVoteWhileCheckingIfUserAlreadyHasVotedOnGivenAgenda() {
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

        verify(repository, atLeastOnce()).findByUserIdAndAgendaId(userId, agendaId);
    }

    @Test
    public void shouldReturnValidationExceptionOnTryingToChooseVoteForUserAndAgendaWhenFoundListedBetweenVotesThatAlreadyHasBeenVoted() {
        Vote vote = new Vote(voteId, userId, expectedAgenda, expectedSession, YES);
        when(repository.findByUserIdAndAgendaId(userId, agendaId)).thenReturn(asList(vote));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.chooseVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionOnTryingToChooseVoteForUserAndAgendaWhenFoundBetweenVotesThatWasNotVotedYet() {
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(repository.findByUserIdAndAgendaId(userId, agendaId)).thenReturn(emptyList());
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldLoadUserForVoteByAgendaIdWhenUserIsChoosingVote() {
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

        verify(userService, only()).loadUser(userId);
    }

    @Test
    public void shouldLoadSessionForVoteByAgendaIdWhenUserIsChoosingVote() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

        verify(votingSessionService, atLeastOnce()).loadVoteSession(agendaId);
    }

    @Test
    public void shouldSaveVoteToUserMakeChooseOnVotingAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

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
                .isThrownBy(() -> service.chooseVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVoterUserIsAbleToVote() {
        User expectedUser = new User(userId, ABLE_TO_VOTE);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnValidationExceptionWhenVotingSessionIsNoLongerOpenOnPastTimeRange() {
        VotingSession expectedSession = new VotingSession(sessionId, expectedAgenda, expectedCanvass, openingTime.minusMinutes(10), closingTime.minusMinutes(2));
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.chooseVote(userId, agendaId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVotingSessionIsStillOpenOnPresentTimeRange() {
        User expectedUser = new User(userId, ABLE_TO_VOTE);
        when(userService.loadUser(userId)).thenReturn(expectedUser);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, agendaId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnSessionForAgendaIdForMakeVoteOnAgendaWhenUserIsChoosingVote() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        Vote expectedVote = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        Vote vote = service.chooseVote(userId, agendaId, YES);

        assertThat(vote, hasProperty("id", equalTo(expectedVote.getId())));
        assertThat(vote, hasProperty("agenda", equalTo(expectedVote.getAgenda())));
        assertThat(vote, hasProperty("choice", equalTo(expectedVote.getChoice())));
    }

    @Test
    public void shouldUpdateTotalizedVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        Vote expectedVote = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

        verify(votingSessionCanvassService, only()).saveCanvass(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldIncrementAffirmativeChoiceOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        Vote expectedVote = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, YES);

        verify(votingSessionCanvassService, only()).saveCanvass(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(0)));
    }

    @Test
    public void shouldIncrementNegativeChoiceOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        Vote expectedVote = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, NO);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, agendaId, NO);

        verify(votingSessionCanvassService, only()).saveCanvass(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(0)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(1)));
    }

    @Test
    public void shouldIncrementMultipleChoicesOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(expectedSession);

        Vote vote1 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        Vote vote2 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, NO);
        Vote vote3 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        Vote vote4 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        Vote vote5 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, YES);
        Vote vote6 = new Vote(voteId, userId, expectedSession.getAgenda(), expectedSession, NO);

        service.applyVoteOnSessionCanvass(agendaId, vote1);
        service.applyVoteOnSessionCanvass(agendaId, vote2);
        service.applyVoteOnSessionCanvass(agendaId, vote3);
        service.applyVoteOnSessionCanvass(agendaId, vote4);
        service.applyVoteOnSessionCanvass(agendaId, vote5);
        service.applyVoteOnSessionCanvass(agendaId, vote6);

        verify(votingSessionCanvassService, times(6)).saveCanvass(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(6)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(4)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(2)));
    }

}
