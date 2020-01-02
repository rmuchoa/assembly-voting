package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.builder.VotingAgendaBuilder;
import com.cooperative.assembly.builder.VotingSessionBuilder;
import com.cooperative.assembly.builder.VotingSessionCanvassBuilder;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.util.Optional.empty;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { VotingSessionService.class })
public class VotingSessionServiceTest {

    @Autowired
    private VotingSessionService service;

    @MockBean
    private VotingSessionRepository repository;

    @MockBean
    private VotingAgendaService votingAgendaService;

    @Captor
    private ArgumentCaptor<VotingSession> votingSessionCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> timeCaptor;

    @Test
    public void shouldFindSessionByAgendaIdToCheckIfAlreadyExistsSomeSessionOpenedForGivenAgenda() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, atLeastOnce()).findByAgendaId(eq(agendaId));
    }

    @Test
    public void shouldLoadAgendaByAgendaIdToOpenVotingSessionWhenCouldNotFindAnySessionByGivenAgendaId() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));
        when(repository.findByAgendaId(agendaId)).thenReturn(empty());

        service.openFor(agendaId, deadlineMinutes);

        verify(votingAgendaService, only()).loadAgenda(agendaId);
    }

    @Test
    public void shouldLoadAgendaByGivenAgendaIdWhenOpeningVotingSession() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(votingAgendaService, only()).loadAgenda(agendaId);
    }

    @Test
    public void shouldSaveVotingSessionWhenOpeningVotingSessionForGivenAgendaId() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, atLeastOnce()).save(any(VotingSession.class));
    }

    @Test
    public void shouldSaveVotingSessionWithUUIDAndOpeningAndClosingTimeAndAgendaLoadedById() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        LocalDateTime expectedOpeningTime = now().withNano(0);
        LocalDateTime expectedClosingTime = expectedOpeningTime.plusMinutes(deadlineMinutes);
        service.openFor(agendaId, deadlineMinutes);

        verify(repository).save(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", notNullValue()));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSessionCaptor.getValue().getOpeningTime().withNano(0), equalTo(expectedOpeningTime));
        assertThat(votingSessionCaptor.getValue().getClosingTime().withNano(0), equalTo(expectedClosingTime));
        assertThat(votingSessionCaptor.getValue(), hasProperty("status", equalTo(OPENED)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("published", equalTo(FALSE)));
    }

    @Test
    public void shouldReturnSavedVotingSessionAsIs() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda expectedAgenda = buildAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(expectedAgenda));

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession expectedVotingSession = buildSession(sessionId, expectedAgenda, openingTime, closingTime, OPENED, FALSE);
        when(repository.save(any(VotingSession.class))).thenReturn(expectedVotingSession);

        VotingSession votingSession = service.openFor(agendaId, deadlineMinutes);

        assertThat(votingSession, hasProperty("id", equalTo(sessionId)));
        assertThat(votingSession, hasProperty("agenda", notNullValue()));
        assertThat(votingSession, hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSession, hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSession.getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSession.getClosingTime().withNano(0), equalTo(closingTime));
        assertThat(votingSession, hasProperty("status", equalTo(OPENED)));
        assertThat(votingSession, hasProperty("published", equalTo(FALSE)));
    }

    @Test
    public void shouldThrowsValidationExceptionExceptionWhenFindAnotherSessionByTheGivenAgendaId() {
        String agendaId = randomUUID().toString();
        String sessionId = randomUUID().toString();
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId);
        VotingSession foundSession = buildSession(sessionId, agenda, deadlineMinutes);
        when(repository.findByAgendaId(agendaId)).thenReturn(of(foundSession));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.openFor(agendaId, deadlineMinutes))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(empty());

        assertThatExceptionOfType(NotFoundReferenceException.class)
                .isThrownBy(() -> service.loadSessionAgenda(agendaId))
                .withMessage("Reference not found");
    }

    @Test
    public void shouldNeverThrowsAnyExceptionWhenCouldNotFindAnyOtherSessionByGivenAgendaIdAndCouldFindTheAgendaById() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);

        when(repository.findByAgendaId(agendaId)).thenReturn(empty());
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.openFor(agendaId, deadlineMinutes))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNeverThrowExceptionWhenCanFindAnAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.loadSessionAgenda(agendaId))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanNotFindAnySessionByGivenAgendaId() {
        String agendaId = randomUUID().toString();
        when(repository.findByAgendaId(agendaId)).thenReturn(empty());

        assertThatExceptionOfType(NotFoundReferenceException.class)
                .isThrownBy(() -> service.loadVoteSession(agendaId))
                .withMessage("Reference not found");
    }

    @Test
    public void shouldNeverThrowExceptionWhenCanFindAnSessionByGivenAgendaId() {
        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId);
        when(repository.findById(sessionId)).thenReturn(of(session));

        assertThatCode(() -> service.loadVoteSession(sessionId))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldLoadOpenedSessionCanvassesWhenLoadingMissClosedSessions() {
        service.loadMissClosedSessions();

        verify(repository, only()).findByStatusAndClosingTimeBefore(eq(OPENED.toString()), any(LocalDateTime.class));
    }

    @Test
    public void shouldFindSessionsByOpenedSessionCanvassesAndClosingTimeBeforeNowWhenLoadingMissClosedSessions() {
        service.loadMissClosedSessions();

        verify(repository, only()).findByStatusAndClosingTimeBefore(eq(OPENED.toString()), timeCaptor.capture());
        assertThat(timeCaptor.getValue(), hasProperty("hour", equalTo(now().getHour())));
        assertThat(timeCaptor.getValue(), hasProperty("minute", equalTo(now().getMinute())));
    }

    @Test
    public void shouldReturnFoundSessionsByOpenedCanvassesAndClosingTimeWhenLoadingMissClosedSessions() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession session = buildSession(sessionId, agenda, openingTime, closingTime, OPENED, FALSE);

        when(repository.findByStatusAndClosingTimeBefore(eq(OPENED.toString()), any(LocalDateTime.class)))
                .thenReturn(asList(session));

        List<VotingSession> sessions = service.loadMissClosedSessions();
        assertThat(sessions.get(0), hasProperty("id", equalTo(sessionId)));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("openingTime", equalTo(openingTime)));
        assertThat(sessions.get(0), hasProperty("closingTime", equalTo(closingTime)));
        assertThat(sessions.get(0), hasProperty("status", equalTo(OPENED)));
        assertThat(sessions.get(0), hasProperty("published", equalTo(FALSE)));
    }

    @Test
    public void shouldLoadClosedSessionCanvassToPublishWhenLoadingClosedSessionsToPublish() {
        service.loadClosedSessionsToPublish();

        verify(repository, only()).findByStatusAndPublished(eq(CLOSED.toString()), eq(FALSE));
    }

    @Test
    public void shouldReturnFoundSessionsByClosedCanvassesWhenLoadingClosedSessionsToPublish() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession session = buildSession(sessionId, agenda, openingTime, closingTime, CLOSED, FALSE);
        when(repository.findByStatusAndPublished(eq(CLOSED.toString()), eq(FALSE))).thenReturn(asList(session));

        List<VotingSession> sessions = service.loadClosedSessionsToPublish();

        assertThat(sessions.get(0), hasProperty("id", equalTo(session.getId())));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("openingTime", equalTo(openingTime)));
        assertThat(sessions.get(0), hasProperty("closingTime", equalTo(closingTime)));
        assertThat(sessions.get(0), hasProperty("status", equalTo(CLOSED)));
        assertThat(sessions.get(0), hasProperty("published", equalTo(FALSE)));
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

    private VotingSessionCanvass buildCanvass(String canvassId, String title, Integer totalVotes, Integer affirmativeVotes, Integer negativeVotes, VotingSession session) {
        return VotingSessionCanvassBuilder.get()
                .with(VotingSessionCanvass::setId, canvassId)
                .with(VotingSessionCanvass::setTitle, title)
                .with(VotingSessionCanvass::setTotalVotes, totalVotes)
                .with(VotingSessionCanvass::setAffirmativeVotes, affirmativeVotes)
                .with(VotingSessionCanvass::setNegativeVotes, negativeVotes)
                .with(VotingSessionCanvass::setSession, session)
                .build();
    }

    private VotingSession buildSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(), now().withNano(0), now().withNano(0).plusMinutes(5), OPENED, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda, Long deadlineMinutes) {
        return buildSession(sessionId, agenda, now().withNano(0), now().withNano(0).plusMinutes(deadlineMinutes), OPENED, FALSE);
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

}
