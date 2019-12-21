package com.cooperative.assembly.vote.counting;

import com.cooperative.assembly.voting.session.VotingSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteCounting {

    private String agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;

}
