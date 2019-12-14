package com.cooperative.assembly.voting.meeting.agenda;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingAgenda {

    @Id
    private String id;
    private String title;

}
