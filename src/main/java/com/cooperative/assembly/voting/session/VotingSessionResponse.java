package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.response.ResponseJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingSessionResponse {

    private String id;
    private VotingAgendaResponse agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    public static ResponseJson<VotingSessionResponse, Void> buildResponse(VotingSession session) {
        VotingAgenda agenda = session.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());

        VotingSessionResponse data = new VotingSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime());
        return new ResponseJson<>(data);
    }

}
