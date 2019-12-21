package com.cooperative.assembly.vote.counting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteCountingRequest {

    @NotBlank(message = "vote.counting.agenda.id.not.empty")
    @Size(min= 36, max = 36, message = "vote.counting.agenda.id.invalid.size")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", message = "vote.counting.agenda.id.invalid.uuid.format")
    private String agendaId;

}
