package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.builder.*;
import com.cooperative.assembly.v1.user.User;
import com.cooperative.assembly.v1.user.UserService;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.v1.user.VotingAbility;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static com.cooperative.assembly.v1.vote.VoteChoice.YES;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
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
import static com.cooperative.assembly.v1.user.VotingAbility.ABLE_TO_VOTE;
import static com.cooperative.assembly.v1.user.VotingAbility.UNABLE_TO_VOTE;

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

    @Test
    public void shouldFindVotesByUserAndAgendaWhenChooseVoteWhileCheckingIfUserAlreadyHasVotedOnGivenAgenda() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, sessionId, YES);

        verify(repository, atLeastOnce()).findByUserIdAndSessionId(userId, sessionId);
    }

    @Test
    public void shouldReturnValidationExceptionOnTryingToChooseVoteForUserAndAgendaWhenFoundListedBetweenVotesThatAlreadyHasBeenVoted() {
        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);

        String userId = "1234567890";
        String voteId = randomUUID().toString();
        Vote vote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.findByUserIdAndSessionId(userId, sessionId)).thenReturn(asList(vote));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.chooseVote(userId, sessionId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionOnTryingToChooseVoteForUserAndAgendaWhenFoundBetweenVotesThatWasNotVotedYet() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(repository.findByUserIdAndSessionId(userId, sessionId)).thenReturn(emptyList());
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, sessionId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldLoadUserForVoteByAgendaIdWhenUserIsChoosingVote() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, sessionId, YES);

        verify(userService, only()).loadUser(userId);
    }

    @Test
    public void shouldLoadSessionForVoteByAgendaIdWhenUserIsChoosingVote() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, sessionId, YES);

        verify(votingSessionService, atLeastOnce()).loadVoteSession(sessionId);
    }

    @Test
    public void shouldSaveVoteToUserMakeChooseOnVotingAgenda() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        service.chooseVote(userId, sessionId, YES);

        verify(repository, atLeastOnce()).save(voteCaptor.capture());
        assertThat(voteCaptor.getValue(), hasProperty("userId", equalTo(expectedUser.getId())));
        assertThat(voteCaptor.getValue(), hasProperty("session", equalTo(expectedSession)));
        assertThat(voteCaptor.getValue(), hasProperty("choice", equalTo(YES)));
    }

    @Test
    public void shouldReturnValidationExceptionWhenVoterUserIsUnableToVote() {
        String userId = "1234567890";
        User expectedUser = buildUserUnable(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.chooseVote(userId, sessionId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVoterUserIsAbleToVote() {
        String userId = "1234567890";
        User expectedUser = buildUserAble(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, sessionId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnValidationExceptionWhenVotingSessionIsNoLongerOpenOnPastTimeRange() {
        String userId = "1234567890";
        User expectedUser = buildUser(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildPastSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.chooseVote(userId, sessionId, YES))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldDontReturnAnyExceptionWhenVotingSessionIsStillOpenOnPresentTimeRange() {
        String userId = "1234567890";
        User expectedUser = buildUser(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        assertThatCode(() -> service.chooseVote(userId, sessionId, YES))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldReturnSessionForAgendaIdForMakeVoteOnAgendaWhenUserIsChoosingVote() {
        String userId = "1234567890";
        User expectedUser = buildUser(userId);
        when(userService.loadUser(userId)).thenReturn(expectedUser);

        String sessionId = randomUUID().toString();
        VotingSession expectedSession = buildSession(sessionId);
        when(votingSessionService.loadVoteSession(sessionId)).thenReturn(expectedSession);

        String voteId = randomUUID().toString();
        Vote expectedVote = buildVoteYes(voteId, userId, expectedSession);
        when(repository.save(any(Vote.class))).thenReturn(expectedVote);

        Vote vote = service.chooseVote(userId, sessionId, YES);

        assertThat(vote, hasProperty("id", equalTo(expectedVote.getId())));
        assertThat(vote, hasProperty("session", equalTo(expectedVote.getSession())));
        assertThat(vote, hasProperty("choice", equalTo(expectedVote.getChoice())));
    }

    private User buildUserAble(String userId) {
        return buildUser(userId, ABLE_TO_VOTE);
    }

    private User buildUserUnable(String userId) {
        return buildUser(userId, UNABLE_TO_VOTE);
    }

    private User buildUser(String userId) {
        return buildUserAble(userId);
    }

    private User buildUser(String userId, VotingAbility ability) {
        return UserBuilder.get()
                .with(User::setId, userId)
                .with(User::setAbility, ability)
                .build();
    }

    private VotingAgenda buildAgenda() {
        return buildAgenda(randomUUID().toString());
    }

    private VotingAgenda buildAgenda(String agendaId) {
        return buildAgenda(agendaId, "agenda-title-1");
    }

    private VotingAgenda buildAgenda(String agendaId, String agendaTitle) {
        return VotingAgendaBuilder.get()
                .with(VotingAgenda::setId, agendaId)
                .with(VotingAgenda::setTitle, agendaTitle)
                .build();
    }

    private VotingSession buildSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(),
                now().withNano(0), now().withNano(0).plusMinutes(5), OPENED, FALSE);
    }

    private VotingSession buildPastSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(),
                now().withNano(0).minusMinutes(10), now().withNano(0).minusMinutes(5), OPENED, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda, LocalDateTime openingTime, LocalDateTime closingTime, VotingSessionStatus status, Boolean published) {
        return VotingSessionBuilder.get()
                .with(VotingSession::setId, sessionId)
                .with(VotingSession::setAgenda, agenda)
                .with(VotingSession::setOpeningTime, openingTime)
                .with(VotingSession::setClosingTime, closingTime)
                .with(VotingSession::setStatus, status)
                .with(VotingSession::setPublished, published)
                .build();
    }

    private Vote buildVoteYes(String voteId, String userId, VotingSession session) {
        return buildVote(voteId, userId, session, YES);
    }

    private Vote buildVote(String voteId, String userId, VotingSession session, VoteChoice choice) {
        return VoteBuilder.get()
                .with(Vote::setId, voteId)
                .with(Vote::setUserId, userId)
                .with(Vote::setSession, session)
                .with(Vote::setChoice, choice)
                .build();
    }

}
