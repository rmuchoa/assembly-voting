package com.cooperative.assembly.v1.voting.session.canvass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes);

        service.saveCanvass(canvass);

        verify(repository, only()).save(canvass);
    }

    @Test
    public void shouldReturnSavedVotingSessionCanvassWhenUpdateCanvass() {
        VotingSessionCanvass canvass = new VotingSessionCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes);
        when(repository.save(canvass)).thenReturn(canvass);

        VotingSessionCanvass savedCanvass = service.saveCanvass(canvass);

        assertThat(savedCanvass, hasProperty("id", equalTo(canvassId)));
        assertThat(savedCanvass, hasProperty("title", equalTo(title)));
        assertThat(savedCanvass, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(savedCanvass, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(savedCanvass, hasProperty("negativeVotes", equalTo(negativeVotes)));
    }

}
