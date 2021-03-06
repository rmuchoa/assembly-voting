package com.cooperative.assembly.v1.voting.report;

import com.cooperative.assembly.v1.vote.Vote;
import com.cooperative.assembly.v1.vote.VoteChoice;
import com.cooperative.assembly.v1.vote.VoteService;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.springframework.util.CollectionUtils.isEmpty;

@Log4j2
@Component
public class VotingReportMessageProducer {

    @Value("${spring.activemq.application.queue.name}")
    private String assemblyVotingQueueName;

    private JmsTemplate jmsTemplate;
    private VotingReportMapper reportMapper;
    private VotingSessionService votingSessionService;
    private VotingSessionCanvassService votingSessionCanvassService;

    @Autowired
    public VotingReportMessageProducer(final JmsTemplate jmsTemplate, final VotingReportMapper reportMapper,
                                       final VotingSessionService votingSessionService, final VotingSessionCanvassService votingSessionCanvassService) {
        this.jmsTemplate = jmsTemplate;
        this.reportMapper = reportMapper;
        this.votingSessionService = votingSessionService;
        this.votingSessionCanvassService = votingSessionCanvassService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void reportClosedSessionResults() {
        List<VotingSession> sessions = votingSessionService.loadClosedSessionsToPublish();

        if (!isEmpty(sessions)) {
            log.debug("Found closed sessions to publish couting results.");
            publishVotingReport(sessions);
        }
    }

    protected void publishVotingReport(final List<VotingSession> sessions) {
        for (VotingSession session : sessions) {
            buildReportToSendMessage(session);
        }
    }

    protected void buildReportToSendMessage(final VotingSession session) {
        VotingSessionCanvass canvass = votingSessionCanvassService.reloadVotingSessionCanvass(session);
        VotingReport report = VotingReport.buildReport(session, canvass);
        Optional<String> json = reportMapper.toJson(report);
        if (json.isPresent()) {
            sendReportMessage(json.get());
            updatePublishedSessionCanvass(session);
        }
    }

    protected void sendReportMessage(final String reporMessage) {
        log.debug("Publishing result from closed session: ", reporMessage);
        jmsTemplate.convertAndSend(assemblyVotingQueueName, reporMessage);
    }

    private void updatePublishedSessionCanvass(final VotingSession session) {
        session.setPublished(TRUE);
        log.debug("Marking session as published: ", session.getId());
        votingSessionService.saveSession(session);
    }

}
