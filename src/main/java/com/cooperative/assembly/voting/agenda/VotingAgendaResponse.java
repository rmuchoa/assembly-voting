package com.cooperative.assembly.voting.agenda;

import com.cooperative.assembly.response.ResponseJson;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingAgendaResponse {

    private String id;
    private String title;

    public static ResponseJson<VotingAgendaResponse, Void> buildResponse(VotingAgenda agenda) {
        VotingAgendaResponse data = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());
        return new ResponseJson<>(data);
    }

}
