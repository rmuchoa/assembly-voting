package com.cooperative.assembly.voting.meeting.agenda;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MeetingAgendaRequest {

    @NotBlank(message = "meeting.agenda.title.not.empty")
    @NotNull(message = "meeting.agenda.title.invalid")
    @Size(max = 100, message = "meeting.agenda.title.size.invalid")
    private String title;

}
