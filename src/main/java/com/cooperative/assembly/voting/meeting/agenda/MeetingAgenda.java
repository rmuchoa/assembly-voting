package com.cooperative.assembly.voting.meeting.agenda;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "MeetingAgenda")
public class MeetingAgenda {

    @Id
    private String id;
    private String title;

    public MeetingAgenda(String id) {
        this.id = id;
    }

}
