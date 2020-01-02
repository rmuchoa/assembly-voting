package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.builder.*;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { VoteCountingService.class, VotingSessionService.class })
public class VoteCountingServiceTest {

    @Autowired
    private VoteCountingService service;

    @MockBean
    private VotingSessionService votingSessionService;

    @Test
    public void shouldLoadVotingSessionForGivenAgendaWhenGettingVoteCounting() {
        String agendaId = randomUUID().toString();
        VotingAgenda agenda = buildAgenda(agendaId);

        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId, agenda);
        when(votingSessionService.loadVoteSessionByAgenda(agendaId)).thenReturn(session);

        service.getVoteCounting(agendaId);

        verify(votingSessionService, only()).loadVoteSessionByAgenda(agendaId);
    }

    @Test
    public void shouldReturnLoadedVoteCountingByAgendaIdWithRespectiveAgendaAndSessionAndSessionCanvassData() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String canvassId = randomUUID().toString();
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSessionCanvass canvass = buildCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);

        String sessionId = randomUUID().toString();
        LocalDateTime openingTime = now().withNano(0);
        LocalDateTime closingTime = openingTime.plusMinutes(5);
        VotingSessionStatus status = CLOSED;
        Boolean published = FALSE;
        VotingSession session = buildSession(sessionId, agenda, canvass, openingTime, closingTime, status, published);
        when(votingSessionService.loadVoteSessionByAgenda(agendaId)).thenReturn(session);

        VoteCounting counting = service.getVoteCounting(agendaId);

        assertThat(counting, hasProperty("agenda", equalTo(agendaTitle)));
        assertThat(counting, hasProperty("openingTime", equalTo(openingTime)));
        assertThat(counting, hasProperty("closingTime", equalTo(closingTime)));
        assertThat(counting, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(counting, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(counting, hasProperty("negativeVotes", equalTo(negativeVotes)));
        assertThat(counting, hasProperty("session", equalTo(status)));
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

    private VotingSession buildSession(String sessionId, VotingAgenda agenda) {
        return buildSession(sessionId, agenda, 5L);
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
