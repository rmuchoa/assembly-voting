package com.cooperative.assembly.voting.meeting.agenda;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MeetingAgendaService {

    private MeetingAgendaRepository repository;

    @Autowired
    public MeetingAgendaService(MeetingAgendaRepository repository) {
        this.repository = repository;
    }

    public MeetingAgenda create(String title) {
        String id = UUID.randomUUID().toString();
        MeetingAgenda meetingAgenda = new MeetingAgenda(id, title);

        return repository.save(meetingAgenda);
    }

}
