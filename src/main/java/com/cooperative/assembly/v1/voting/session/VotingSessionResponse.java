package com.cooperative.assembly.v1.voting.session;

import com.cooperative.assembly.v1.voting.agenda.VotingAgenda;
import com.cooperative.assembly.v1.voting.agenda.VotingAgendaResponse;
import com.cooperative.assembly.response.ResponseJson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.cooperative.assembly.v1.voting.session.VotingSessionStatus.getStatusByTimeRange;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingSessionResponse {

    private String id;
    private VotingAgendaResponse agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private VotingSessionStatus status;

    public VotingSessionResponse(final String id, final VotingAgendaResponse agenda,
                                 final LocalDateTime openingTime, final LocalDateTime closingTime) {
        this.id = id;
        this.agenda = agenda;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.status = getStatusByTimeRange(openingTime, closingTime);
    }

    public static ResponseJson<VotingSessionResponse, Void> buildResponse(VotingSession session) {
        VotingAgenda agenda = session.getAgenda();
        VotingAgendaResponse agendaResponse = new VotingAgendaResponse(agenda.getId(), agenda.getTitle());

        VotingSessionResponse data = new VotingSessionResponse(session.getId(), agendaResponse, session.getOpeningTime(), session.getClosingTime());
        return new ResponseJson<>(data);
    }

}
