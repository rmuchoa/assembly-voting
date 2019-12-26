package com.cooperative.assembly.v1.voting.session.canvass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "VotingSessionCanvass")
public class VotingSessionCanvass {

    @Id
    private String id;
    private String title;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;

    /**
     * Increment an afirmative vote, increasing total and affirmative votes
     *
     */
    public void incrementAffirmative() {
        this.affirmativeVotes++;
        this.totalVotes++;
    }

    /**
     * Increment an negative vote, increasing total and negative votes
     *
     */
    public void incrementNegative() {
        this.negativeVotes++;
        this.totalVotes++;
    }

}
