package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.voting.error.exception.ValidationException;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgendaService;
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
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
@ContextConfiguration(classes = VoteSessionService.class)
public class VoteSessionServiceTest {

    @Autowired
    private VoteSessionService service;

    @MockBean
    private VoteSessionRepository repository;

    @MockBean
    private MeetingAgendaService meetingAgendaService;

    @Captor
    private ArgumentCaptor<VoteSession> voteSessionCaptor;

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
    public void shouldSaveVoteSessionWhenOpeningVoteSessionForGivenAgendaId() {
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository, times(1)).save(any(VoteSession.class));
    }

    @Test
    public void shouldLoadAgendaByGivenAgendaIdWhenOpeningVoteSession() {
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(meetingAgendaService, times(1)).loadAgenda(agendaId);
    }

    @Test
    public void shouldSaveVoteSessionWithUUIDAndOpeningAndClosingTimeAndAgendaLoadedById() {
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        service.openFor(agendaId, deadlineMinutes);

        verify(repository).save(voteSessionCaptor.capture());
        assertThat(voteSessionCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(voteSessionCaptor.getValue(), hasProperty("agenda", notNullValue()));
        assertThat(voteSessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(voteSessionCaptor.getValue().getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(voteSessionCaptor.getValue().getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldReturnSavedVoteSessionAsIs() {
        MeetingAgenda expectedAgenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(expectedAgenda));

        VoteSession expectedVoteSession = new VoteSession(sessionId, expectedAgenda, openingTime, closingTime);
        when(repository.save(any(VoteSession.class))).thenReturn(expectedVoteSession);

        VoteSession voteSession = service.openFor(agendaId, deadlineMinutes);

        assertThat(voteSession, hasProperty("id", equalTo(sessionId)));
        assertThat(voteSession, hasProperty("agenda", notNullValue()));
        assertThat(voteSession, hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(voteSession.getOpeningTime().withNano(0), equalTo(openingTime));
        assertThat(voteSession.getClosingTime().withNano(0), equalTo(closingTime));
    }

    @Test
    public void shouldThrowsNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        VoteSession foundSession = new VoteSession(sessionId, agenda, openingTime, closingTime);
        when(repository.findByAgendaId(agendaId)).thenReturn(asList(foundSession));

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.openFor(agendaId, deadlineMinutes))
                .withMessage("Invalid parameter");
    }

    @Test
    public void shouldNeverThrowsAnyNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(repository.findByAgendaId(agendaId)).thenReturn(emptyList());
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.openFor(agendaId, deadlineMinutes))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(empty());

        assertThatExceptionOfType(NotFoundReferenceException.class)
                .isThrownBy(() -> service.loadSessionAgenda(agendaId))
                .withMessage("Reference not found");
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(meetingAgendaService.loadAgenda(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.loadSessionAgenda(agendaId))
                .doesNotThrowAnyException();
    }

}
