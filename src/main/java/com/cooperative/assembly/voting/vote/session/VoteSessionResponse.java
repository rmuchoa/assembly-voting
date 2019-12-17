package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import com.cooperative.assembly.voting.meeting.agenda.MeetingAgendaResponse;
import com.cooperative.assembly.voting.response.ResponseJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteSessionResponse {

    private String id;
    private MeetingAgendaResponse agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    public static ResponseJson<VoteSessionResponse, Void> buildResponse(VoteSession session) {
        MeetingAgenda agenda = session.getAgenda();
        MeetingAgendaResponse agendaResponse = new MeetingAgendaResponse(agenda.getId(), agenda.getTitle());

        VoteSessionResponse data = new VoteSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime());
        return new ResponseJson<>(data);
    }

}
