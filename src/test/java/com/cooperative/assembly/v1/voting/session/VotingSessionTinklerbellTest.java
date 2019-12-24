package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvassService;
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
import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.util.Arrays.asList;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { VotingSessionTinklerbell.class, VotingSessionService.class, VotingSessionCanvassService.class })
public class VotingSessionTinklerbellTest {

    @Autowired
    private VotingSessionTinklerbell tinklerbell;

    @MockBean
    private VotingSessionService service;

    @MockBean
    private VotingSessionCanvassService votingSessionCanvassService;

    @Captor
    private ArgumentCaptor<VotingSessionCanvass> votingSessionCanvassCaptor;

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
    public void shouldLoadMissOpenSessionsWhenRingingTheSessionBell() {
        tinklerbell.ringTheSessionBell();

        verify(service, only()).loadMissClosedSessions();
    }

    @Test
    public void shouldCloseAllSessionsThatHaveBeenLoadedAsMissCloseStatus() {
        VotingSession session1 = buildNewSession();
        VotingSession session2 = buildNewSession();
        List<VotingSession> sessions = asList(session1, session2);
        when(service.loadMissClosedSessions()).thenReturn(sessions);

        tinklerbell.ringTheSessionBell();

        verify(votingSessionCanvassService, times(2)).saveCanvass(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldCloseEachSessionThatHaveBeenLoadedAsMissCloseStatus() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);
        List<VotingSession> sessions = asList(buildNewSession(canvass));
        when(service.loadMissClosedSessions()).thenReturn(sessions);

        tinklerbell.ringTheSessionBell();

        verify(votingSessionCanvassService).saveCanvass(votingSessionCanvassCaptor.capture());
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("id", equalTo(canvass.getId())));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("title", equalTo(canvass.getTitle())));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("totalVotes", equalTo(canvass.getTotalVotes())));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(canvass.getAffirmativeVotes())));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(canvass.getNegativeVotes())));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("status", equalTo(CLOSED)));
        assertThat(votingSessionCanvassCaptor.getValue(), hasProperty("published", is(canvass.getPublished())));
    }

    private VotingSession buildNewSession() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);
        return buildNewSession(canvass);
    }

    private VotingSession buildNewSession(final VotingSessionCanvass canvass) {
        String sessionId = randomUUID().toString();
        return buildNewSession(sessionId, canvass);
    }

    private VotingSession buildNewSession(final String sessionId, final VotingSessionCanvass canvass) {
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        return new VotingSession(sessionId, agenda, canvass, openingTime, closingTime);
    }

}
