package com.cooperative.assembly.voting.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingSessionRequest {

    @NotBlank(message = "voting.session.agenda.id.not.empty")
    @NotNull(message = "voting.session.agenda.id.invalid")
    @Size(min= 36, max = 36, message = "voting.session.agenda.id.invalid.size")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", message = "voting.session.agenda.id.invalid.format")
    private String agendaId;

    @Positive(message = "voting.session.deadline.minutes.invalid")
    private Long deadlineMinutes;

}
