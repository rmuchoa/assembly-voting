package com.cooperative.assembly.v1.voting.report;

import com.cooperative.assembly.builder.*;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { VotingReportMessageProducer.class, VotingSessionService.class })
@TestPropertySource(properties = { "spring.activemq.application.queue.name=assembly-voting-results" })
public class VotingReportMessageProducerTest {

    private static final String APPLICATION_QUEUE_NAME = "assembly-voting-results";

    @Autowired
    private VotingReportMessageProducer messageProducer;

    @MockBean
    private VotingSessionService service;

    @MockBean
    private VotingSessionCanvassService votingSessionCanvassService;

    @MockBean
    private JmsTemplate jmsTemplate;

    @MockBean
    private VotingReportMapper reportMapper;

    @Captor
    private ArgumentCaptor<VotingReport> reportCaptor;

    @Captor
    private ArgumentCaptor<VotingSession> sessionCaptor;

    @Test
    public void shouldLoadClosedSessionsToPublishWhenReportClosedSessionResults() {
        messageProducer.reportClosedSessionResults();

        verify(service, only()).loadClosedSessionsToPublish();
    }

    @Test
    public void shouldConvertAllSessionCanvassInJsonVotingReportToSendMessageWhenLoadingClosedSessionsToPublish() {
        VotingSession session1 = buildSession();
        VotingSession session2 = buildSession();
        List<VotingSession> sessions = asList(session1, session2);
        when(service.loadClosedSessionsToPublish()).thenReturn(sessions);

        VotingSessionCanvass canvass1 = buildCanvass(session1);
        VotingSessionCanvass canvass2 = buildCanvass(session1);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session1))).thenReturn(canvass1);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session2))).thenReturn(canvass2);

        messageProducer.reportClosedSessionResults();

        verify(reportMapper, times(2)).toJson(any(VotingReport.class));
    }

    @Test
    public void shouldConvertEachSessionCanvassInJsonVotingReportWhenLoadingClosedSessionsToPublish() {
        String canvassId = randomUUID().toString();
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(5);
        VotingSession session = buildSession(sessionId, agenda, openingTime, closingTime, OPENED, FALSE);
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass canvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes, session);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session))).thenReturn(canvass);

        messageProducer.reportClosedSessionResults();

        verify(reportMapper, only()).toJson(reportCaptor.capture());
        assertThat(reportCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(reportCaptor.getValue(), hasProperty("status", equalTo(OPENED)));
        assertThat(reportCaptor.getValue(), hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(reportCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(reportCaptor.getValue(), hasProperty("negativeVotes", equalTo(negativeVotes)));
        assertThat(reportCaptor.getValue(), hasProperty("agendaId", equalTo(agendaId)));
        assertThat(reportCaptor.getValue(), hasProperty("sessionId", equalTo(sessionId)));
    }

    @Test
    public void shouldConvertToReportAndSendAsMessageForEachSessionCanvassWhenLoadingClosedSessionsToPublish() throws Exception {
        VotingSession session = buildSession();
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        VotingSessionCanvass canvass = buildCanvass(session);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session))).thenReturn(canvass);

        VotingReport report = VotingReport.buildReport(session, canvass);
        String reportAsString = new ObjectMapper().writeValueAsString(report);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(of(reportAsString));

        messageProducer.reportClosedSessionResults();

        verify(jmsTemplate, only()).convertAndSend(APPLICATION_QUEUE_NAME, reportAsString);
    }

    @Test
    public void shouldSaveEachSessionCanvassWithTruePublishedWhenLoadingClosedSessionsToPublish() throws Exception {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(5);
        VotingSession session = buildSession(sessionId, agenda, openingTime, closingTime, CLOSED, FALSE);
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass canvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes, session);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session))).thenReturn(canvass);

        VotingReport report = VotingReport.buildReport(session, canvass);
        String reportAsString = new ObjectMapper().writeValueAsString(report);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(of(reportAsString));

        messageProducer.reportClosedSessionResults();

        verify(service, atLeastOnce()).saveSession(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue(), hasProperty("id", equalTo(sessionId)));
        assertThat(sessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agendaId))));
        assertThat(sessionCaptor.getValue(), hasProperty("agenda", hasProperty("title", equalTo(agendaTitle))));
        assertThat(sessionCaptor.getValue(), hasProperty("openingTime", equalTo(openingTime)));
        assertThat(sessionCaptor.getValue(), hasProperty("closingTime", equalTo(closingTime)));
        assertThat(sessionCaptor.getValue(), hasProperty("status", equalTo(CLOSED)));
        assertThat(sessionCaptor.getValue(), hasProperty("published", equalTo(TRUE)));
    }

    @Test
    public void shouldNeverConvertAnyReportWhenCanNotLoadAnyClosedSessionToPublish() {
        when(service.loadClosedSessionsToPublish()).thenReturn(emptyList());

        messageProducer.reportClosedSessionResults();

        verify(reportMapper, never()).toJson(any(VotingReport.class));
    }

    @Test
    public void shouldNeverReportAnyMessageWhenCanNotConvertCanvassVotingReportIntoJsonForAnyClosedSessionToPublish() {
        VotingSession session = buildSession();
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        VotingSessionCanvass canvass = buildCanvass(session);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session))).thenReturn(canvass);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(empty());

        messageProducer.reportClosedSessionResults();

        verify(jmsTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    public void shouldNeverSaveAnyCanvassStatusWhenCanNotConvertCanvassVotingReportIntoJsonForAnyClosedSessionToPublish() {
        VotingSession session = buildSession();
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(empty());

        VotingSessionCanvass canvass = buildCanvass(session);
        when(votingSessionCanvassService.reloadVotingSessionCanvass(eq(session))).thenReturn(canvass);

        messageProducer.reportClosedSessionResults();

        verify(service, never()).saveSession(any(VotingSession.class));
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
        return buildCanvass(buildSession());
    }

    private VotingSessionCanvass buildCanvass(VotingSession session) {
        return buildCanvass(randomUUID().toString(), "agenda-title-1", 10, 8, 2, session);
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

    private VotingSession buildSession() {
        return buildSession(randomUUID().toString());
    }

    private VotingSession buildSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(), now().withNano(0), now().withNano(0).plusMinutes(5), OPENED, FALSE);
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
