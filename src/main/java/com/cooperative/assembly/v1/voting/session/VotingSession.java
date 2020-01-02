package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.isNoLongerOpenSession;

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
    private VotingSessionStatus status;
    private Boolean published;

    /**
     * Check if closing time is past before right now to infer this voting session is still opened
     *
     * @return
     */
    public Boolean isNoLongerOpen() {
        return isNoLongerOpenSession(this);
    }

}
