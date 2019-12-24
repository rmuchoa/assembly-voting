package com.cooperative.assembly.v1.voting.session.canvass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.CLOSED;
import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.OPENED;
import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = VotingSessionCanvassService.class)
public class VotingSessionCanvassServiceTest {

    @Autowired
    private VotingSessionCanvassService service;

    @MockBean
    private VotingSessionCanvassRepository repository;

    private String canvassId;
    private String title;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;

    @Before
    public void setUp() {
        this.canvassId = randomUUID().toString();
        this.title = "agenda-title-1";
        this.totalVotes = 0;
        this.affirmativeVotes = 0;
        this.negativeVotes = 0;
    }

    @Test
    public void shouldSaveVotingSessionCanvassWhenUpdateCanvass() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);

        service.saveCanvass(canvass);

        verify(repository, only()).save(canvass);
    }

    @Test
    public void shouldReturnSavedVotingSessionCanvassWhenUpdateCanvass() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);
        when(repository.save(canvass)).thenReturn(canvass);

        VotingSessionCanvass savedCanvass = service.saveCanvass(canvass);

        assertThat(savedCanvass, hasProperty("id", equalTo(canvassId)));
        assertThat(savedCanvass, hasProperty("title", equalTo(title)));
        assertThat(savedCanvass, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(savedCanvass, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(savedCanvass, hasProperty("negativeVotes", equalTo(negativeVotes)));
        assertThat(savedCanvass, hasProperty("status", equalTo(OPENED)));
        assertThat(savedCanvass, hasProperty("published", is(FALSE)));
    }

    @Test
    public void shouldFindCanvasByClosedStatusWhenLoadingOpenedSessionCanvass() {
        service.loadOpenedSessionCanvass();

        verify(repository, only()).findByStatus(eq(OPENED.toString()));
    }

    @Test
    public void shouldReturnFoundCanvassesWhenLoadingOpenedSessionCanvass() {
        VotingSessionCanvass expectedCanvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);
        List<VotingSessionCanvass> expectedCanvasses = asList(expectedCanvass);
        when(repository.findByStatus(eq(OPENED.toString()))).thenReturn(expectedCanvasses);

        List<VotingSessionCanvass> canvasses = service.loadOpenedSessionCanvass();

        assertThat(canvasses.get(0), hasProperty("id", equalTo(expectedCanvass.getId())));
        assertThat(canvasses.get(0), hasProperty("title", equalTo(expectedCanvass.getTitle())));
        assertThat(canvasses.get(0), hasProperty("totalVotes", equalTo(expectedCanvass.getTotalVotes())));
        assertThat(canvasses.get(0), hasProperty("affirmativeVotes", equalTo(expectedCanvass.getAffirmativeVotes())));
        assertThat(canvasses.get(0), hasProperty("negativeVotes", equalTo(expectedCanvass.getNegativeVotes())));
        assertThat(canvasses.get(0), hasProperty("status", equalTo(expectedCanvass.getStatus())));
        assertThat(canvasses.get(0), hasProperty("published", equalTo(expectedCanvass.getPublished())));
    }

    @Test
    public void shouldFindCanvasByOpenedStatusAndPublishedFalseWhenLoadingClosedSessionCanvassToPublish() {
        service.loadClosedSessionCanvassToPublish();

        verify(repository, only()).findByStatusAndPublished(eq(CLOSED.toString()), eq(FALSE));
    }

    @Test
    public void shouldReturnFoundCanvassesWhenLoadingClosedSessionCanvassToPublish() {
        VotingSessionCanvass expectedCanvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes, OPENED, FALSE);
        List<VotingSessionCanvass> expectedCanvasses = asList(expectedCanvass);
        when(repository.findByStatusAndPublished(eq(CLOSED.toString()), eq(FALSE))).thenReturn(expectedCanvasses);

        List<VotingSessionCanvass> canvasses = service.loadClosedSessionCanvassToPublish();

        assertThat(canvasses.get(0), hasProperty("id", equalTo(expectedCanvass.getId())));
        assertThat(canvasses.get(0), hasProperty("title", equalTo(expectedCanvass.getTitle())));
        assertThat(canvasses.get(0), hasProperty("totalVotes", equalTo(expectedCanvass.getTotalVotes())));
        assertThat(canvasses.get(0), hasProperty("affirmativeVotes", equalTo(expectedCanvass.getAffirmativeVotes())));
        assertThat(canvasses.get(0), hasProperty("negativeVotes", equalTo(expectedCanvass.getNegativeVotes())));
        assertThat(canvasses.get(0), hasProperty("status", equalTo(expectedCanvass.getStatus())));
        assertThat(canvasses.get(0), hasProperty("published", equalTo(expectedCanvass.getPublished())));
    }

}
