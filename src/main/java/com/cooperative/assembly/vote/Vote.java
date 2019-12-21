package com.cooperative.assembly.vote;

import com.cooperative.assembly.voting.agenda.VotingAgenda;
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
    private VoteChoice choice;

    public Vote(final String id, final String userId, final VotingAgenda agenda) {
        this.id = id;
        this.userId = userId;
        this.agenda = agenda;
    }

}
