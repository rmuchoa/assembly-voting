package com.cooperative.assembly.voting.vote.session;

import com.cooperative.assembly.voting.meeting.agenda.MeetingAgenda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "VoteSession")
public class VoteSession {

    @Id
    private String id;
    @DBRef
    private MeetingAgenda agenda;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;

}
