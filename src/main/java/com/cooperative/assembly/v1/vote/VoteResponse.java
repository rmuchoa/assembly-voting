package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private String id;
    private String userId;
    private VotingSessionResponse session;
    private VoteChoice choice;

    public static ResponseJson<VoteResponse, Void> buildResponse(final Vote vote) {
        VotingSession session = vote.getSession();
        VotingAgenda agenda = session.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());

        VotingSessionResponse sessionResponse = new VotingSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime(), session.getStatus());
        VoteResponse data = new VoteResponse(vote.getId(), vote.getUserId(), sessionResponse, vote.getChoice());
        return new ResponseJson<>(data);
    }

}
