package com.cooperative.assembly.v1.voting.agenda;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class VotingAgendaRequest {

    @NotBlank(message = "voting.agenda.title.not.empty")
    @Size(max = 100, message = "voting.agenda.title.invalid.size")
    private String title;

}
