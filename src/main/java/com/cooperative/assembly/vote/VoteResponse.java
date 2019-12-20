package com.cooperative.assembly.vote;

import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private String id;
    private String userId;
    private VotingAgendaResponse agenda;
    private VoteChoice choice;

    public static ResponseJson<VoteResponse, Void> buildResponse(final Vote vote) {
        VotingAgenda agenda = vote.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());
        VoteResponse data = new VoteResponse(vote.getId(), vote.getUserId(), agendaResponse, vote.getChoice());
        return new ResponseJson<>(data);
    }

}
