package com.cooperative.assembly.voting.session;

import com.cooperative.assembly.voting.agenda.VotingAgenda;
import com.cooperative.assembly.voting.session.canvass.VotingSessionCanvass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "VotingSession")
public class VotingSession {

    @Id
    private String id;
    @DBRef
    private VotingAgenda agenda;
    @DBRef
    private VotingSessionCanvass canvass;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

    /**
     * Check if closing time is past before right now to infer this voting session is still open
     *
     * @return
     */
    public Boolean isNoLongerOpen() {
        return now().isAfter(closingTime);
    }

}
