package com.cooperative.assembly.v1.voting.report;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
    private JmsTemplate jmsTemplate;

    @MockBean
    private VotingReportMapper reportMapper;

    @Captor
    private ArgumentCaptor<VotingReport> reportCaptor;

    @Captor
    private ArgumentCaptor<VotingSession> sessionCaptor;

    private String agendaId;
    private String canvassId;
    private String agendaTitle;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private Long deadlineMinutes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    @Before
    public void setUp() {
        this.agendaId = randomUUID().toString();
        this.canvassId = randomUUID().toString();
        this.agendaTitle = "agenda-title-1";
        this.totalVotes = 10;
        this.affirmativeVotes = 8;
        this.negativeVotes = 2;
        this.deadlineMinutes = 5L;
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(deadlineMinutes);
    }

    @Test
    public void shouldLoadClosedSessionsToPublishWhenReportClosedSessionResults() {
        messageProducer.reportClosedSessionResults();

        verify(service, only()).loadClosedSessionsToPublish();
    }

    @Test
    public void shouldConvertAllSessionCanvassInJsonVotingReportToSendMessageWhenLoadingClosedSessionsToPublish() {
        VotingSession session1 = buildNewSession();
        VotingSession session2 = buildNewSession();
        List<VotingSession> sessions = asList(session1, session2);
        when(service.loadClosedSessionsToPublish()).thenReturn(sessions);

        messageProducer.reportClosedSessionResults();

        verify(reportMapper, times(2)).toJson(any(VotingReport.class));
    }

    @Test
    public void shouldConvertEachSessionCanvassInJsonVotingReportWhenLoadingClosedSessionsToPublish() {
        String sessionId = randomUUID().toString();
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime, OPENED, FALSE);
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        messageProducer.reportClosedSessionResults();

        verify(reportMapper, only()).toJson(reportCaptor.capture());
        assertThat(reportCaptor.getValue(), hasProperty("title", equalTo(canvass.getTitle())));
        assertThat(reportCaptor.getValue(), hasProperty("status", equalTo(session.getStatus())));
        assertThat(reportCaptor.getValue(), hasProperty("totalVotes", equalTo(canvass.getTotalVotes())));
        assertThat(reportCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(canvass.getAffirmativeVotes())));
        assertThat(reportCaptor.getValue(), hasProperty("negativeVotes", equalTo(canvass.getNegativeVotes())));
        assertThat(reportCaptor.getValue(), hasProperty("agendaId", equalTo(agendaId)));
        assertThat(reportCaptor.getValue(), hasProperty("sessionId", equalTo(sessionId)));
    }

    @Test
    public void shouldConvertToReportAndSendAsMessageForEachSessionCanvassWhenLoadingClosedSessionsToPublish() throws Exception {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        List<VotingSession> sessions = asList(buildNewSession(canvass));
        when(service.loadClosedSessionsToPublish()).thenReturn(sessions);

        VotingReport report = VotingReport.buildReport(sessions.get(0));
        String reportAsString = new ObjectMapper().writeValueAsString(report);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(of(reportAsString));

        messageProducer.reportClosedSessionResults();

        verify(jmsTemplate, only()).convertAndSend(APPLICATION_QUEUE_NAME, reportAsString);
    }

    @Test
    public void shouldSaveEachSessionCanvassWithTruePublishedWhenLoadingClosedSessionsToPublish() throws Exception {
        String sessionId = randomUUID().toString();
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime, CLOSED, FALSE);
        when(service.loadClosedSessionsToPublish()).thenReturn(asList(session));

        VotingReport report = VotingReport.buildReport(session);
        String reportAsString = new ObjectMapper().writeValueAsString(report);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(of(reportAsString));

        messageProducer.reportClosedSessionResults();

        verify(service, atLeastOnce()).saveSession(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue(), hasProperty("id", equalTo(session.getId())));
        assertThat(sessionCaptor.getValue(), hasProperty("agenda", hasProperty("id", equalTo(agenda.getId()))));
        assertThat(sessionCaptor.getValue(), hasProperty("agenda", hasProperty("title", equalTo(agenda.getTitle()))));
        assertThat(sessionCaptor.getValue(), hasProperty("canvass", hasProperty("id", equalTo(canvass.getId()))));
        assertThat(sessionCaptor.getValue(), hasProperty("canvass", hasProperty("title", equalTo(canvass.getTitle()))));
        assertThat(sessionCaptor.getValue(), hasProperty("canvass", hasProperty("totalVotes", equalTo(canvass.getTotalVotes()))));
        assertThat(sessionCaptor.getValue(), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(canvass.getAffirmativeVotes()))));
        assertThat(sessionCaptor.getValue(), hasProperty("canvass", hasProperty("negativeVotes", equalTo(canvass.getNegativeVotes()))));
        assertThat(sessionCaptor.getValue(), hasProperty("openingTime", equalTo(session.getOpeningTime())));
        assertThat(sessionCaptor.getValue(), hasProperty("closingTime", equalTo(session.getClosingTime())));
        assertThat(sessionCaptor.getValue(), hasProperty("status", equalTo(session.getStatus())));
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
        List<VotingSession> sessions = asList(buildNewSession());
        when(service.loadClosedSessionsToPublish()).thenReturn(sessions);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(empty());

        messageProducer.reportClosedSessionResults();

        verify(jmsTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    public void shouldNeverSaveAnyCanvassStatusWhenCanNotConvertCanvassVotingReportIntoJsonForAnyClosedSessionToPublish() {
        List<VotingSession> sessions = asList(buildNewSession());
        when(service.loadClosedSessionsToPublish()).thenReturn(sessions);
        when(reportMapper.toJson(any(VotingReport.class))).thenReturn(empty());

        messageProducer.reportClosedSessionResults();

        verify(service, never()).saveSession(any(VotingSession.class));
    }

    private VotingSession buildNewSession() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        return buildNewSession(canvass);
    }

    private VotingSession buildNewSession(final VotingSessionCanvass canvass) {
        String sessionId = randomUUID().toString();
        return buildNewSession(sessionId, canvass);
    }

    private VotingSession buildNewSession(final String sessionId, final VotingSessionCanvass canvass) {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        return new VotingSession(sessionId, agenda, canvass, openingTime, closingTime, CLOSED, FALSE);
    }

}
