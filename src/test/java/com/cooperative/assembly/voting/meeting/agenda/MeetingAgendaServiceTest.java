package com.cooperative.assembly.voting.meeting.agenda;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MeetingAgendaService.class)
public class MeetingAgendaServiceTest {

    @Autowired
    private MeetingAgendaService service;

    @Test
    public void shouldReturnSavedMeetingAgendaAsIs() {
        String title = "agenda-title-1";

        MeetingAgenda meetingAgenda = service.create(title);

        assertThat(meetingAgenda, hasProperty("id", not(isEmptyString())));
        assertThat(meetingAgenda, hasProperty("title", equalTo(title)));
    }

}
