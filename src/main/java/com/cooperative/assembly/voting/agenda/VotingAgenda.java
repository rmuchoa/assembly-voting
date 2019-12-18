package com.cooperative.assembly.voting.agenda;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "VotingAgenda")
public class VotingAgenda {

    @Id
    private String id;
    private String title;

    public VotingAgenda(String id) {
        this.id = id;
    }

}
