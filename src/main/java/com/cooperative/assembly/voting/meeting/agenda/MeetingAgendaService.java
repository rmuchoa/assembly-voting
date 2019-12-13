package com.cooperative.assembly.voting.meeting.agenda;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MeetingAgendaService {

    public MeetingAgenda create(String title) {
        String id = UUID.randomUUID().toString();
        MeetingAgenda meetingAgenda = new MeetingAgenda(id, title);
        return meetingAgenda;
    }

}
