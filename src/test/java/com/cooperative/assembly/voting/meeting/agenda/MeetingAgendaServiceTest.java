package com.cooperative.assembly.voting.meeting.agenda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;

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

        verify(repository, times(1)).save(any(MeetingAgenda.class));
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

}
