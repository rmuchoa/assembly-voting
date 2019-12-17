package com.cooperative.assembly.voting.meeting.agenda;

import com.cooperative.assembly.voting.error.exception.NotFoundReferenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static java.util.Optional.of;
import static java.util.Optional.empty;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MeetingAgendaService.class)
public class MeetingAgendaServiceTest {

    @Autowired
    private MeetingAgendaService service;

    @MockBean
    private MeetingAgendaRepository repository;

    @Captor
    private ArgumentCaptor<MeetingAgenda> meetingAgendaCaptor;

    @Test
    public void shouldSaveMeetingAgendaWhenCreatingByTitle() {
        service.create("agenda-title-1");

        verify(repository, only()).save(any(MeetingAgenda.class));
    }

    @Test
    public void shouldSaveNewMeetingAgendaWithUUIDAndReveivedAgendaTitle() {
        String title = "agenda-title-1";
        service.create(title);

        verify(repository).save(meetingAgendaCaptor.capture());
        assertThat(meetingAgendaCaptor.getValue(), hasProperty("id", not(isEmptyString())));
        assertThat(meetingAgendaCaptor.getValue(), hasProperty("title", equalTo(title)));
    }

    @Test
    public void shouldReturnSavedMeetingAgendaAsIs() {
        String title = "agenda-title-1";
        String id = randomUUID().toString();
        MeetingAgenda expectedMeetingAgenda = new MeetingAgenda(id, title);
        when(repository.save(any(MeetingAgenda.class))).thenReturn(expectedMeetingAgenda);

        MeetingAgenda meetingAgenda = service.create(title);

        assertThat(meetingAgenda, hasProperty("id", equalTo(id)));
        assertThat(meetingAgenda, hasProperty("title", equalTo(title)));
    }

    @Test
    public void shouldFindAgendaByIdWhenLoadingAgendaByReceivedAgendaIdReference() {
        String agendaId = randomUUID().toString();
        MeetingAgenda expectedAgenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(repository.findById(agendaId)).thenReturn(of(expectedAgenda));

        service.loadAgenda(agendaId);

        verify(repository, only()).findById(eq(agendaId));
    }

    @Test
    public void shouldReturnFoundAgendaByIdGotOptionally() {
        String agendaId = randomUUID().toString();
        MeetingAgenda expectedAgenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(repository.findById(agendaId)).thenReturn(of(expectedAgenda));

        MeetingAgenda agenda = service.loadAgenda(agendaId);

        assertThat(agenda, hasProperty("id", equalTo(expectedAgenda.getId())));
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanNotFindAnyAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        when(repository.findById(agendaId)).thenReturn(empty());

        assertThatExceptionOfType(NotFoundReferenceException.class)
                .isThrownBy(() -> service.loadAgenda(agendaId))
                .withMessage("Reference not found");
    }

    @Test
    public void shouldThrowNotFoundReferenceExceptionExceptionWhenCanFindAnAgendaByGivenId() {
        String agendaId = randomUUID().toString();
        MeetingAgenda agenda = new MeetingAgenda(agendaId, "agenda-title-1");
        when(repository.findById(agendaId)).thenReturn(of(agenda));

        assertThatCode(() -> service.loadAgenda(agendaId))
                .doesNotThrowAnyException();
    }

}
