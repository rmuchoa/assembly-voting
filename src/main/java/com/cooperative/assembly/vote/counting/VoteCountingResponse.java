package com.cooperative.assembly.vote.counting;

import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.voting.session.VotingSession;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvassResponse;
import com.cooperative.assembly.voting.session.VotingSessionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteCountingResponse {

    private String agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;

    public static ResponseJson<VoteCountingResponse, Void> buildResponse(final VoteCounting counting) {
        VoteCountingResponse data = new VoteCountingResponse(counting.getAgenda(), counting.getOpeningTime(), counting.getClosingTime(),
                counting.getTotalVotes(), counting.getAffirmativeVotes(), counting.getNegativeVotes());
        return new ResponseJson<>(data);
    }

}