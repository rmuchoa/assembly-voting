package com.cooperative.assembly.vote;

import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.VotingSession;
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
    private VotingAgenda agenda;
    @DBRef
    private VotingSession session;
    private VoteChoice choice;

    public Vote(final String id, final String userId, final VotingAgenda agenda, final VotingSession session) {
        this.id = id;
        this.userId = userId;
        this.agenda = agenda;
        this.session = session;
    }

}
