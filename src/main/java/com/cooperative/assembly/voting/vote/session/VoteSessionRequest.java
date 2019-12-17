package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteSessionRequest {

    @NotBlank(message = "vote.session.agenda.id.not.empty")
    @NotNull(message = "vote.session.agenda.id.invalid")
    @Size(min= 36, max = 36, message = "vote.session.agenda.id.invalid.size")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", message = "vote.session.agenda.id.invalid.format")
    private String agendaId;

    @Positive(message = "vote.session.deadline.minutes.invalid")
    private Long deadlineMinutes;

}
