package com.cooperative.assembly.voting.session.canvass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingSessionCanvassResponse {

    private String id;
    private String title;
    private Integer totalVotes;
    private Integer afirmativeVotes;
    private Integer negativeVotes;

}
