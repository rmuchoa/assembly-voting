package com.cooperative.assembly.v1.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotBlank(message = "vote.user.id.not.empty")
    @CPF(message = "vote.user.id.invalid.cpf.format")
    private String userId;

    @NotBlank(message = "vote.agenda.id.not.empty")
    @Size(min= 36, max = 36, message = "vote.agenda.id.invalid.size")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", message = "vote.agenda.id.invalid.uuid.format")
    private String agendaId;

    @NotNull(message = "vote.choice.not.null")
    private VoteChoice choice;

    public String getUserId() {
        return userId.replaceAll("\\D+","");
    }

}
