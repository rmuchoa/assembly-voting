package com.cooperative.assembly.v1.voting.report;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.session.VotingSession;
import com.cooperative.assembly.v1.voting.session.VotingSessionStatus;
import com.cooperative.assembly.v1.voting.session.canvass.VotingSessionCanvass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingReport {

    private String title;
    private VotingSessionStatus status;
    private Integer totalVotes;
    private Integer affirmativeVotes;
    private Integer negativeVotes;
    private String agendaId;
    private String sessionId;

    public static VotingReport buildReport(final VotingSession session) {
        VotingAgenda agenda = session.getAgenda();
        VotingSessionCanvass canvass = session.getCanvass();
        return new VotingReport(canvass.getTitle(), session.getStatus(), canvass.getTotalVotes(),
                canvass.getAffirmativeVotes(), canvass.getNegativeVotes(), agenda.getId(), session.getId());
    }

}
