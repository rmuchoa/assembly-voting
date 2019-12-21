package com.cooperative.assembly.vote;

import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.VotingSessionResponse;
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
        VotingAgenda agenda = vote.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());

        VotingSession session = vote.getSession();
        VotingSessionResponse sessionResponse = new VotingSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime());
        VoteResponse data = new VoteResponse(vote.getId(), vote.getUserId(), sessionResponse, vote.getChoice());
        return new ResponseJson<>(data);
    }

}
