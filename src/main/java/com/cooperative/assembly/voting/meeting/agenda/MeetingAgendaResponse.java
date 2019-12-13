package com.cooperative.assembly.voting.meeting.agenda;

import com.cooperative.assembly.voting.response.ResponseJson;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingAgendaResponse {

    private String id;
    private String title;

    public static ResponseJson<MeetingAgendaResponse, Void> buildResponse(MeetingAgenda meetingAgenda) {
        MeetingAgendaResponse data = new MeetingAgendaResponse(meetingAgenda.getId(), meetingAgenda.getTitle());
        return new ResponseJson<>(data);
    }

}
