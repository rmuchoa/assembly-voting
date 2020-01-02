package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.builder.*;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void shouldLoadMissOpenSessionsWhenRingingTheSessionBell() {
        tinklerbell.ringTheSessionBell();

        verify(service, only()).loadMissClosedSessions();
    }

    @Test
    public void shouldCloseAllSessionsThatHaveBeenLoadedAsMissCloseStatus() {
        VotingSession session1 = buildSession();
        VotingSession session2 = buildSession();
        List<VotingSession> sessions = asList(session1, session2);
        when(service.loadMissClosedSessions()).thenReturn(sessions);

        tinklerbell.ringTheSessionBell();

        verify(service, times(2)).saveSession(any(VotingSession.class));
    }

    @Test
    public void shouldCloseEachSessionThatHaveBeenLoadedAsMissCloseStatus() {
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
        VotingSession session = buildSession(sessionId, agenda, canvass, openingTime, closingTime, OPENED, FALSE);
        when(service.loadMissClosedSessions()).thenReturn(asList(session));

        tinklerbell.ringTheSessionBell();

        verify(service).saveSession(votingSessionCaptor.capture());
        assertThat(votingSessionCaptor.getValue(), hasProperty("id", equalTo(sessionId)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("id", equalTo(canvassId))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("title", equalTo(agendaTitle))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("totalVotes", equalTo(totalVotes))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("affirmativeVotes", equalTo(affirmativeVotes))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("canvass", hasProperty("negativeVotes", equalTo(negativeVotes))));
        assertThat(votingSessionCaptor.getValue(), hasProperty("openingTime", equalTo(openingTime)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("closingTime", equalTo(closingTime)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("status", equalTo(CLOSED)));
        assertThat(votingSessionCaptor.getValue(), hasProperty("published", equalTo(FALSE)));
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

    private VotingSession buildSession() {
        return buildSession(randomUUID().toString());
    }

    private VotingSession buildSession(String sessionId) {
        return buildSession(sessionId, buildAgenda(), buildCanvass(),
                now().withNano(0), now().withNano(0).plusMinutes(5), OPENED, FALSE);
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
