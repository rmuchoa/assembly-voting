package com.cooperative.assembly.voting.agenda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static java.util.Optional.of;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = VotingAgendaService.class)
public class VotingAgendaServiceTest {

    @Autowired
    private VotingAgendaService service;

    @MockBean
    private VotingAgendaRepository repository;

    @Captor
    private ArgumentCaptor<VotingAgenda> votingAgendaCaptor;

    @Test
    public void shouldSaveVotingAgendaWhenCreatingByTitle() {
        service.create("agenda-title-1");

        verify(repository, only()).save(any(VotingAgenda.class));
    }

    @Test
    public void shouldSaveNewVotingAgendaWithUUIDAndReveivedAgendaTitle() {
        String title = "agenda-title-1";
        service.create(title);

        verify(repository).save(votingAgendaCaptor.capture());
        assertThat(votingAgendaCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(votingAgendaCaptor.getValue(), hasProperty("title", equalTo(title)));
    }

    @Test
    public void shouldReturnSavedVotingAgendaAsIs() {
        String title = "agenda-title-1";
        String id = randomUUID().toString();
        VotingAgenda expectedVotingAgenda = new VotingAgenda(id, title);
        when(repository.save(any(VotingAgenda.class))).thenReturn(expectedVotingAgenda);

        VotingAgenda votingAgenda = service.create(title);

        assertThat(votingAgenda, hasProperty("id", equalTo(id)));
        assertThat(votingAgenda, hasProperty("title", equalTo(title)));
    }

    @Test
    public void shouldFindAgendaByIdWhenLoadingAgendaByReceivedAgendaIdReference() {
        String agendaId = randomUUID().toString();
        VotingAgenda expectedAgenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(repository.findById(agendaId)).thenReturn(of(expectedAgenda));

        service.loadAgenda(agendaId);

        verify(repository, only()).findById(eq(agendaId));
    }

    @Test
    public void shouldReturnFoundAgendaByIdGotOptionally() {
        String agendaId = randomUUID().toString();
        VotingAgenda expectedAgenda = new VotingAgenda(agendaId, "agenda-title-1");
        when(repository.findById(agendaId)).thenReturn(of(expectedAgenda));

        Optional<VotingAgenda> agenda = service.loadAgenda(agendaId);

        assertThat(agenda.isPresent(), is(TRUE));
        assertThat(agenda.get(), hasProperty("id", equalTo(expectedAgenda.getId())));
    }

}
