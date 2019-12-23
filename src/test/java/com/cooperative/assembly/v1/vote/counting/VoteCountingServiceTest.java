package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionService;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

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

    private String agendaId;
    private String sessionId;
    private String canvassId;
    private String agendaTitle;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    @Before
    public void setUp() {
        this.agendaId = randomUUID().toString();
        this.sessionId = randomUUID().toString();
        this.canvassId = randomUUID().toString();
        this.agendaTitle = "agenda-title-1";
        this.totalVotes = 10;
        this.affirmativeVotes = 6;
        this.negativeVotes = 4;
        this.openingTime = now().withNano(0);
        this.closingTime = openingTime.plusMinutes(1);
    }

    @Test
    public void shouldLoadVotingSessionForGivenAgendaWhenGettingVoteCounting() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(session);

        service.getVoteCounting(agendaId);

        verify(votingSessionService, only()).loadVoteSession(agendaId);
    }

    @Test
    public void shouldReturnLoadedVoteCountingByAgendaIdWithRespectiveAgendaAndSessionAndSessionCanvassData() {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime);
        when(votingSessionService.loadVoteSession(agendaId)).thenReturn(session);

        VoteCounting counting = service.getVoteCounting(agendaId);

        assertThat(counting, hasProperty("agenda", equalTo(agendaTitle)));
        assertThat(counting, hasProperty("openingTime", equalTo(openingTime)));
        assertThat(counting, hasProperty("closingTime", equalTo(closingTime)));
        assertThat(counting, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(counting, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(counting, hasProperty("negativeVotes", equalTo(negativeVotes)));
    }

}
