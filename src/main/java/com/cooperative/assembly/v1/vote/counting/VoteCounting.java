package com.cooperative.assembly.v1.vote.counting;

import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteCounting {

    private String agenda;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private VotingSessionStatus session;

}
