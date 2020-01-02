package com.cooperative.assembly.v1.voting.session.canvass;

import com.cooperative.assembly.builder.*;

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
        VotingSessionCanvass canvass = buildCanvass(canvassId, title, totalVotes, affirmativeVotes, negativeVotes);
        when(repository.save(canvass)).thenReturn(canvass);

        VotingSessionCanvass savedCanvass = service.saveCanvass(canvass);

        assertThat(savedCanvass, hasProperty("id", equalTo(canvassId)));
        assertThat(savedCanvass, hasProperty("title", equalTo(title)));
        assertThat(savedCanvass, hasProperty("totalVotes", equalTo(totalVotes)));
        assertThat(savedCanvass, hasProperty("affirmativeVotes", equalTo(affirmativeVotes)));
        assertThat(savedCanvass, hasProperty("negativeVotes", equalTo(negativeVotes)));
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

}
