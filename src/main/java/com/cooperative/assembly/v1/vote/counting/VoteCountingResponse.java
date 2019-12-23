package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.response.ResponseJson;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteCountingResponse {

    private String agenda;
    private VoteCountingStatus status;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private VotingSessionStatus session;

    public VoteCountingResponse(final String agenda, final Integer totalVotes, final Integer affirmativeVotes,
                                final Integer negativeVotes, final LocalDateTime openingTime, final LocalDateTime closingTime) {
        this.agenda = agenda;
        this.totalVotes = totalVotes;
        this.affirmativeVotes = affirmativeVotes;
        this.negativeVotes = negativeVotes;
        this.status = VoteCountingStatus.getByCountingVotes(affirmativeVotes, negativeVotes);
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.session = VotingSessionStatus.getStatusByTimeRange(openingTime, closingTime);
    }

    public static ResponseJson<VoteCountingResponse, Void> buildResponse(final VoteCounting counting) {
        VoteCountingResponse data = new VoteCountingResponse(counting.getAgenda(), counting.getTotalVotes(),
                counting.getAffirmativeVotes(), counting.getNegativeVotes(), counting.getOpeningTime(), counting.getClosingTime());
        return new ResponseJson<>(data);
    }

}
