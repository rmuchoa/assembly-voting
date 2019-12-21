package com.cooperative.assembly.voting.session.canvass;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = VotingSessionCanvassService.class)
public class VotingSessionCanvassServiceTest {

    @Autowired
    private VotingSessionCanvassService service;

    @MockBean
    private VotingSessionCanvassRepository repository;

    @Captor
    private ArgumentCaptor<VotingSessionCanvass> canvassCaptor;

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

}
