package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
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
    private VotingSessionStatus status;

    public static ResponseJson<VotingSessionResponse, Void> buildResponse(VotingSession session) {
        VotingAgenda agenda = session.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());

        VotingSessionResponse data = new VotingSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime(), session.getStatus());
        return new ResponseJson<>(data);
    }

}
