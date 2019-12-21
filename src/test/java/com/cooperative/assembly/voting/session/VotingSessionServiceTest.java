package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.error.exception.ValidationException;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaService;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
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

    private String agendaId;
    private String sessionId;
    private String canvassId;
    private String agendaTitle;
    private Long deadlineMinutes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    @Before
    public void setUp() {
        this.agendaId = randomUUID().toString();
        this.sessionId = randomUUID().toString();
        this.canvassId = randomUUID().toString();
        this.agendaTitle = "agenda-title-1";
        this.deadlineMinutes = 5L;
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(deadlineMinutes);
    }

    @Test
    public void shouldSaveVotingSessionWhenOpeningVotingSessionForGivenAgendaId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, times(1)).save(any(VotingSession.class));
    }

    @Test
    public void shouldLoadAgendaByGivenAgendaIdWhenOpeningVotingSession() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(votingAgendaService, times(1)).loadAgenda(agendaId);
    }

    @Test
    public void shouldSaveVotingSessionWithUUIDAndOpeningAndClosingTimeAndAgendaLoadedById() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass expectedCanvass = new VotingSessionCanvass(canvassId, agendaTitle, 0, 0, 0);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));
        when(votingSessionCanvassService.saveCanvass(any(VotingSessionCanvass.class))).thenReturn(expectedCanvass);

        service.openFor(agendaId, deadlineMinutes);

        verify(repository).save(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", notNullValue()));
        assertThat(votingSessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("totalVotes", equalTo(0))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(0))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("negativeVotes", equalTo(0))));
        assertThat(votingSessionCaptor.getValue().getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSessionCaptor.getValue().getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldReturnSavedVotingSessionAsIs() {
        VotingAgenda expectedAgenda = new VotingAgenda(agendaId, agendaTitle);
        when(votingAgendaService.loadAgenda(agendaId)).thenReturn(of(expectedAgenda));

        VotingSessionCanvass expectedCanvass = new VotingSessionCanvass(canvassId, agendaTitle, 0, 0, 0);
        when(votingSessionCanvassService.saveCanvass(any(VotingSessionCanvass.class))).thenReturn(expectedCanvass);

        VotingSession expectedVotingSession = new VotingSession(sessionId, expectedAgenda, expectedCanvass, openingTime, closingTime);
        when(repository.save(any(VotingSession.class))).thenReturn(expectedVotingSession);

        VotingSession votingSession = service.openFor(agendaId, deadlineMinutes);

        assertThat(votingSession, hasProperty("id", equalTo(sessionId)));
        assertThat(votingSession, hasProperty("agenda", notNullValue()));
        assertThat(votingSession, hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(votingSession, hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("totalVotes", equalTo(0))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(0))));
        assertThat(votingSession, hasProperty("canvass", hasProperty("negativeVotes", equalTo(0))));
        assertThat(votingSession.getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(votingSession.getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldThrowsNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, 0, 0, 0);
        VotingSession foundSession = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime);
        when(repository.findByAgendaId(agendaId)).thenReturn(of(foundSession));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.openFor(agendaId, deadlineMinutes))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldNeverThrowsAnyNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
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
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
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
        String canvassId = randomUUID().toString();
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, 0, 0, 0);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime);
        when(repository.findByAgendaId(agendaId)).thenReturn(of(session));

        assertThatCode(() -> service.loadVoteSession(agendaId))
                .doesNotThrowAnyException();
    }

}
