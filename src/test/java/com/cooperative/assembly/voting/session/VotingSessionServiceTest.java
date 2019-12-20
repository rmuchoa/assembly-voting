package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaService;
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

import static java.util.Optional.empty;
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
@ContextConfiguration(classes = VotingSessionService.class)
public class VotingSessionServiceTest {

    @Autowired
    private VotingSessionService service;

    @MockBean
    private VotingSessionRepository repository;

    @MockBean
    private VotingAgendaService votingAgendaService;

    @Captor
    private ArgumentCaptor<VotingSession> votingSessionCaptor;

    private String agendaId;
    private String sessionId;
    private Long deadlineMinutes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    @Before
    public void setUp() {
        this.agendaId = randomUUID().toString();
        this.sessionId = randomUUID().toString();
        this.deadlineMinutes = 5L;
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(deadlineMinutes);
    }

    @Test
    public void shouldSaveVotingSessionWhenOpeningVotingSessionForGivenAgendaId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, times(1)).save(any(VotingSession.class));
    }

    @Test
    public void shouldLoadAgendaByGivenAgendaIdWhenOpeningVotingSession() {
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(votingAgendaService, times(1)).loadAgenda(agendaId);
    }

    @Test
    public void shouldSaveVotingSessionWithUUIDAndOpeningAndClosingTimeAndAgendaLoadedById() {
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository).save(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", notNullValue()));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSessionCaptor.getValue().getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSessionCaptor.getValue().getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldReturnSavedVotingSessionAsIs() {
        VotingAgenda expectedAgenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(expectedAgenda));

        VotingSession expectedVotingSession = new VotingSession(sessionId, expectedAgenda, openingTime, closingTime);
        when(repository.save(any(VotingSession.class))).thenReturn(expectedVotingSession);

        VotingSession votingSession = service.openFor(agendaId, deadlineMinutes);

        assertThat(votingSession, hasProperty("id", equalTo(sessionId)));
        assertThat(votingSession, hasProperty("agenda", notNullValue()));
        assertThat(votingSession, hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSession.getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSession.getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldThrowsNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
        VotingSession foundSession = new VotingSession(sessionId, agenda, openingTime, closingTime);
        when(repository.findByAgendaId(agendaId)).thenReturn(of(foundSession));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.openFor(agendaId, deadlineMinutes))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldNeverThrowsAnyNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
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
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
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
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = new VotingAgenda(agendaId, "agenda-title-1");
        VotingSession session = new VotingSession(sessionId, agenda, openingTime, closingTime);
        when(repository.findByAgendaId(agendaId)).thenReturn(of(session));

        assertThatCode(() -> service.loadVoteSession(agendaId))
                .doesNotThrowAnyException();
    }

}
