package com.cooperative.assembly.v1.vote;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Vote")
public class Vote {

    @Id
    private String id;
    private String userId;
    @DBRef
    private VotingSession session;
    private VoteChoice choice;

    public Vote(final String id, final String userId, final VotingSession session) {
        this.id = id;
        this.userId = userId;
        this.session = session;
    }

}
