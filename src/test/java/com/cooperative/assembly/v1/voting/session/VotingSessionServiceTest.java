package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.builder.VotingAgendaBuilder;
import com.cooperative.assembly.builder.VotingSessionBuilder;
import com.cooperative.assembly.builder.VotingSessionCanvassBuilder;
import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
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
@ContextConfiguration(classes = { VotingSessionService.class, VotingSessionCanvassService.class })
public class VotingSessionServiceTest {

    @Autowired
    private VotingSessionService service;

    @MockBean
    private VotingSessionRepository repository;

    @MockBean
    private VotingAgendaService votingAgendaService;

    @MockBean
    private VotingSessionCanvassService votingSessionCanvassService;

    @Captor
    private ArgumentCaptor<VotingSession> votingSessionCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> timeCaptor;

    @Test
    public void shouldSaveVotingSessionWhenOpeningVotingSessionForGivenAgendaId() {
        String agendaId = randomUUID().toString();
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, atLeastOnce()).save(any(VotingSession.class));
    }

    @Test
    public void shouldLoadAgendaByGivenAgendaIdWhenOpeningVotingSession() {
        String agendaId = randomUUID().toString();
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(votingAgendaService, only()).loadAgenda(agendaId);
    }

    @Test
    public void shouldSaveNewSessionCanvassWhenOpeningSessionForAgendaId() {
        String agendaId = randomUUID().toString();
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(votingSessionCanvassService, only()).saveCanvass(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldSaveVotingSessionWithUUIDAndOpeningAndClosingTimeAndAgendaLoadedById() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        Long deadlineMinutes = 5L;
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass expectedCanvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        when(votingSessionCanvassService.saveCanvass(any(VotingSessionCanvass.class))).thenReturn(expectedCanvass);

        LocalDateTime expectedOpeningTime = now().withNano(0);
        LocalDateTime expectedClosingTime = expectedOpeningTime.plusMinutes(deadlineMinutes);
        service.openFor(agendaId, deadlineMinutes);

        verify(repository).save(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", notNullValue()));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("totalVotes", equalTo(totalVotes))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(affirmativeVotes))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("negativeVotes", equalTo(negativeVotes))));
        assertThat(votingSessionCaptor.getValue().getOpeningTime().withNano(0), equalTo(expectedOpeningTime));
        assertThat(votingSessionCaptor.getValue().getClosingTime().withNano(0), equalTo(expectedClosingTime));
        assertThat(votingSessionCaptor.getValue(), hasProperty("status", equalTo(OPENED)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("published", equalTo(FALSE)));
    }

    @Test
    public void shouldReturnSavedVotingSessionAsIs() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        Long deadlineMinutes = 5L;
        VotingAgenda expectedAgenda = buildAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(expectedAgenda));

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass expectedCanvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        when(votingSessionCanvassService.saveCanvass(any(VotingSessionCanvass.class))).thenReturn(expectedCanvass);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession expectedVotingSession = new VotingSession(sessionId, expectedAgenda, expectedCanvass, openingTime, closingTime, OPENED, FALSE);
        when(repository.save(any(VotingSession.class))).thenReturn(expectedVotingSession);

        VotingSession votingSession = service.openFor(agendaId, deadlineMinutes);

        assertThat(votingSession, hasProperty("id", equalTo(sessionId)));
        assertThat(votingSession, hasProperty("agenda", notNullValue()));
        assertThat(votingSession, hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSession, hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("totalVotes", equalTo(totalVotes))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(affirmativeVotes))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("negativeVotes", equalTo(negativeVotes))));
        assertThat(votingSession.getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSession.getClosingTime().withNano(0), equalTo(closingTime));
        assertThat(votingSession, hasProperty("status", equalTo(OPENED)));
        assertThat(votingSession, hasProperty("published", equalTo(FALSE)));
    }

    @Test
    public void shouldThrowsNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
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
    public void shouldNeverThrowsAnyNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        Long deadlineMinutes = 5L;
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);

        when(repository.findByAgendaId(agendaId)).thenReturn(empty());
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.openFor(agendaId, deadlineMinutes))
                .doesNotThrowAnyException();
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

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass sessionCanvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession session = buildSession(sessionId, agenda, sessionCanvass, openingTime, closingTime, OPENED, FALSE);

        List<VotingSession> expectedSessions = asList(session);
        when(repository.findByStatusAndClosingTimeBefore(eq(OPENED.toString()), any(LocalDateTime.class))).thenReturn(expectedSessions);

        List<VotingSession> sessions = service.loadMissClosedSessions();
        assertThat(sessions.get(0), hasProperty("id", equalTo(sessionId)));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("id", equalTo(canvassId))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("totalVotes", equalTo(totalVotes))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(affirmativeVotes))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("negativeVotes", equalTo(negativeVotes))));
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

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass sessionCanvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(deadlineMinutes);
        VotingSession session = buildSession(sessionId, agenda, sessionCanvass, openingTime, closingTime, CLOSED, FALSE);
        List<VotingSession> expectedSessions = asList(session);
        when(repository.findByStatusAndPublished(eq(CLOSED.toString()), eq(FALSE))).thenReturn(expectedSessions);

        List<VotingSession> sessions = service.loadClosedSessionsToPublish();

        assertThat(sessions.get(0), hasProperty("id", equalTo(session.getId())));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(sessions.get(0), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("id", equalTo(canvassId))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("totalVotes", equalTo(totalVotes))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(affirmativeVotes))));
        assertThat(sessions.get(0), hasProperty("canvass", hasProperty("negativeVotes", equalTo(negativeVotes))));
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

    private VotingSessionCanvass buildCanvass() {
        return buildCanvass(randomUUID().toString(), "agenda-title-1", 10, 8, 2);
    }

    private VotingSessionCanvass buildCanvass(String canvassId, String title, Integer totalVotes, Integer affirmativeVotes, Integer negativeVotes) {
        return VotingSessionCanvassBuilder.get()
                .with(VotingSessionCanvass::setId, canvassId)
                .with(VotingSessionCanvass::setTitle, title)
                .with(VotingSessionCanvass::setTotalVotes, totalVotes)
                .with(VotingSessionCanvass::setAffirmativeVotes, affirmativeVotes)
                .with(VotingSessionCanvass::setNegativeVotes, negativeVotes)
                .build();
    }

    private VotingSession buildSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(), buildCanvass(),
                now().withNano(0), now().withNano(0).plusMinutes(5), OPENED, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda, Long deadlineMinutes) {
        return buildSession(sessionId, agenda, buildCanvass(),
                now().withNano(0), now().withNano(0).plusMinutes(deadlineMinutes), OPENED, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda, VotingSessionCanvass canvass, LocalDateTime openingTime, LocalDateTime closingTime, VotingSessionStatus status, Boolean published) {
        return VotingSessionBuilder.get()
                .with(VotingSession::setId, sessionId)
                .with(VotingSession::setAgenda, agenda)
                .with(VotingSession::setCanvass, canvass)
                .with(VotingSession::setOpeningTime, openingTime)
                .with(VotingSession::setClosingTime, closingTime)
                .with(VotingSession::setStatus, status)
                .with(VotingSession::setPublished, published)
                .build();
    }

}
