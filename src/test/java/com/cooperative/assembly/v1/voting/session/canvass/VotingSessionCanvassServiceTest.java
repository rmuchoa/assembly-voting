package com.cooperative.assembly.v1.voting.session.canvass;

import com.cooperative.assembly.builder.*;

import com.cooperative.assembly.v1.vote.Vote;
import com.cooperative.assembly.v1.vote.VoteChoice;
import com.cooperative.assembly.v1.vote.VoteService;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
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

import static com.cooperative.assembly.v1.vote.VoteChoice.NO;
import static com.cooperative.assembly.v1.vote.VoteChoice.YES;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.util.Optional.of;
import static java.util.Arrays.asList;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = VotingSessionCanvassService.class)
public class VotingSessionCanvassServiceTest {

    @Autowired
    private VotingSessionCanvassService service;

    @MockBean
    private VotingSessionCanvassRepository repository;

    @MockBean
    private VoteService voteService;

    @Captor
    private ArgumentCaptor<VotingSessionCanvass> canvassCaptor;

    @Test
    public void shouldSaveVotingSessionCanvassWhenUpdateCanvass() {
        VotingSessionCanvass canvass = buildCanvass();

        service.saveCanvass(canvass);

        verify(repository, only()).save(canvass);
    }

    @Test
    public void shouldReturnSavedVotingSessionCanvassWhenUpdateCanvass() {
        String canvassId = randomUUID().toString();
        String title = "agenda-title-1";
        Integer totalVotes = 10;
        Integer affirmativeVotes = 8;
        Integer negativeVotes = 2;
        VotingSession session = buildSession();
        VotingSessionCanvass canvass = buildCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes, session);
        when(repository.save(canvass)).thenReturn(canvass);

        VotingSessionCanvass savedCanvass = service.saveCanvass(canvass);

        assertThat(savedCanvass, hasProperty("id", equalTo(canvassId)));
        assertThat(savedCanvass, hasProperty("title", equalTo(title)));
        assertThat(savedCanvass, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(savedCanvass, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(savedCanvass, hasProperty("negativeVotes", equalTo(negativeVotes)));
    }

    @Test
    public void shouldFindCanvassBySessionIdWhenReloadingSessionCanvass() {
        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).findBySessionId(sessionId);
    }

    @Test
    public void shouldLoadVotesFromSessionWhenReloadingVotingSessionCanvass() {
        VotingSession session = buildSession();

        service.reloadVotingSessionCanvass(session);

        verify(voteService, only()).getSessionVotes(eq(session));
    }

    @Test
    public void shouldNeverLoadVotesRetotalizeOrSaveResultsFromSessionWhenSessionStatusIsAlreadyClosed() {
        VotingSession session = buildSession(CLOSED);

        service.reloadVotingSessionCanvass(session);

        verify(voteService, never()).getSessionVotes(eq(session));
        verify(repository, never()).save(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldSaveSessionCanvassForVotingSessionWhenReloadingCanvassFromOpenedVotingSession() {
        VotingSession session = buildSession(OPENED);
        List<Vote> votes = asList(buildVoteYes(session));
        when(voteService.getSessionVotes(session)).thenReturn(votes);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).save(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldSaveFoundSessionCanvassWithNewTotalVoteValues() {
        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId, OPENED);
        VotingSessionCanvass canvass = buildCanvass(session, 2, 1, 0);
        when(repository.findBySessionId(sessionId)).thenReturn(of(canvass));

        List<Vote> votes = asList(buildVoteYes(session));
        when(voteService.getSessionVotes(session)).thenReturn(votes);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).save(any(VotingSessionCanvass.class));
    }

    @Test
    public void shouldIncrementAffirmativeChoiceOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId, agenda);

        List<Vote> votes = asList(buildVoteYes(session));
        when(voteService.getSessionVotes(session)).thenReturn(votes);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).save(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(0)));
    }

    @Test
    public void shouldIncrementNegativeChoiceOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId, agenda);

        List<Vote> votes = asList(buildVoteNo(session));
        when(voteService.getSessionVotes(session)).thenReturn(votes);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).save(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agendaTitle)));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(1)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(0)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(1)));
    }

    @Test
    public void shouldIncrementMultipleChoicesOnVotingSessionCanvassWhenChoosingVoteForSessionAgenda() {
        String agendaId = randomUUID().toString();
        String agendaTitle = "agenda-title-1";
        VotingAgenda agenda = buildAgenda(agendaId, agendaTitle);

        String sessionId = randomUUID().toString();
        VotingSession session = buildSession(sessionId, agenda);

        Vote voteYes1 = buildVoteYes(session);
        Vote voteNo1 = buildVoteNo(session);
        Vote voteYes2 = buildVoteYes(session);
        Vote voteYes3 = buildVoteYes(session);
        Vote voteYes4 = buildVoteYes(session);
        Vote voteNo2 = buildVoteNo(session);
        List<Vote> votes = asList(voteYes1, voteNo1, voteYes2, voteYes3, voteYes4, voteNo2);
        when(voteService.getSessionVotes(session)).thenReturn(votes);

        service.reloadVotingSessionCanvass(session);

        verify(repository, atLeastOnce()).save(canvassCaptor.capture());
        assertThat(canvassCaptor.getValue(), hasProperty("title", equalTo(agenda.getTitle())));
        assertThat(canvassCaptor.getValue(), hasProperty("totalVotes", equalTo(6)));
        assertThat(canvassCaptor.getValue(), hasProperty("affirmativeVotes", equalTo(4)));
        assertThat(canvassCaptor.getValue(), hasProperty("negativeVotes", equalTo(2)));
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

    private VotingSessionCanvass buildCanvass(VotingSession session, Integer totalVotes, Integer affirmativeVotes, Integer negativeVotes) {
        return buildCanvass(randomUUID().toString(), "agenda-title-1", totalVotes, affirmativeVotes, negativeVotes, session);
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

    private VotingSession buildSession(VotingSessionStatus status) {
        return buildSession(randomUUID().toString(), buildAgenda(), now().withNano(0), now().withNano(0).plusMinutes(5), status, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingSessionStatus status) {
        return buildSession(sessionId, buildAgenda(), now().withNano(0), now().withNano(0).plusMinutes(5), status, FALSE);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda) {
        return buildSession(sessionId, agenda, 5L);
    }

    private VotingSession buildSession(String sessionId, VotingAgenda agenda, Long deadlineMinutes) {
        return buildSession(sessionId, agenda, now().withNano(0), now().withNano(0).plusMinutes(deadlineMinutes), OPENED, FALSE);
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

    private Vote buildVoteYes(VotingSession session) {
        String userId = "1234567890";
        return buildVote(randomUUID().toString(), userId, session, YES);
    }

    private Vote buildVoteNo(VotingSession session) {
        String userId = "1234567890";
        return buildVote(randomUUID().toString(), userId, session, NO);
    }

    private Vote buildVote(String voteId, String userId, VotingSession session, VoteChoice choice) {
        return VoteBuilder.get()
                .with(Vote::setId, voteId)
                .with(Vote::setUserId, userId)
                .with(Vote::setSession, session)
                .with(Vote::setChoice, choice)
                .build();
    }

}
