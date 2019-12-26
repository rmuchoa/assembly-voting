package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
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
@ContextConfiguration(classes = { VotingSessionTinklerbell.class, VotingSessionService.class })
public class VotingSessionTinklerbellTest {

    @Autowired
    private VotingSessionTinklerbell tinklerbell;

    @MockBean
    private VotingSessionService service;

    @Captor
    private ArgumentCaptor<VotingSession> votingSessionCaptor;

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

        verify(service, times(2)).saveSession(any(VotingSession.class));
    }

    @Test
    public void shouldCloseEachSessionThatHaveBeenLoadedAsMissCloseStatus() {
        String sessionId = randomUUID().toString();
        VotingAgenda agenda = new VotingAgenda(agendaId, agendaTitle);
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, agendaTitle, totalVotes, affirmativeVotes, negativeVotes);
        VotingSession session = new VotingSession(sessionId, agenda, canvass, openingTime, closingTime, OPENED, FALSE);
        when(service.loadMissClosedSessions()).thenReturn(asList(session));

        tinklerbell.ringTheSessionBell();

        verify(service).saveSession(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", equalTo(session.getId())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("id", equalTo(canvass.getId()))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("title", equalTo(canvass.getTitle()))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("totalVotes", equalTo(canvass.getTotalVotes()))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(canvass.getAffirmativeVotes()))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("negativeVotes", equalTo(canvass.getNegativeVotes()))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("openingTime", equalTo(session.getOpeningTime())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("closingTime", equalTo(session.getClosingTime())));
        assertThat(votingSessionCaptor.getValue(), hasProperty("status", equalTo(CLOSED)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("published", equalTo(session.getPublished())));
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
        return new VotingSession(sessionId, agenda, canvass, openingTime, closingTime, OPENED, FALSE);
    }

}
