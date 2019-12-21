package com.cooperative.assembly.voting.session.canvass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
